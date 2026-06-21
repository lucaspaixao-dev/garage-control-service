package io.github.lucaspaixaodev.garageservice.domain.spot.valueobject

import io.github.lucaspaixaodev.garageservice.domain.exception.SpotException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.junit.jupiter.api.Test

class LatitudeTest {

    @Test
    fun `accepts values within the valid range`() {
        assertEquals(-90.0, Latitude(value = -90.0).value)
        assertEquals(90.0, Latitude(value = 90.0).value)
        assertEquals(-23.561684, Latitude.of(value = -23.561684).value)
    }

    @Test
    fun `rejects values below the minimum`() {
        val exception = assertFailsWith<SpotException.InvalidLatitude> { Latitude(value = -90.1) }

        assertEquals("Latitude must be between -90.0 and 90.0 degrees, got: -90.1", exception.message)
    }

    @Test
    fun `rejects values above the maximum`() {
        assertFailsWith<SpotException.InvalidLatitude> { Latitude(value = 90.1) }
    }
}
