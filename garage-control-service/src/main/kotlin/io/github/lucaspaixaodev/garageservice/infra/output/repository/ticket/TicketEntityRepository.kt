package io.github.lucaspaixaodev.garageservice.infra.output.repository.ticket

import io.github.lucaspaixaodev.garageservice.domain.ticket.TicketStatus
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface TicketEntityRepository : JpaRepository<TicketEntity, UUID> {

    fun findFirstByLicensePlateAndStatus(licensePlate: String, status: TicketStatus): TicketEntity?
}
