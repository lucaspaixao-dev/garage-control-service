package io.github.lucaspaixaodev.garageservice.infra.output.repository.event

import io.github.lucaspaixaodev.garageservice.application.ticket.repository.ProcessedEventRepository
import java.time.LocalDateTime
import org.springframework.stereotype.Repository

@Repository
class JpaProcessedEventRepository(
    private val processedEventEntityRepository: ProcessedEventEntityRepository
) : ProcessedEventRepository {

    override fun register(eventId: String): Boolean {
        if (processedEventEntityRepository.existsById(eventId)) {
            return false
        }
        processedEventEntityRepository.save(
            ProcessedEventEntity(eventId = eventId, processedAt = LocalDateTime.now()),
        )
        return true
    }
}
