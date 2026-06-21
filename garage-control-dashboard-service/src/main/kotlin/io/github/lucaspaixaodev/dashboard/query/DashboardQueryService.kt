package io.github.lucaspaixaodev.dashboard.query

import io.github.lucaspaixaodev.dashboard.api.DashboardView
import io.github.lucaspaixaodev.dashboard.api.SectorRevenueView
import io.github.lucaspaixaodev.dashboard.api.SpotView
import io.github.lucaspaixaodev.dashboard.api.SummaryView
import io.github.lucaspaixaodev.dashboard.api.TicketEventView
import io.github.lucaspaixaodev.dashboard.api.TicketView
import io.github.lucaspaixaodev.dashboard.persistence.GarageRepository
import io.github.lucaspaixaodev.dashboard.persistence.SpotEntity
import io.github.lucaspaixaodev.dashboard.persistence.SpotRepository
import io.github.lucaspaixaodev.dashboard.persistence.TicketEntity
import io.github.lucaspaixaodev.dashboard.persistence.TicketEventEntity
import io.github.lucaspaixaodev.dashboard.persistence.TicketEventRepository
import io.github.lucaspaixaodev.dashboard.persistence.TicketRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class DashboardQueryService(
    private val spotRepository: SpotRepository,
    private val ticketRepository: TicketRepository,
    private val ticketEventRepository: TicketEventRepository,
    private val garageRepository: GarageRepository,
) {

    private companion object {
        private const val RECENT_TICKETS = 60
        private val EVENT_ORDER = mapOf("ENTRY" to 0, "PARKED" to 1, "EXIT" to 2)
    }

    @Transactional(readOnly = true)
    fun snapshot(): DashboardView {
        val spots = spotRepository.findAll().sortedBy { it.externalId }
        val sectorByGarageId = garageRepository.findAll().associate { it.id to it.sector }

        return DashboardView(
            summary = summaryOf(spots = spots, sectors = sectorByGarageId.values.toSortedSet().toList()),
            spots = spots.map { it.toView(sector = sectorByGarageId[it.garageId]) },
            tickets = recentTickets(),
        )
    }

    private fun summaryOf(spots: List<SpotEntity>, sectors: List<String>): SummaryView {
        val total = spots.size
        val occupied = spots.count { it.occupied }
        val today = LocalDate.now()
        return SummaryView(
            totalSpots = total,
            occupiedSpots = occupied,
            freeSpots = total - occupied,
            occupancyRate = if (total > 0) occupied.toDouble() / total else 0.0,
            revenueBySector =
                sectors.map { sector ->
                    SectorRevenueView(
                        sector = sector,
                        amount =
                            ticketRepository.sumFareBySectorAndPaidAtBetween(
                                sector = sector,
                                start = today.atStartOfDay(),
                                end = today.plusDays(1).atStartOfDay(),
                            ),
                    )
                },
        )
    }

    private fun recentTickets(): List<TicketView> {
        val tickets = ticketRepository.findRecent(limit = RECENT_TICKETS)
        val eventsByTicket =
            ticketEventRepository.findAllByIdTicketIdIn(tickets.map { it.id })
                .groupBy { it.id.ticketId }
        return tickets.map { it.toView(events = eventsByTicket[it.id].orEmpty()) }
    }

    private fun SpotEntity.toView(sector: String?): SpotView =
        SpotView(
            externalId = externalId,
            sector = sector,
            latitude = latitude,
            longitude = longitude,
            occupied = occupied,
        )

    private fun TicketEntity.toView(events: List<TicketEventEntity>): TicketView =
        TicketView(
            id = id.toString(),
            licensePlate = licensePlate,
            sector = sector,
            status = status,
            hourlyPrice = hourlyPrice,
            fare = fare,
            events =
                events
                    .sortedBy { EVENT_ORDER[it.id.type] ?: Int.MAX_VALUE }
                    .map { TicketEventView(type = it.id.type, time = it.eventTime) },
        )
}
