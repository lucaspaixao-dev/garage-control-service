package io.github.lucaspaixaodev.garageservice.infra.output.repository.spot

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface SpotEntityRepository : JpaRepository<SpotEntity, UUID> {

    fun findAllByExternalIdIn(externalIds: Collection<Int>): List<SpotEntity>
}
