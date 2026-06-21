package io.github.lucaspaixaodev.garageservice.domain.ticket.valueobject

import io.github.lucaspaixaodev.garageservice.domain.exception.TicketException
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class VehicleTest {

    @Test
    fun `accepts a non-blank license plate`() {
        assertEquals("ZUL0001", Vehicle(licensePlate = "ZUL0001").licensePlate)
    }

    @Test
    fun `rejects a blank license plate`() {
        assertThrows<TicketException.InvalidLicensePlate> { Vehicle(licensePlate = "   ") }
    }
}
