package io.github.lucaspaixaodev.garageservice.infra.output.repository.garage

import io.github.lucaspaixaodev.garageservice.domain.garage.valueobject.GarageSector
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface GarageEntityRepository : JpaRepository<GarageEntity, UUID> {

    fun findAllBySectorIn(sectors: Collection<GarageSector>): List<GarageEntity>
}
