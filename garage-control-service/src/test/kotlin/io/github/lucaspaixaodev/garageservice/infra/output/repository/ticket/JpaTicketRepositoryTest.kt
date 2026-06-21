package io.github.lucaspaixaodev.garageservice.infra.output.repository.ticket

import io.github.lucaspaixaodev.garageservice.domain.garage.valueobject.GarageSector
import io.github.lucaspaixaodev.garageservice.domain.garage.valueobject.Money
import io.github.lucaspaixaodev.garageservice.domain.ticket.Ticket
import io.github.lucaspaixaodev.garageservice.domain.ticket.valueobject.TicketCharge
import io.github.lucaspaixaodev.garageservice.domain.ticket.valueobject.TicketEvent
import io.github.lucaspaixaodev.garageservice.domain.ticket.valueobject.TicketEventTime
import io.github.lucaspaixaodev.garageservice.domain.ticket.valueobject.TicketEventType
import io.github.lucaspaixaodev.garageservice.domain.ticket.valueobject.TicketStatus
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import org.junit.jupiter.api.Test

class JpaTicketRepositoryTest {

    private val ticketEntityRepository = mockk<TicketEntityRepository>(relaxed = true)
    private val ticketEventEntityRepository = mockk<TicketEventEntityRepository>(relaxed = true)
    private val repository =
        JpaTicketRepository(
            ticketEntityRepository = ticketEntityRepository,
            ticketEventEntityRepository = ticketEventEntityRepository,
        )

    private val entryTime = LocalDateTime.parse("2025-01-01T12:00:00")

    @Test
    fun `save persists the ticket and replaces its events keyed by ticket id and type`() {
        val ticket = Ticket.entry(licensePlate = "ZUL0001", entryTime = entryTime)
        every { ticketEntityRepository.save(any()) } answers { firstArg() }
        val savedEvents = slot<List<TicketEventEntity>>()
        every { ticketEventEntityRepository.saveAll(capture(savedEvents)) } answers { savedEvents.captured }

        repository.save(ticket = ticket)

        verify {
            ticketEntityRepository.save(
                match { it.licensePlate == "ZUL0001" && it.status == TicketStatus.OPEN && it.sector == null },
            )
        }
        verify { ticketEventEntityRepository.deleteAllByIdTicketId(ticketId = ticket.id.value) }
        assertEquals(1, savedEvents.captured.size)
        assertEquals(TicketEventType.ENTRY, savedEvents.captured.first().id.type)
        assertEquals(ticket.id.value, savedEvents.captured.first().id.ticketId)
    }

    @Test
    fun `save persists the pricing snapshot and settlement of a closed ticket`() {
        val exitTime = LocalDateTime.parse("2025-01-01T14:00:00")
        val ticket =
            Ticket.restore(
                id = UUID.randomUUID().toString(),
                licensePlate = "ZUL0001",
                spotId = UUID.randomUUID().toString(),
                status = TicketStatus.CLOSED,
                events =
                    listOf(
                        TicketEvent(type = TicketEventType.ENTRY, time = TicketEventTime(entryTime)),
                        TicketEvent(type = TicketEventType.PARKED),
                        TicketEvent(type = TicketEventType.EXIT, time = TicketEventTime(exitTime)),
                    ),
                charge =
                    TicketCharge(
                        sector = GarageSector.A,
                        hourlyPrice = Money.of(amount = "10.00"),
                        fare = Money.of(amount = "20.00"),
                    ),
            )
        val savedEntity = slot<TicketEntity>()
        every { ticketEntityRepository.save(capture(savedEntity)) } answers { savedEntity.captured }

        repository.save(ticket = ticket)

        assertEquals(GarageSector.A, savedEntity.captured.sector)
        assertEquals(BigDecimal("10.00"), savedEntity.captured.hourlyPrice)
        assertEquals(BigDecimal("20.00"), savedEntity.captured.fare)
        assertEquals(exitTime, savedEntity.captured.paidAt)
    }

    @Test
    fun `findOpenByLicensePlate reconstructs the ticket with its events and charge`() {
        val ticketId = UUID.randomUUID()
        val spotId = UUID.randomUUID()
        every { ticketEntityRepository.findFirstByLicensePlateAndStatus("ZUL0001", TicketStatus.OPEN) } returns
                TicketEntity(
                    id = ticketId,
                    licensePlate = "ZUL0001",
                    spotId = spotId,
                    status = TicketStatus.OPEN,
                    sector = GarageSector.A,
                    hourlyPrice = BigDecimal("10.00"),
                )
        every { ticketEventEntityRepository.findAllByIdTicketId(ticketId) } returns
                listOf(
                    TicketEventEntity(
                        id = TicketEventId(ticketId = ticketId, type = TicketEventType.ENTRY),
                        eventTime = entryTime,
                    ),
                )

        val ticket = repository.findOpenByLicensePlate(licensePlate = "ZUL0001")

        assertNotNull(ticket)
        assertEquals("ZUL0001", ticket.vehicle.licensePlate)
        assertEquals(spotId, ticket.spotId?.value)
        assertEquals(GarageSector.A, ticket.charge?.sector)
        assertEquals(Money.of(amount = "10.00"), ticket.charge?.hourlyPrice)
        assertEquals(1, ticket.events.size)
        assertEquals(TicketEventType.ENTRY, ticket.events.first().type)
    }

    @Test
    fun `findOpenByLicensePlate leaves the charge null when not yet parked`() {
        val ticketId = UUID.randomUUID()
        every { ticketEntityRepository.findFirstByLicensePlateAndStatus("ZUL0001", TicketStatus.OPEN) } returns
                TicketEntity(id = ticketId, licensePlate = "ZUL0001", spotId = null, status = TicketStatus.OPEN)
        every { ticketEventEntityRepository.findAllByIdTicketId(ticketId) } returns emptyList()

        val ticket = repository.findOpenByLicensePlate(licensePlate = "ZUL0001")

        assertNull(ticket!!.charge)
    }

    @Test
    fun `findOpenByLicensePlate returns null when there is none`() {
        every { ticketEntityRepository.findFirstByLicensePlateAndStatus(any(), any()) } returns null

        assertNull(repository.findOpenByLicensePlate(licensePlate = "UNKNOWN"))
    }

    @Test
    fun `totalRevenue sums fares for the sector over the exit day`() {
        val date = LocalDate.parse("2025-01-01")
        every {
            ticketEntityRepository.sumFareBySectorAndPaidAtBetween(
                sector = GarageSector.A,
                start = date.atStartOfDay(),
                end = date.plusDays(1).atStartOfDay(),
            )
        } returns BigDecimal("123.45")

        val revenue = repository.totalRevenue(sector = GarageSector.A, date = date)

        assertEquals(Money.of(amount = "123.45"), revenue)
    }
}
