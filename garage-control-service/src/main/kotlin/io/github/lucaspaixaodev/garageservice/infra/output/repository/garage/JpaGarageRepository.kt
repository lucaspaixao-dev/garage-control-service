package io.github.lucaspaixaodev.garageservice.infra.output.repository.garage

import io.github.lucaspaixaodev.garageservice.application.garage.repository.GarageRepository
import io.github.lucaspaixaodev.garageservice.domain.garage.valueobject.GarageSector
import io.github.lucaspaixaodev.garageservice.domain.garage.Garage
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class JpaGarageRepository(
    private val garageEntityRepository: GarageEntityRepository
) : GarageRepository {

    private companion object {
        private val logger = LoggerFactory.getLogger(JpaGarageRepository::class.java)
    }

    override fun saveAll(garages: List<Garage>): Map<GarageSector, Garage> {
        logger.info("Upserting ${garages.size} garages by sector")

        val existingIdBySector = garageEntityRepository
            .findAllBySectorIn(garages.map { it.sector })
            .associate { it.sector to it.id }

        val persistedBySector =
            garages.associate { garage ->
                val persisted = existingIdBySector[garage.sector]?.let { garage.reIdentified(it) } ?: garage
                garageEntityRepository.save(persisted.toEntity())
                persisted.sector to persisted
            }

        logger.info("Upserted garages sectors=${persistedBySector.keys.joinToString { it.name }}")
        return persistedBySector
    }

    private fun Garage.reIdentified(id: UUID): Garage =
        Garage.restore(
            id = id.toString(),
            sector = sector.name,
            basePrice = basePrice.amount,
            open = openHour.toString(),
            close = closeHour.toString(),
            durationLimit = durationLimit.minutes
        )

    private fun Garage.toEntity(): GarageEntity =
        GarageEntity(
            id = id.value,
            sector = sector,
            basePrice = basePrice.amount,
            openHour = openHour.value,
            closeHour = closeHour.value,
            durationLimitMinutes = durationLimit.minutes
        )
}
