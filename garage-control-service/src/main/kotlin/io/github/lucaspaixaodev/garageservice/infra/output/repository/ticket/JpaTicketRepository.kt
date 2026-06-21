package io.github.lucaspaixaodev.garageservice.infra.output.repository.ticket

import io.github.lucaspaixaodev.garageservice.application.ticket.repository.TicketRepository
import io.github.lucaspaixaodev.garageservice.domain.garage.valueobject.GarageSector
import io.github.lucaspaixaodev.garageservice.domain.garage.valueobject.Money
import io.github.lucaspaixaodev.garageservice.domain.ticket.Ticket
import io.github.lucaspaixaodev.garageservice.domain.ticket.valueobject.TicketCharge
import io.github.lucaspaixaodev.garageservice.domain.ticket.valueobject.TicketEvent
import io.github.lucaspaixaodev.garageservice.domain.ticket.valueobject.TicketEventTime
import io.github.lucaspaixaodev.garageservice.domain.ticket.valueobject.TicketEventType
import io.github.lucaspaixaodev.garageservice.domain.ticket.valueobject.TicketStatus
import java.time.LocalDate
import java.util.UUID
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

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

    override fun totalRevenue(sector: GarageSector, date: LocalDate): Money {
        val total =
            ticketEntityRepository.sumFareBySectorAndPaidAtBetween(
                sector = sector,
                start = date.atStartOfDay(),
                end = date.plusDays(1).atStartOfDay(),
            )
        return Money.of(total)
    }

    private fun Ticket.toEntity(): TicketEntity {
        val exitTime = events.firstOrNull { it.type == TicketEventType.EXIT }?.time?.value
        return TicketEntity(
            id = id.value,
            licensePlate = vehicle.licensePlate,
            spotId = spotId?.value,
            status = status,
            sector = charge?.sector,
            hourlyPrice = charge?.hourlyPrice?.amount,
            fare = charge?.fare?.amount,
            paidAt = charge?.fare?.let { exitTime },
        )
    }

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
            events = events,
            charge = toCharge(),
        )

    private fun TicketEntity.toCharge(): TicketCharge? {
        val sector = this.sector ?: return null
        val hourlyPrice = this.hourlyPrice ?: return null
        return TicketCharge(
            sector = sector,
            hourlyPrice = Money.of(hourlyPrice),
            fare = fare?.let { Money.of(it) },
        )
    }
}
