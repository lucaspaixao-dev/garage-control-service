package io.github.lucaspaixaodev.garageservice.infra.output.repository.ticket

import io.github.lucaspaixaodev.garageservice.application.ticket.repository.TicketRepository
import io.github.lucaspaixaodev.garageservice.domain.ticket.Ticket
import io.github.lucaspaixaodev.garageservice.domain.ticket.TicketEvent
import io.github.lucaspaixaodev.garageservice.domain.ticket.TicketEventTime
import io.github.lucaspaixaodev.garageservice.domain.ticket.TicketStatus
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class JpaTicketRepository(
    private val ticketEntityRepository: TicketEntityRepository,
    private val ticketEventEntityRepository: TicketEventEntityRepository
) : TicketRepository {

    private companion object {
        private val logger = LoggerFactory.getLogger(JpaTicketRepository::class.java)
    }

    override fun save(ticket: Ticket): Ticket {
        logger.info("Saving ticket id=${ticket.id} plate=${ticket.vehicle.licensePlate} status=${ticket.status}")

        ticketEntityRepository.save(ticket.toEntity())
        // The aggregate owns its events: replace them wholesale on each save.
        ticketEventEntityRepository.deleteAllByIdTicketId(ticket.id.value)
        ticketEventEntityRepository.saveAll(ticket.events.map { it.toEntity(ticket.id.value) })

        return ticket
    }

    override fun findOpenByLicensePlate(licensePlate: String): Ticket? {
        val entity =
            ticketEntityRepository.findFirstByLicensePlateAndStatus(licensePlate, TicketStatus.OPEN)
                ?: return null
        val events = ticketEventEntityRepository.findAllByIdTicketId(entity.id).map { it.toEvent() }
        return entity.toDomain(events)
    }

    private fun Ticket.toEntity(): TicketEntity =
        TicketEntity(
            id = id.value,
            licensePlate = vehicle.licensePlate,
            spotId = spotId?.value,
            status = status
        )

    private fun TicketEvent.toEntity(ticketId: UUID): TicketEventEntity =
        TicketEventEntity(
            id = TicketEventId(ticketId = ticketId, type = type),
            eventTime = time?.value
        )

    private fun TicketEventEntity.toEvent(): TicketEvent =
        TicketEvent(type = id.type, time = eventTime?.let { TicketEventTime(it) })

    private fun TicketEntity.toDomain(events: List<TicketEvent>): Ticket =
        Ticket.restore(
            id = id.toString(),
            licensePlate = licensePlate,
            spotId = spotId?.toString(),
            status = status,
            events = events
        )
}
