package io.github.lucaspaixaodev.dashboard.api

import java.math.BigDecimal
import java.time.LocalDateTime

data class DashboardView(
    val summary: SummaryView,
    val spots: List<SpotView>,
    val tickets: List<TicketView>,
)

data class SummaryView(
    val totalSpots: Int,
    val occupiedSpots: Int,
    val freeSpots: Int,
    val occupancyRate: Double,
    val revenueBySector: List<SectorRevenueView>,
)

data class SectorRevenueView(
    val sector: String,
    val amount: BigDecimal,
)

data class SpotView(
    val externalId: Int,
    val sector: String?,
    val latitude: Double,
    val longitude: Double,
    val occupied: Boolean,
)

data class TicketView(
    val id: String,
    val licensePlate: String,
    val sector: String?,
    val status: String,
    val hourlyPrice: BigDecimal?,
    val fare: BigDecimal?,
    val events: List<TicketEventView>,
)

data class TicketEventView(
    val type: String,
    val time: LocalDateTime?,
)
