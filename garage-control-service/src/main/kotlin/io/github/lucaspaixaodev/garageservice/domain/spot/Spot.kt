package io.github.lucaspaixaodev.garageservice.domain.spot

import io.github.lucaspaixaodev.garageservice.domain.Id
import io.github.lucaspaixaodev.garageservice.domain.garage.Garage
import io.github.lucaspaixaodev.garageservice.domain.spot.valueobject.ExternalId
import io.github.lucaspaixaodev.garageservice.domain.spot.valueobject.Latitude
import io.github.lucaspaixaodev.garageservice.domain.spot.valueobject.Longitude

class Spot private constructor(
    val id: Id,
    val externalId: ExternalId,
    val garageId: Id,
    val latitude: Latitude,
    val longitude: Longitude,
    val occupied: Boolean
) {

    companion object Factory {
        fun occupied(
            garage: Garage,
            externalId: Int,
            latitude: Double,
            longitude: Double
        ): Spot =
            build(
                id = Id.generate(),
                externalId = externalId,
                garageId = garage.id,
                latitude = latitude,
                longitude = longitude,
                occupied = true
            )

        fun available(
            garage: Garage,
            externalId: Int,
            latitude: Double,
            longitude: Double
        ): Spot =
            build(
                id = Id.generate(),
                externalId = externalId,
                garageId = garage.id,
                latitude = latitude,
                longitude = longitude,
                occupied = false
            )

        fun restore(
            id: String,
            externalId: Int,
            garageId: String,
            latitude: Double,
            longitude: Double,
            occupied: Boolean
        ): Spot =
            build(
                id = Id.of(id),
                externalId = externalId,
                garageId = Id.of(garageId),
                latitude = latitude,
                longitude = longitude,
                occupied = occupied
            )

        private fun build(
            id: Id,
            externalId: Int,
            garageId: Id,
            latitude: Double,
            longitude: Double,
            occupied: Boolean
        ): Spot = Spot(
            id = id,
            externalId = ExternalId(externalId),
            garageId = garageId,
            latitude = Latitude(latitude),
            longitude = Longitude(longitude),
            occupied = occupied
        )
    }
}
