package io.github.lucaspaixaodev.garageservice.domain.spot.valueobject

import io.github.lucaspaixaodev.garageservice.domain.exception.SpotException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.junit.jupiter.api.Test

class ExternalIdTest {

    @Test
    fun `accepts a positive value`() {
        assertEquals(1, ExternalId(value = 1).value)
        assertEquals(30, ExternalId.of(value = 30).value)
        assertEquals("30", ExternalId(value = 30).toString())
    }

    @Test
    fun `rejects zero`() {
        val exception = assertFailsWith<SpotException.InvalidExternalId> { ExternalId(value = 0) }

        assertEquals("Spot external id must be greater than 0, got: 0", exception.message)
    }

    @Test
    fun `rejects a negative value`() {
        assertFailsWith<SpotException.InvalidExternalId> { ExternalId(value = -1) }
    }
}
