package io.github.lucaspaixaodev.dashboard.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

@Entity
@Table(name = "spot")
class SpotEntity(

    @Id
    @Column(name = "id")
    val id: UUID,

    @Column(name = "external_id")
    val externalId: Int,

    @Column(name = "garage_id")
    val garageId: UUID,

    @Column(name = "latitude")
    val latitude: Double,

    @Column(name = "longitude")
    val longitude: Double,

    @Column(name = "occupied")
    val occupied: Boolean
)

interface SpotRepository : JpaRepository<SpotEntity, UUID>
