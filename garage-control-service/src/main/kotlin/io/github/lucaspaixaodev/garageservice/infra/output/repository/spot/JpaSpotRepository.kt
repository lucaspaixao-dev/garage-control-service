package io.github.lucaspaixaodev.garageservice.infra.output.repository.spot

import io.github.lucaspaixaodev.garageservice.application.spot.repository.SpotRepository
import io.github.lucaspaixaodev.garageservice.domain.Id
import io.github.lucaspaixaodev.garageservice.domain.spot.Spot
import java.util.UUID
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class JpaSpotRepository(
    private val spotEntityRepository: SpotEntityRepository
) : SpotRepository {

    private companion object {
        private val logger = LoggerFactory.getLogger(JpaSpotRepository::class.java)
    }

    override fun saveAll(spots: List<Spot>) {
        logger.info("Upserting ${spots.size} spots by external id")

        val existingIdByExternalId = spotEntityRepository
            .findAllByExternalIdIn(spots.map { it.externalId.value })
            .associate { it.externalId to it.id }

        val entities = spots.map { spot ->
            val id = existingIdByExternalId[spot.externalId.value] ?: spot.id.value
            spot.toEntity(id)
        }
        spotEntityRepository.saveAll(entities)

        logger.info("Upserted ${entities.size} spots")
    }

    override fun save(spot: Spot): Spot {
        logger.info("Saving spot id=${spot.id} occupied=${spot.occupied}")
        spotEntityRepository.save(spot.toEntity(spot.id.value))
        return spot
    }

    override fun findById(id: Id): Spot? =
        spotEntityRepository.findById(id.value).orElse(null)?.toDomain()

    override fun findByCoordinates(latitude: Double, longitude: Double): Spot? =
        spotEntityRepository.findByLatitudeAndLongitude(latitude, longitude)?.toDomain()

    override fun countTotal(): Int = spotEntityRepository.count().toInt()

    override fun countOccupied(): Int = spotEntityRepository.countByOccupiedTrue().toInt()

    private fun SpotEntity.toDomain(): Spot =
        Spot.restore(
            id = id.toString(),
            externalId = externalId,
            garageId = garageId.toString(),
            latitude = latitude,
            longitude = longitude,
            occupied = occupied
        )

    private fun Spot.toEntity(id: UUID): SpotEntity =
        SpotEntity(
            id = id,
            externalId = externalId.value,
            garageId = garageId.value,
            latitude = latitude.value,
            longitude = longitude.value,
            occupied = occupied
        )
}
