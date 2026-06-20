package io.github.lucaspaixaodev.garageservice.domain.garage.valueobject

import io.github.lucaspaixaodev.garageservice.domain.exception.GarageException
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GarageSectorTest {

    @Test
    fun `of parses sectors case-insensitively and trimmed`() {
        assertEquals(GarageSector.A, GarageSector.of(value = "A"))
        assertEquals(GarageSector.A, GarageSector.of(value = "a"))
        assertEquals(GarageSector.B, GarageSector.of(value = " b "))
    }

    @Test
    fun `of rejects an unknown sector`() {
        val exception = assertFailsWith<GarageException.InvalidSector> { GarageSector.of(value = "C") }

        assertEquals("Invalid garage sector 'C'. Allowed values: A, B", exception.message)
    }
}
