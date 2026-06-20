package io.github.lucaspaixaodev.webhook.persistence

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey

@DynamoDbBean
class VehicleEventItem {

    @get:DynamoDbPartitionKey
    var id: String = ""

    var licensePlate: String = ""

    var eventType: String = ""

    var entryTime: String? = null

    var exitTime: String? = null

    var lat: Double? = null

    var lng: Double? = null

    var receivedAt: String = ""
}
