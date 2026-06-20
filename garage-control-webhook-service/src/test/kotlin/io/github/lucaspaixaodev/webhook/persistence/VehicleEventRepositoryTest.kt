package io.github.lucaspaixaodev.webhook.persistence

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable
import software.amazon.awssdk.enhanced.dynamodb.TableSchema

class VehicleEventRepositoryTest {

    private val enhancedClient = mockk<DynamoDbEnhancedClient>()
    private val table = mockk<DynamoDbTable<VehicleEventItem>>(relaxed = true)

    @Test
    fun `save puts the item into the configured table`() {
        every {
            enhancedClient.table(any(), any<TableSchema<VehicleEventItem>>())
        } returns table
        val repository = VehicleEventRepository(enhancedClient = enhancedClient, tableName = "webhook_events")
        val item =
            VehicleEventItem().apply {
                id = "evt-1"
                licensePlate = "ZUL0001"
                eventType = "ENTRY"
                entryTime = "2025-01-01T12:00:00.000Z"
                receivedAt = "2026-06-20T10:00:00Z"
            }

        repository.save(event = item)

        verify(exactly = 1) { table.putItem(item) }
    }
}
