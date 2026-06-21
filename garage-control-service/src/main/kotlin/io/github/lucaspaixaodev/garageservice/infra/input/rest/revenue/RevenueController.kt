package io.github.lucaspaixaodev.garageservice.infra.input.rest.revenue

import io.github.lucaspaixaodev.garageservice.application.ticket.usecase.GetRevenueUseCase
import io.github.lucaspaixaodev.garageservice.application.ticket.usecase.RevenueQuery
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import org.slf4j.LoggerFactory
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/revenue")
class RevenueController(
    private val getRevenueUseCase: GetRevenueUseCase
) {

    private companion object {
        private val logger = LoggerFactory.getLogger(RevenueController::class.java)
        private const val CURRENCY = "BRL"
    }

    @GetMapping
    fun revenue(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate,
        @RequestParam sector: String,
    ): ResponseEntity<RevenueResponse> {
        logger.info("GET /revenue date=$date sector=$sector")

        val result = getRevenueUseCase.execute(query = RevenueQuery(date = date, sector = sector))

        return ResponseEntity.ok(
            RevenueResponse(
                amount = result.amount.amount,
                currency = CURRENCY,
                timestamp = result.timestamp,
            ),
        )
    }
}

data class RevenueResponse(
    val amount: BigDecimal,
    val currency: String,
    val timestamp: LocalDateTime,
)
