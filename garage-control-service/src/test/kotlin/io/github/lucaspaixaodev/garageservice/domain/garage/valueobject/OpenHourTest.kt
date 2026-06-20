package io.github.lucaspaixaodev.garageservice.domain.garage.valueobject

import io.github.lucaspaixaodev.garageservice.domain.exception.GarageException
import org.junit.jupiter.api.Test
import java.time.LocalTime
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OpenHourTest {

    @Test
    fun `of parses HH mm`() {
        val open = OpenHour.of(value = "08:00")

        assertEquals(LocalTime.of(8, 0), open.value)
        assertEquals("08:00", open.toString())
    }

    @Test
    fun `isAfter compares against a close hour`() {
        assertTrue(OpenHour.of(value = "10:00").isAfter(closeHour = CloseHour.of(value = "09:00")))
        assertFalse(OpenHour.of(value = "08:00").isAfter(closeHour = CloseHour.of(value = "09:00")))
    }

    @Test
    fun `of rejects an invalid format`() {
        val exception = assertFailsWith<GarageException.InvalidOpenHour> { OpenHour.of(value = "25:99") }

        assertEquals("Invalid open hour '25:99': expected format HH:mm", exception.message)
    }
}
