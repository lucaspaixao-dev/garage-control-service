package io.github.lucaspaixaodev.garageservice.application.ticket.usecase

import io.github.lucaspaixaodev.garageservice.application.ticket.repository.TicketRepository
import io.github.lucaspaixaodev.garageservice.domain.exception.GarageException
import io.github.lucaspaixaodev.garageservice.domain.garage.valueobject.GarageSector
import io.github.lucaspaixaodev.garageservice.domain.garage.valueobject.Money
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class GetRevenueUseCaseTest {

    private val ticketRepository = mockk<TicketRepository>()
    private val useCase = GetRevenueUseCase(ticketRepository = ticketRepository)

    @Test
    fun `returns the total revenue for the sector and date`() {
        val date = LocalDate.parse("2025-01-01")
        every { ticketRepository.totalRevenue(sector = GarageSector.A, date = date) } returns Money.of(amount = "42.00")

        val result = useCase.execute(query = RevenueQuery(date = date, sector = "A"))

        assertEquals(Money.of(amount = "42.00"), result.amount)
    }

    @Test
    fun `rejects an unknown sector`() {
        assertThrows<GarageException.InvalidSector> {
            useCase.execute(query = RevenueQuery(date = LocalDate.parse("2025-01-01"), sector = "Z"))
        }
    }
}
