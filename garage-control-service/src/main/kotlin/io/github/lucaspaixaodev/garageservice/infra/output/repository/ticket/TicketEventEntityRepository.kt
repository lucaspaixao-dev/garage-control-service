package io.github.lucaspaixaodev.garageservice.infra.output.repository.ticket

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface TicketEventEntityRepository : JpaRepository<TicketEventEntity, TicketEventId> {

    fun findAllByIdTicketId(ticketId: UUID): List<TicketEventEntity>

    fun deleteAllByIdTicketId(ticketId: UUID)
}
