package io.github.lucaspaixaodev.garageservice.domain.ticket.valueobject

import io.github.lucaspaixaodev.garageservice.domain.exception.TicketException

enum class TicketEventType {
    ENTRY,
    PARKED,
    EXIT;

    companion object {

        fun of(value: String): TicketEventType =
            entries.firstOrNull { it.name.equals(value.trim(), ignoreCase = true) }
                ?: throw TicketException.InvalidEventType(value = value, allowed = entries.joinToString { it.name })
    }
}
