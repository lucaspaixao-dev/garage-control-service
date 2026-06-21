package io.github.lucaspaixaodev.garageservice.infra.output.repository.event

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test

class JpaProcessedEventRepositoryTest {

    private val entityRepository = mockk<ProcessedEventEntityRepository>(relaxed = true)
    private val repository = JpaProcessedEventRepository(processedEventEntityRepository = entityRepository)

    @Test
    fun `register stores and returns true the first time an event is seen`() {
        every { entityRepository.existsById("evt-1") } returns false
        val saved = slot<ProcessedEventEntity>()
        every { entityRepository.save(capture(saved)) } answers { saved.captured }

        val firstTime = repository.register(eventId = "evt-1")

        assertTrue(firstTime)
        assertEquals("evt-1", saved.captured.eventId)
    }

    @Test
    fun `register returns false and stores nothing for an already-processed event`() {
        every { entityRepository.existsById("evt-1") } returns true

        val firstTime = repository.register(eventId = "evt-1")

        assertFalse(firstTime)
        verify(exactly = 0) { entityRepository.save(any()) }
    }
}
