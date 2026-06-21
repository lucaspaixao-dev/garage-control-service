package io.github.lucaspaixaodev.garageservice.domain.ticket

import io.github.lucaspaixaodev.garageservice.domain.Id
import java.time.LocalDateTime

class Ticket private constructor(
    val id: Id,
    val vehicle: Vehicle,
    var spotId: Id?,
    private var _status: TicketStatus,
    private var _events: MutableList<TicketEvent>,
) {

    val status: TicketStatus
        get() = _status

    val events: List<TicketEvent>
        get() = _events

    fun park(spotId: Id) {
        this.spotId = spotId
        addEvent(type = TicketEventType.PARKED)
    }

    fun exit(exitTime: LocalDateTime) {
        addEvent(type = TicketEventType.EXIT, time = exitTime)
        _status = TicketStatus.CLOSED
    }

    fun getFare() {
        // TODO: compute fare from the garage base price and the parked duration.
    }

    private fun addEvent(
        type: TicketEventType,
        time: LocalDateTime? = null,
    ) {
        _events.add(TicketEvent(type = type, time = time?.let { TicketEventTime(it) }))
    }

    companion object Factory {
        fun entry(
            licensePlate: String,
            entryTime: LocalDateTime,
        ): Ticket {
            val ticket =
                Ticket(
                    id = Id.generate(),
                    vehicle = Vehicle(licensePlate = licensePlate),
                    spotId = null,
                    _status = TicketStatus.OPEN,
                    _events = mutableListOf(),
                )
            ticket.addEvent(type = TicketEventType.ENTRY, time = entryTime)
            return ticket
        }

        fun restore(
            id: String,
            licensePlate: String,
            spotId: String?,
            status: TicketStatus,
            events: List<TicketEvent>,
        ): Ticket =
            Ticket(
                id = Id.of(id),
                vehicle = Vehicle(licensePlate = licensePlate),
                spotId = spotId?.let { Id.of(it) },
                _status = status,
                _events = events.toMutableList(),
            )
    }
}

data class Vehicle(
    val licensePlate: String,
)

data class TicketEvent(
    val type: TicketEventType,
    val time: TicketEventTime? = null,
)

@JvmInline
value class TicketEventTime(val value: LocalDateTime)

enum class TicketEventType {
    ENTRY,
    PARKED,
    EXIT,
}

enum class TicketStatus {
    OPEN,
    CLOSED,
}
