package io.github.lucaspaixaodev.garageservice.application.ticket.usecase

import io.github.lucaspaixaodev.garageservice.application.ticket.repository.TicketRepository
import io.github.lucaspaixaodev.garageservice.domain.garage.valueobject.GarageSector
import io.github.lucaspaixaodev.garageservice.domain.garage.valueobject.Money
import java.time.LocalDate
import java.time.LocalDateTime
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class GetRevenueUseCase(
    private val ticketRepository: TicketRepository,
) {

    @Transactional(readOnly = true)
    fun execute(query: RevenueQuery): RevenueResult {
        val sector = GarageSector.of(query.sector)
        val amount = ticketRepository.totalRevenue(sector = sector, date = query.date)
        return RevenueResult(amount = amount, timestamp = LocalDateTime.now())
    }
}

data class RevenueQuery(
    val date: LocalDate,
    val sector: String,
)

data class RevenueResult(
    val amount: Money,
    val timestamp: LocalDateTime,
)
