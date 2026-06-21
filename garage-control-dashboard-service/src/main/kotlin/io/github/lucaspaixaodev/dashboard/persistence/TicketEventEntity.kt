package io.github.lucaspaixaodev.dashboard.persistence

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import java.io.Serializable
import java.time.LocalDateTime
import java.util.UUID

@Embeddable
data class TicketEventId(

    @Column(name = "ticket_id")
    val ticketId: UUID,

    @Column(name = "type")
    val type: String
) : Serializable

@Entity
@Table(name = "ticket_event")
class TicketEventEntity(

    @EmbeddedId
    val id: TicketEventId,

    @Column(name = "event_time")
    val eventTime: LocalDateTime?
)

interface TicketEventRepository : JpaRepository<TicketEventEntity, TicketEventId> {

    fun findAllByIdTicketIdIn(ticketIds: Collection<UUID>): List<TicketEventEntity>
}
