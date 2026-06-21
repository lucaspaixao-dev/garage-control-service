package io.github.lucaspaixaodev.garageservice.domain.exception

sealed class TicketException(
    message: String,
) : BaseException(message) {

    class OpenTicketNotFound(
        licensePlate: String,
    ) : TicketException("No open ticket found for vehicle '$licensePlate'")
}
