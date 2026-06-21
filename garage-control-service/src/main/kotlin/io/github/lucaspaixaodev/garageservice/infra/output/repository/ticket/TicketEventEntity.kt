package io.github.lucaspaixaodev.garageservice.infra.output.repository.ticket

import io.github.lucaspaixaodev.garageservice.domain.ticket.TicketEventType
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.EnumType
import jakarta.persistence.Entity
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import java.io.Serializable
import java.time.LocalDateTime
import java.util.UUID

@Embeddable
data class TicketEventId(

    @Column(name = "ticket_id", nullable = false)
    val ticketId: UUID,

    @Column(name = "type", nullable = false, length = 16)
    @Enumerated(EnumType.STRING)
    val type: TicketEventType
) : Serializable

@Entity
@Table(name = "ticket_event")
class TicketEventEntity(

    @EmbeddedId
    val id: TicketEventId,

    @Column(name = "event_time")
    val eventTime: LocalDateTime?
)
