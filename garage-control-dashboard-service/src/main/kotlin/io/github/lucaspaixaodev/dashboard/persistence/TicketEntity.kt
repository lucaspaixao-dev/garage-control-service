package io.github.lucaspaixaodev.dashboard.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "ticket")
class TicketEntity(

    @Id
    @Column(name = "id")
    val id: UUID,

    @Column(name = "license_plate")
    val licensePlate: String,

    @Column(name = "sector")
    val sector: String?,

    @Column(name = "status")
    val status: String,

    @Column(name = "hourly_price")
    val hourlyPrice: BigDecimal?,

    @Column(name = "fare")
    val fare: BigDecimal?,

    @Column(name = "paid_at")
    val paidAt: LocalDateTime?
)

interface TicketRepository : JpaRepository<TicketEntity, UUID> {

    @Query(
        nativeQuery = true,
        value = """
        SELECT t.* FROM ticket t
        LEFT JOIN (
            SELECT ticket_id, MAX(event_time) AS last_event
            FROM ticket_event
            GROUP BY ticket_id
        ) le ON le.ticket_id = t.id
        ORDER BY COALESCE(le.last_event, TIMESTAMP 'epoch') DESC
        LIMIT :limit
        """,
    )
    fun findRecent(@Param("limit") limit: Int): List<TicketEntity>

    @Query(
        """
        SELECT COALESCE(SUM(t.fare), 0)
        FROM TicketEntity t
        WHERE t.sector = :sector
          AND t.paidAt >= :start
          AND t.paidAt < :end
        """,
    )
    fun sumFareBySectorAndPaidAtBetween(
        @Param("sector") sector: String,
        @Param("start") start: LocalDateTime,
        @Param("end") end: LocalDateTime,
    ): BigDecimal
}
