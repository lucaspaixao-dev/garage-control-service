package io.github.lucaspaixaodev.garageservice.infra.output.repository.ticket

import io.github.lucaspaixaodev.garageservice.domain.ticket.Ticket
import io.github.lucaspaixaodev.garageservice.domain.ticket.TicketEventType
import io.github.lucaspaixaodev.garageservice.domain.ticket.TicketStatus
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class JpaTicketRepositoryTest {

    private val ticketEntityRepository = mockk<TicketEntityRepository>(relaxed = true)
    private val ticketEventEntityRepository = mockk<TicketEventEntityRepository>(relaxed = true)
    private val repository =
        JpaTicketRepository(
            ticketEntityRepository = ticketEntityRepository,
            ticketEventEntityRepository = ticketEventEntityRepository,
        )

    @Test
    fun `save persists the ticket and replaces its events keyed by ticket id and type`() {
        val ticket = Ticket.entry(licensePlate = "ZUL0001", entryTime = LocalDateTime.parse("2025-01-01T12:00:00"))
        every { ticketEntityRepository.save(any()) } answers { firstArg() }
        val savedEvents = slot<List<TicketEventEntity>>()
        every { ticketEventEntityRepository.saveAll(capture(savedEvents)) } answers { savedEvents.captured }

        repository.save(ticket = ticket)

        verify { ticketEntityRepository.save(match { it.licensePlate == "ZUL0001" && it.status == TicketStatus.OPEN }) }
        verify { ticketEventEntityRepository.deleteAllByIdTicketId(ticketId = ticket.id.value) }
        assertEquals(1, savedEvents.captured.size)
        assertEquals(TicketEventType.ENTRY, savedEvents.captured.first().id.type)
        assertEquals(ticket.id.value, savedEvents.captured.first().id.ticketId)
    }

    @Test
    fun `findOpenByLicensePlate reconstructs the ticket with its events`() {
        val ticketId = UUID.randomUUID()
        val spotId = UUID.randomUUID()
        every { ticketEntityRepository.findFirstByLicensePlateAndStatus("ZUL0001", TicketStatus.OPEN) } returns
            TicketEntity(id = ticketId, licensePlate = "ZUL0001", spotId = spotId, status = TicketStatus.OPEN)
        every { ticketEventEntityRepository.findAllByIdTicketId(ticketId) } returns
            listOf(
                TicketEventEntity(
                    id = TicketEventId(ticketId = ticketId, type = TicketEventType.ENTRY),
                    eventTime = LocalDateTime.parse("2025-01-01T12:00:00"),
                ),
            )

        val ticket = repository.findOpenByLicensePlate(licensePlate = "ZUL0001")

        assertNotNull(ticket)
        assertEquals("ZUL0001", ticket.vehicle.licensePlate)
        assertEquals(spotId, ticket.spotId?.value)
        assertEquals(1, ticket.events.size)
        assertEquals(TicketEventType.ENTRY, ticket.events.first().type)
    }

    @Test
    fun `findOpenByLicensePlate returns null when there is none`() {
        every { ticketEntityRepository.findFirstByLicensePlateAndStatus(any(), any()) } returns null

        assertNull(repository.findOpenByLicensePlate(licensePlate = "UNKNOWN"))
    }
}
