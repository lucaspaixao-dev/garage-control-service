package io.github.lucaspaixaodev.garageservice.infra.input.rest.revenue

import io.github.lucaspaixaodev.garageservice.application.ticket.usecase.GetRevenueUseCase
import io.github.lucaspaixaodev.garageservice.application.ticket.usecase.RevenueQuery
import io.github.lucaspaixaodev.garageservice.application.ticket.usecase.RevenueResult
import io.github.lucaspaixaodev.garageservice.domain.garage.valueobject.Money
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class RevenueControllerTest {

    private val getRevenueUseCase = mockk<GetRevenueUseCase>()
    private val controller = RevenueController(getRevenueUseCase = getRevenueUseCase)

    @Test
    fun `revenue returns 200 with the amount and BRL currency`() {
        val date = LocalDate.parse("2025-01-01")
        every { getRevenueUseCase.execute(query = RevenueQuery(date = date, sector = "A")) } returns
                RevenueResult(
                    amount = Money.of(amount = "42.00"),
                    timestamp = LocalDateTime.parse("2025-01-01T12:00:00")
                )

        val response = controller.revenue(date = date, sector = "A")

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(BigDecimal("42.00"), response.body!!.amount)
        assertEquals("BRL", response.body!!.currency)
        verify(exactly = 1) { getRevenueUseCase.execute(query = RevenueQuery(date = date, sector = "A")) }
    }
}
