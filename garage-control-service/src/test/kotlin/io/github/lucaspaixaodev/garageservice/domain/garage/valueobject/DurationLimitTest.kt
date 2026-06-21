package io.github.lucaspaixaodev.garageservice.domain.garage.valueobject

import io.github.lucaspaixaodev.garageservice.domain.exception.GarageException
import java.time.Duration
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.junit.jupiter.api.Test

class DurationLimitTest {

    @Test
    fun `valid limit exposes minutes and duration`() {
        val limit = DurationLimit(minutes = 60)

        assertEquals(60, limit.minutes)
        assertEquals(Duration.ofMinutes(60), limit.duration)
        assertEquals("60 min", limit.toString())
    }

    @Test
    fun `ofMinutes builds a duration limit`() {
        assertEquals(90, DurationLimit.ofMinutes(minutes = 90).minutes)
    }

    @Test
    fun `zero minutes is rejected`() {
        val exception = assertFailsWith<GarageException.InvalidDurationLimit> { DurationLimit(minutes = 0) }

        assertEquals("Duration limit must be greater than 0 minutes, got: 0", exception.message)
    }

    @Test
    fun `negative minutes is rejected`() {
        assertFailsWith<GarageException.InvalidDurationLimit> { DurationLimit(minutes = -5) }
    }
}
