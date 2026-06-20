package io.github.lucaspaixaodev.garageservice.domain

import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertFailsWith

class IdTest {

    @Test
    fun `generate produces unique non-null ids`() {
        val first = Id.generate()
        val second = Id.generate()

        assertNotNull(first.value)
        assertNotEquals(first, second)
    }

    @Test
    fun `of parses a uuid string and round-trips`() {
        val uuid = "cb745afa-aa13-47c5-ac27-347dc169e156"
        val id = Id.of(value = uuid)

        assertEquals(UUID.fromString(uuid), id.value)
        assertEquals(uuid, id.toString())
    }

    @Test
    fun `of rejects an invalid uuid`() {
        assertFailsWith<IllegalArgumentException> { Id.of(value = "not-a-uuid") }
    }
}
