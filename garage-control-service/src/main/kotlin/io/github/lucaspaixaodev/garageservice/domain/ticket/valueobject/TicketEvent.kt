package io.github.lucaspaixaodev.garageservice.domain.ticket.valueobject

data class TicketEvent(
    val type: TicketEventType,
    val time: TicketEventTime? = null,
)
