package io.github.lucaspaixaodev.garageservice.domain.ticket

import io.github.lucaspaixaodev.garageservice.domain.Id
import io.github.lucaspaixaodev.garageservice.domain.garage.valueobject.GarageSector
import io.github.lucaspaixaodev.garageservice.domain.garage.valueobject.Money
import io.github.lucaspaixaodev.garageservice.domain.ticket.Ticket.Factory.FREE_MINUTES
import io.github.lucaspaixaodev.garageservice.domain.ticket.valueobject.TicketCharge
import io.github.lucaspaixaodev.garageservice.domain.ticket.valueobject.TicketEvent
import io.github.lucaspaixaodev.garageservice.domain.ticket.valueobject.TicketEventTime
import io.github.lucaspaixaodev.garageservice.domain.ticket.valueobject.TicketEventType
import io.github.lucaspaixaodev.garageservice.domain.ticket.valueobject.TicketStatus
import io.github.lucaspaixaodev.garageservice.domain.ticket.valueobject.Vehicle
import java.time.Duration
import java.time.LocalDateTime
import kotlin.math.ceil

class Ticket private constructor(
    val id: Id,
    val vehicle: Vehicle,
    private var _spotId: Id?,
    private var _status: TicketStatus,
    private var _events: MutableList<TicketEvent>,
    private var _charge: TicketCharge?,
) {

    val spotId: Id?
        get() = _spotId

    val status: TicketStatus
        get() = _status

    val events: List<TicketEvent>
        get() = _events

    /** Pricing snapshot taken when the vehicle parked, settled with the fare on exit; null until parked. */
    val charge: TicketCharge?
        get() = _charge

    /**
     * Parks the vehicle on the given spot, capturing the sector and the hourly price
     * in effect at this moment so the fare is computed against the price the driver "saw".
     */
    fun park(
        spotId: Id,
        sector: GarageSector,
        hourlyPrice: Money,
    ) {
        this._spotId = spotId
        this._charge = TicketCharge(sector = sector, hourlyPrice = hourlyPrice)
        addEvent(type = TicketEventType.PARKED)
    }

    fun exit(exitTime: LocalDateTime) {
        addEvent(type = TicketEventType.EXIT, time = exitTime)
        this._charge = _charge?.settled(fare = fareFor(exitTime = exitTime))
        _status = TicketStatus.CLOSED
    }

    /**
     * Fare = charged hours × snapshot hourly price, where the first [FREE_MINUTES] are free
     * and any started hour is billed in full (rounded up). A vehicle that exits without ever
     * parking owes nothing.
     */
    private fun fareFor(exitTime: LocalDateTime): Money {
        val hourlyPrice = _charge?.hourlyPrice ?: return Money.ZERO

        val parkedMinutes = Duration.between(entryTime(), exitTime).toMinutes()
        if (parkedMinutes <= FREE_MINUTES) return Money.ZERO

        val chargedHours = ceil(parkedMinutes.toDouble() / MINUTES_PER_HOUR).toLong()
        return hourlyPrice.times(quantity = chargedHours)
    }

    private fun entryTime(): LocalDateTime =
        _events.first { it.type == TicketEventType.ENTRY }.time?.value
            ?: error("Ticket '$id' has no ENTRY time")

    private fun addEvent(
        type: TicketEventType,
        time: LocalDateTime? = null,
    ) {
        _events.add(TicketEvent(type = type, time = time?.let { TicketEventTime(it) }))
    }

    companion object Factory {

        private const val FREE_MINUTES = 30L
        private const val MINUTES_PER_HOUR = 60.0

        fun entry(
            licensePlate: String,
            entryTime: LocalDateTime,
        ): Ticket {
            val ticket =
                Ticket(
                    id = Id.generate(),
                    vehicle = Vehicle(licensePlate = licensePlate),
                    _spotId = null,
                    _status = TicketStatus.OPEN,
                    _events = mutableListOf(),
                    _charge = null,
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
            charge: TicketCharge?,
        ): Ticket =
            Ticket(
                id = Id.of(id),
                vehicle = Vehicle(licensePlate = licensePlate),
                _spotId = spotId?.let { Id.of(it) },
                _status = status,
                _events = events.toMutableList(),
                _charge = charge,
            )
    }
}
