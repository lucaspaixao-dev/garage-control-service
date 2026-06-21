package io.github.lucaspaixaodev.garageservice.application.ticket.usecase

import io.github.lucaspaixaodev.garageservice.application.spot.repository.SpotRepository
import io.github.lucaspaixaodev.garageservice.application.ticket.repository.TicketRepository
import io.github.lucaspaixaodev.garageservice.domain.exception.SpotException
import io.github.lucaspaixaodev.garageservice.domain.exception.TicketException
import io.github.lucaspaixaodev.garageservice.domain.ticket.Ticket
import io.github.lucaspaixaodev.garageservice.domain.ticket.TicketEventType
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
class RegisterVehicleEventUseCase(
    private val ticketRepository: TicketRepository,
    private val spotRepository: SpotRepository
) {

    @Transactional
    fun execute(command: VehicleEventCommand) {
        when (command.type) {
            TicketEventType.ENTRY -> handleEntry(command = command)
            TicketEventType.PARKED -> handleParked(command = command)
            TicketEventType.EXIT -> handleExit(command = command)
        }
    }

    private fun handleEntry(command: VehicleEventCommand) {
        val ticket =
            Ticket.entry(
                licensePlate = command.licensePlate,
                entryTime = command.entryTime ?: LocalDateTime.now(),
            )
        ticketRepository.save(ticket = ticket)
    }

    private fun handleParked(command: VehicleEventCommand) {
        val latitude = command.latitude
        val longitude = command.longitude
        require(latitude != null && longitude != null) {
            "PARKED event for '${command.licensePlate}' must carry coordinates"
        }

        val ticket = openTicketFor(licensePlate = command.licensePlate)
        val spot =
            spotRepository.findByCoordinates(latitude = latitude, longitude = longitude)
                ?: throw SpotException.NotFoundForCoordinates(latitude = latitude, longitude = longitude)

        spot.occupied()
        spotRepository.save(spot = spot)

        ticket.park(spotId = spot.id)
        ticketRepository.save(ticket = ticket)
    }

    private fun handleExit(command: VehicleEventCommand) {
        val ticket = openTicketFor(licensePlate = command.licensePlate)

        ticket.spotId?.let { spotId ->
            val spot =
                spotRepository.findById(id = spotId)
                    ?: throw SpotException.NotFound(id = spotId.toString())
            spot.free()
            spotRepository.save(spot = spot)
        }

        ticket.exit(exitTime = command.exitTime ?: LocalDateTime.now())
        ticketRepository.save(ticket = ticket)
    }

    private fun openTicketFor(licensePlate: String): Ticket =
        ticketRepository.findOpenByLicensePlate(licensePlate = licensePlate)
            ?: throw TicketException.OpenTicketNotFound(licensePlate = licensePlate)
}

data class VehicleEventCommand(
    val type: TicketEventType,
    val licensePlate: String,
    val entryTime: LocalDateTime? = null,
    val exitTime: LocalDateTime? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
)
