package io.github.lucaspaixaodev.garageservice.application.ticket.repository

import io.github.lucaspaixaodev.garageservice.domain.garage.valueobject.GarageSector
import io.github.lucaspaixaodev.garageservice.domain.garage.valueobject.Money
import io.github.lucaspaixaodev.garageservice.domain.ticket.Ticket
import java.time.LocalDate

interface TicketRepository {

    fun save(ticket: Ticket): Ticket

    fun findOpenByLicensePlate(licensePlate: String): Ticket?

    /** Total fare collected for the sector on the given (exit) date. */
    fun totalRevenue(sector: GarageSector, date: LocalDate): Money
}
