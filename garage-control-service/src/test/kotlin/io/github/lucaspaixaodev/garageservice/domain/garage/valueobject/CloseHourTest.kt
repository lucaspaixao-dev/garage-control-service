package io.github.lucaspaixaodev.garageservice.domain.garage.valueobject

import io.github.lucaspaixaodev.garageservice.domain.exception.GarageException
import java.time.LocalTime
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test

class CloseHourTest {

    @Test
    fun `of parses HH mm`() {
        val close = CloseHour.of(value = "23:59")

        assertEquals(LocalTime.of(23, 59), close.value)
        assertEquals("23:59", close.toString())
    }

    @Test
    fun `isBefore compares against an open hour`() {
        assertTrue(CloseHour.of(value = "07:00").isBefore(openHour = OpenHour.of(value = "08:00")))
        assertFalse(CloseHour.of(value = "09:00").isBefore(openHour = OpenHour.of(value = "08:00")))
    }

    @Test
    fun `of rejects an invalid format`() {
        val exception = assertFailsWith<GarageException.InvalidCloseHour> { CloseHour.of(value = "invalid") }

        assertEquals("Invalid close hour 'invalid': expected format HH:mm", exception.message)
    }
}
