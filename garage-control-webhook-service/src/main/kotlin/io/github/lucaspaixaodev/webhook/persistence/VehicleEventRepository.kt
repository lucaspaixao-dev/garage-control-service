package io.github.lucaspaixaodev.webhook.persistence

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Repository
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable
import software.amazon.awssdk.enhanced.dynamodb.TableSchema

@Repository
class VehicleEventRepository(
    enhancedClient: DynamoDbEnhancedClient,
    @Value($$"${webhook.dynamodb.table-name}") tableName: String,
) {

    private companion object {
        private val logger = LoggerFactory.getLogger(VehicleEventRepository::class.java)
    }

    private val table: DynamoDbTable<VehicleEventItem> =
        enhancedClient.table(tableName, TableSchema.fromBean(VehicleEventItem::class.java))

    fun save(event: VehicleEventItem) {
        logger.info("Saving vehicle event id=${event.id} plate=${event.licensePlate} type=${event.eventType} to DynamoDB")
        table.putItem(event)
        logger.info("Saved vehicle event id=${event.id} to DynamoDB")
    }
}
