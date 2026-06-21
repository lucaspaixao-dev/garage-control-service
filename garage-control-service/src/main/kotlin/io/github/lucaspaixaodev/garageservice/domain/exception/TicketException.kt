package io.github.lucaspaixaodev.garageservice.domain.exception

sealed class TicketException(
    message: String,
) : BaseException(message) {

    class InvalidLicensePlate : TicketException("Vehicle license plate must not be blank")

    class InvalidEventType(
        value: String,
        allowed: String,
    ) : TicketException("Invalid ticket event type '$value'. Allowed values: $allowed")
}
