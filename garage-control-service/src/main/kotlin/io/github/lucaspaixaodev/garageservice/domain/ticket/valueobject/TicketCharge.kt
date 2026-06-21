package io.github.lucaspaixaodev.garageservice.domain.ticket.valueobject

import io.github.lucaspaixaodev.garageservice.domain.garage.valueobject.GarageSector
import io.github.lucaspaixaodev.garageservice.domain.garage.valueobject.Money

/** Pricing context captured at PARKED and settled with the [fare] at EXIT. */
data class TicketCharge(
    val sector: GarageSector,
    val hourlyPrice: Money,
    val fare: Money? = null,
) {

    fun settled(fare: Money): TicketCharge = copy(fare = fare)
}
