package io.github.lucaspaixaodev.garageservice.domain.spot.valueobject

import io.github.lucaspaixaodev.garageservice.domain.exception.SpotException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.junit.jupiter.api.Test

class LongitudeTest {

    @Test
    fun `accepts values within the valid range`() {
        assertEquals(-180.0, Longitude(value = -180.0).value)
        assertEquals(180.0, Longitude(value = 180.0).value)
        assertEquals(-46.655981, Longitude.of(value = -46.655981).value)
    }

    @Test
    fun `rejects values below the minimum`() {
        val exception = assertFailsWith<SpotException.InvalidLongitude> { Longitude(value = -180.1) }

        assertEquals("Longitude must be between -180.0 and 180.0 degrees, got: -180.1", exception.message)
    }

    @Test
    fun `rejects values above the maximum`() {
        assertFailsWith<SpotException.InvalidLongitude> { Longitude(value = 180.1) }
    }
}
