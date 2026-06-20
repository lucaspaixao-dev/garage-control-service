package io.github.lucaspaixaodev.garageservice.infra.output.repository.spot

import io.github.lucaspaixaodev.garageservice.application.spot.repository.SpotRepository
import io.github.lucaspaixaodev.garageservice.domain.spot.Spot
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import java.util.UUID

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
