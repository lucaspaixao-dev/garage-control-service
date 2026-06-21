package io.github.lucaspaixaodev.garageservice

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

// Keep the SQS auto-config (SqsTemplate is needed by DashboardEventPublisher) but don't
// start the listener container, so the context loads without talking to LocalStack.
@SpringBootTest(properties = ["spring.cloud.aws.sqs.listener.auto-startup=false"])
class GarageServiceApplicationTests {

    @Test
    fun contextLoads() {
    }

}
