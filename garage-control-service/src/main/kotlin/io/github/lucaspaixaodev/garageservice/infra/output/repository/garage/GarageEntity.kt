package io.github.lucaspaixaodev.garageservice.infra.output.repository.garage

import io.github.lucaspaixaodev.garageservice.domain.garage.valueobject.GarageSector
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalTime
import java.util.UUID

@Entity
@Table(name = "garage")
class GarageEntity(

    @Id
    @Column(name = "id", nullable = false)
    val id: UUID,

    @Column(name = "sector", nullable = false)
    @Enumerated(EnumType.STRING)
    val sector: GarageSector,

    @Column(name = "base_price", nullable = false, precision = 19, scale = 2)
    val basePrice: BigDecimal,

    @Column(name = "open_hour", nullable = false)
    val openHour: LocalTime,

    @Column(name = "close_hour", nullable = false)
    val closeHour: LocalTime,

    @Column(name = "duration_limit_minutes", nullable = false)
    val durationLimitMinutes: Int
)
