package io.github.lucaspaixaodev.garageservice.application.ticket.repository

import io.github.lucaspaixaodev.garageservice.domain.ticket.Ticket

interface TicketRepository {

    fun save(ticket: Ticket): Ticket

    fun findOpenByLicensePlate(licensePlate: String): Ticket?
}
