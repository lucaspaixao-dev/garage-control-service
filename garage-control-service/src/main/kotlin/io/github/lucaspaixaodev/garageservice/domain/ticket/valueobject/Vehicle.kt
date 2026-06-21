package io.github.lucaspaixaodev.garageservice.domain.ticket.valueobject

import io.github.lucaspaixaodev.garageservice.domain.exception.TicketException

data class Vehicle(
    val licensePlate: String,
) {

    init {
        if (licensePlate.isBlank()) throw TicketException.InvalidLicensePlate()
    }
}
