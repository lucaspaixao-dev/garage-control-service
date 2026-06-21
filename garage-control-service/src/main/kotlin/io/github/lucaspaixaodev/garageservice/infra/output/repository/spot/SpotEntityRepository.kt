package io.github.lucaspaixaodev.garageservice.infra.output.repository.spot

import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository

interface SpotEntityRepository : JpaRepository<SpotEntity, UUID> {

    fun findAllByExternalIdIn(externalIds: Collection<Int>): List<SpotEntity>

    fun findByLatitudeAndLongitude(latitude: Double, longitude: Double): SpotEntity?

    fun countByOccupiedTrue(): Long
}
