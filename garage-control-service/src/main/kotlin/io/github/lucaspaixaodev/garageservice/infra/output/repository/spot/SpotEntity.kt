package io.github.lucaspaixaodev.garageservice.infra.output.repository.spot

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "spot")
class SpotEntity(

    @Id
    @Column(name = "id", nullable = false)
    val id: UUID,

    @Column(name = "external_id", nullable = false, unique = true)
    val externalId: Int,

    @Column(name = "garage_id", nullable = false)
    val garageId: UUID,

    @Column(name = "latitude", nullable = false)
    val latitude: Double,

    @Column(name = "longitude", nullable = false)
    val longitude: Double,

    @Column(name = "occupied", nullable = false)
    val occupied: Boolean
)
