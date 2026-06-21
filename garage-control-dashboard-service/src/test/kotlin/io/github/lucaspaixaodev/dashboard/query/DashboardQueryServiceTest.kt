package io.github.lucaspaixaodev.dashboard.query

import io.github.lucaspaixaodev.dashboard.persistence.GarageEntity
import io.github.lucaspaixaodev.dashboard.persistence.GarageRepository
import io.github.lucaspaixaodev.dashboard.persistence.SpotEntity
import io.github.lucaspaixaodev.dashboard.persistence.SpotRepository
import io.github.lucaspaixaodev.dashboard.persistence.TicketEntity
import io.github.lucaspaixaodev.dashboard.persistence.TicketEventEntity
import io.github.lucaspaixaodev.dashboard.persistence.TicketEventId
import io.github.lucaspaixaodev.dashboard.persistence.TicketEventRepository
import io.github.lucaspaixaodev.dashboard.persistence.TicketRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.assertEquals

class DashboardQueryServiceTest {

    private val spotRepository = mockk<SpotRepository>()
    private val ticketRepository = mockk<TicketRepository>()
    private val ticketEventRepository = mockk<TicketEventRepository>()
    private val garageRepository = mockk<GarageRepository>()
    private val service =
        DashboardQueryService(
            spotRepository = spotRepository,
            ticketRepository = ticketRepository,
            ticketEventRepository = ticketEventRepository,
            garageRepository = garageRepository,
        )

    @Test
    fun `builds a snapshot with summary, sector-resolved spots and ordered ticket events`() {
        val garageId = UUID.randomUUID()
        val ticketId = UUID.randomUUID()
        every { spotRepository.findAll() } returns
            listOf(
                SpotEntity(UUID.randomUUID(), 2, garageId, -23.5, -46.6, false),
                SpotEntity(UUID.randomUUID(), 1, garageId, -23.4, -46.5, true),
            )
        every { garageRepository.findAll() } returns listOf(GarageEntity(garageId, "A"))
        every { ticketRepository.sumFareBySectorAndPaidAtBetween(sector = "A", start = any(), end = any()) } returns
            BigDecimal("100.00")
        every { ticketRepository.findRecent(limit = 60) } returns
            listOf(
                TicketEntity(
                    id = ticketId,
                    licensePlate = "ZUL0001",
                    sector = "A",
                    status = "CLOSED",
                    hourlyPrice = BigDecimal("40.50"),
                    fare = BigDecimal("81.00"),
                    paidAt = LocalDateTime.parse("2025-01-01T14:00:00"),
                ),
            )
        every { ticketEventRepository.findAllByIdTicketIdIn(listOf(ticketId)) } returns
            listOf(
                TicketEventEntity(TicketEventId(ticketId, "EXIT"), LocalDateTime.parse("2025-01-01T14:00:00")),
                TicketEventEntity(TicketEventId(ticketId, "ENTRY"), LocalDateTime.parse("2025-01-01T12:00:00")),
            )

        val view = service.snapshot()

        assertEquals(2, view.summary.totalSpots)
        assertEquals(1, view.summary.occupiedSpots)
        assertEquals(1, view.summary.freeSpots)
        assertEquals(0.5, view.summary.occupancyRate)
        assertEquals(BigDecimal("100.00"), view.summary.revenueBySector.single { it.sector == "A" }.amount)
        assertEquals(listOf(1, 2), view.spots.map { it.externalId })
        assertEquals("A", view.spots.first().sector)
        assertEquals(1, view.tickets.size)
        assertEquals(BigDecimal("81.00"), view.tickets.first().fare)
        assertEquals(listOf("ENTRY", "EXIT"), view.tickets.first().events.map { it.type })
    }

    @Test
    fun `reports zero occupancy and no revenue when there is nothing yet`() {
        every { spotRepository.findAll() } returns emptyList()
        every { garageRepository.findAll() } returns emptyList()
        every { ticketRepository.findRecent(limit = any()) } returns emptyList()
        every { ticketEventRepository.findAllByIdTicketIdIn(any()) } returns emptyList()

        val view = service.snapshot()

        assertEquals(0, view.summary.totalSpots)
        assertEquals(0.0, view.summary.occupancyRate)
        assertEquals(0, view.summary.revenueBySector.size)
        assertEquals(0, view.tickets.size)
    }
}
