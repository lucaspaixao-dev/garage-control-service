package io.github.lucaspaixaodev.garageservice.infra.input.rest

import io.github.lucaspaixaodev.garageservice.domain.exception.GarageApiException
import io.github.lucaspaixaodev.garageservice.domain.exception.GarageException
import io.github.lucaspaixaodev.garageservice.domain.exception.MoneyException
import io.github.lucaspaixaodev.garageservice.domain.exception.SpotException
import io.github.lucaspaixaodev.garageservice.domain.exception.TicketException
import java.math.BigDecimal
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class DomainExceptionHandlerTest {

    private val handler = DomainExceptionHandler()

    @Test
    fun `garage exception maps to 422 with its message`() {
        val response = handler.handleDomain(exception = GarageException.InvalidSector(value = "C", allowed = "A, B"))

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.statusCode)
        assertEquals("Invalid garage sector 'C'. Allowed values: A, B", response.body!!.message)
    }

    @Test
    fun `spot exception maps to 422`() {
        val response = handler.handleDomain(exception = SpotException.InvalidExternalId(value = 0))

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.statusCode)
    }

    @Test
    fun `ticket exception maps to 422`() {
        val response = handler.handleDomain(exception = TicketException.InvalidLicensePlate())

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.statusCode)
        assertEquals("Vehicle license plate must not be blank", response.body!!.message)
    }

    @Test
    fun `money exception maps to 422`() {
        val response = handler.handleDomain(exception = MoneyException.NegativeAmount(amount = BigDecimal("-1")))

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.statusCode)
    }

    @Test
    fun `garage api exception maps to 502`() {
        val response = handler.handleDomain(exception = GarageApiException.EmptyResponse())

        assertEquals(HttpStatus.BAD_GATEWAY, response.statusCode)
        assertEquals("Garage API returned an empty response", response.body!!.message)
    }

    @Test
    fun `unexpected exception maps to 500 with a generic message`() {
        val response = handler.handleUnexpected(exception = RuntimeException("boom"))

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertEquals("An unexpected error occurred", response.body!!.message)
    }
}
