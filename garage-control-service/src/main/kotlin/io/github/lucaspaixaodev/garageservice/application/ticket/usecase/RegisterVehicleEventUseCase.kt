package io.github.lucaspaixaodev.garageservice.application.ticket.usecase

import io.github.lucaspaixaodev.garageservice.application.garage.repository.GarageRepository
import io.github.lucaspaixaodev.garageservice.application.spot.repository.SpotRepository
import io.github.lucaspaixaodev.garageservice.application.ticket.repository.ProcessedEventRepository
import io.github.lucaspaixaodev.garageservice.application.ticket.repository.TicketRepository
import io.github.lucaspaixaodev.garageservice.domain.garage.valueobject.OccupancyRate
import io.github.lucaspaixaodev.garageservice.domain.ticket.Ticket
import io.github.lucaspaixaodev.garageservice.domain.ticket.valueobject.TicketEventType
import java.time.LocalDateTime
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class RegisterVehicleEventUseCase(
    private val ticketRepository: TicketRepository,
    private val spotRepository: SpotRepository,
    private val garageRepository: GarageRepository,
    private val processedEventRepository: ProcessedEventRepository,
) {

    private companion object {
        private val logger = LoggerFactory.getLogger(RegisterVehicleEventUseCase::class.java)
    }

    @Transactional
    fun execute(command: VehicleEventCommand) {
        if (!processedEventRepository.register(eventId = command.eventId)) {
            logger.info("Skipping duplicate vehicle event id=${command.eventId} plate=${command.licensePlate}")
            return
        }

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

        val ticket = ticketRepository.findOpenByLicensePlate(licensePlate = command.licensePlate)
        if (ticket == null) {
            logger.warn("Skipping PARKED for '${command.licensePlate}': no open ticket")
            return
        }

        val spot = spotRepository.findByCoordinates(latitude = latitude, longitude = longitude)
        if (spot == null) {
            logger.warn("Skipping PARKED for '${command.licensePlate}': no spot at ($latitude, $longitude)")
            return
        }
        if (spot.occupied) {
            logger.warn("Skipping PARKED for '${command.licensePlate}': spot ${spot.id} already occupied")
            return
        }

        val garage = garageRepository.findById(id = spot.garageId)
        if (garage == null) {
            logger.warn("Skipping PARKED for '${command.licensePlate}': garage ${spot.garageId} not found")
            return
        }

        val hourlyPrice = garage.hourlyPriceAt(occupancy = occupancyAfterParking())

        spot.occupied()
        spotRepository.save(spot = spot)

        ticket.park(spotId = spot.id, sector = garage.sector, hourlyPrice = hourlyPrice)
        ticketRepository.save(ticket = ticket)
    }

    private fun handleExit(command: VehicleEventCommand) {
        val ticket = ticketRepository.findOpenByLicensePlate(licensePlate = command.licensePlate)
        if (ticket == null) {
            logger.warn("Skipping EXIT for '${command.licensePlate}': no open ticket")
            return
        }

        ticket.spotId?.let { spotId ->
            spotRepository.findById(id = spotId)?.let { spot ->
                spot.free()
                spotRepository.save(spot = spot)
            }
        }

        ticket.exit(exitTime = command.exitTime ?: LocalDateTime.now())
        ticketRepository.save(ticket = ticket)
    }

    /** Overall occupancy including the vehicle that is parking right now. */
    private fun occupancyAfterParking(): OccupancyRate =
        OccupancyRate.of(
            occupied = spotRepository.countOccupied() + 1,
            total = spotRepository.countTotal(),
        )
}

data class VehicleEventCommand(
    val eventId: String,
    val type: TicketEventType,
    val licensePlate: String,
    val entryTime: LocalDateTime? = null,
    val exitTime: LocalDateTime? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
)
