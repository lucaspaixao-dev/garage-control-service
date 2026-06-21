package io.github.lucaspaixaodev.garageservice.infra.output.repository.ticket

import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository

interface TicketEventEntityRepository : JpaRepository<TicketEventEntity, TicketEventId> {

    fun findAllByIdTicketId(ticketId: UUID): List<TicketEventEntity>

    fun deleteAllByIdTicketId(ticketId: UUID)
}
