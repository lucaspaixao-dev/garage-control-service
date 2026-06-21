package io.github.lucaspaixaodev.garageservice.application.garage.usecase

import io.github.lucaspaixaodev.garageservice.application.garage.gateway.GarageGateway
import io.github.lucaspaixaodev.garageservice.application.garage.gateway.SpotInfo
import io.github.lucaspaixaodev.garageservice.application.garage.repository.GarageRepository
import io.github.lucaspaixaodev.garageservice.application.spot.repository.SpotRepository
import io.github.lucaspaixaodev.garageservice.domain.exception.SpotException
import io.github.lucaspaixaodev.garageservice.domain.garage.Garage
import io.github.lucaspaixaodev.garageservice.domain.garage.valueobject.GarageSector
import io.github.lucaspaixaodev.garageservice.domain.spot.Spot
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class SetupGarageUseCase(
    private val garageGateway: GarageGateway,
    private val garageRepository: GarageRepository,
    private val spotRepository: SpotRepository
) {

    @Transactional
    fun execute(): SetupGarageResult {
        val data = garageGateway.fetch()

        val garages = data.garages.map { garage ->
            Garage.create(
                sector = garage.sector,
                basePrice = garage.basePrice,
                open = garage.openHour,
                close = garage.closeHour,
                durationLimit = garage.durationLimitMinutes
            )
        }
        val garagesBySector = garageRepository.saveAll(garages)

        val spots = data.spots.map { spot ->
            val garage = garagesBySector[GarageSector.of(spot.sector)]
                ?: throw SpotException.GarageNotFoundForSector(externalId = spot.externalId, sector = spot.sector)
            spot.toSpot(garage)
        }
        spotRepository.saveAll(spots)

        return SetupGarageResult(garages = garages.size, spots = spots.size)
    }

    private fun SpotInfo.toSpot(garage: Garage): Spot =
        if (occupied) {
            Spot.occupied(externalId = externalId, garage = garage, latitude = latitude, longitude = longitude)
        } else {
            Spot.available(externalId = externalId, garage = garage, latitude = latitude, longitude = longitude)
        }
}

data class SetupGarageResult(
    val garages: Int,
    val spots: Int
)
