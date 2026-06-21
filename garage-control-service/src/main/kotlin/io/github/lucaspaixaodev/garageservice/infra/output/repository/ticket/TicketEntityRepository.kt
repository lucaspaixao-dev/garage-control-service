package io.github.lucaspaixaodev.garageservice.infra.output.repository.ticket

import io.github.lucaspaixaodev.garageservice.domain.garage.valueobject.GarageSector
import io.github.lucaspaixaodev.garageservice.domain.ticket.valueobject.TicketStatus
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface TicketEntityRepository : JpaRepository<TicketEntity, UUID> {

    fun findFirstByLicensePlateAndStatus(licensePlate: String, status: TicketStatus): TicketEntity?

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
        @Param("sector") sector: GarageSector,
        @Param("start") start: LocalDateTime,
        @Param("end") end: LocalDateTime,
    ): BigDecimal
}
