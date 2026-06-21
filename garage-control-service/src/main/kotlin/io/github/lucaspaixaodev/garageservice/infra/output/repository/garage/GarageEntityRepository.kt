package io.github.lucaspaixaodev.garageservice.infra.output.repository.garage

import io.github.lucaspaixaodev.garageservice.domain.garage.valueobject.GarageSector
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository

interface GarageEntityRepository : JpaRepository<GarageEntity, UUID> {

    fun findAllBySectorIn(sectors: Collection<GarageSector>): List<GarageEntity>
}
