package io.github.lucaspaixaodev.garageservice

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(properties = ["spring.cloud.aws.sqs.enabled=false"])
class GarageServiceApplicationTests {

    @Test
    fun contextLoads() {
    }

}
