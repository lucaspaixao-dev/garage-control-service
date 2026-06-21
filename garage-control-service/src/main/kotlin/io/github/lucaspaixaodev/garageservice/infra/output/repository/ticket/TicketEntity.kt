package io.github.lucaspaixaodev.garageservice.infra.output.repository.ticket

import io.github.lucaspaixaodev.garageservice.domain.garage.valueobject.GarageSector
import io.github.lucaspaixaodev.garageservice.domain.ticket.valueobject.TicketStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "ticket")
class TicketEntity(

    @Id
    @Column(name = "id", nullable = false)
    val id: UUID,

    @Column(name = "license_plate", nullable = false, length = 16)
    val licensePlate: String,

    @Column(name = "spot_id")
    val spotId: UUID?,

    @Column(name = "status", nullable = false, length = 16)
    @Enumerated(EnumType.STRING)
    val status: TicketStatus,

    @Column(name = "sector")
    @Enumerated(EnumType.STRING)
    val sector: GarageSector? = null,

    @Column(name = "hourly_price", precision = 19, scale = 2)
    val hourlyPrice: BigDecimal? = null,

    @Column(name = "fare", precision = 19, scale = 2)
    val fare: BigDecimal? = null,

    @Column(name = "paid_at")
    val paidAt: LocalDateTime? = null
)
