package io.github.lucaspaixaodev.garageservice.domain.ticket.valueobject

import io.github.lucaspaixaodev.garageservice.domain.exception.TicketException
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TicketEventTypeTest {

    @Test
    fun `of parses a known type case-insensitively and trimmed`() {
        assertEquals(TicketEventType.PARKED, TicketEventType.of("parked"))
        assertEquals(TicketEventType.ENTRY, TicketEventType.of(" ENTRY "))
    }

    @Test
    fun `of rejects an unknown type`() {
        val exception = assertThrows<TicketException.InvalidEventType> { TicketEventType.of("CRUISING") }

        assertEquals(
            "Invalid ticket event type 'CRUISING'. Allowed values: ENTRY, PARKED, EXIT",
            exception.message,
        )
    }
}
