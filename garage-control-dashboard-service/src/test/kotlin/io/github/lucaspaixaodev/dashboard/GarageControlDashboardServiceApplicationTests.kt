package io.github.lucaspaixaodev.dashboard

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

// Keep the SQS auto-config but don't start the listener container, so the context loads
// (and the read-entity mappings are validated against the schema) without LocalStack.
@SpringBootTest(properties = ["spring.cloud.aws.sqs.listener.auto-startup=false"])
class GarageControlDashboardServiceApplicationTests {

    @Test
    fun contextLoads() {
    }
}
