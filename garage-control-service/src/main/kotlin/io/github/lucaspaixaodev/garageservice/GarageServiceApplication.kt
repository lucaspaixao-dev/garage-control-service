package io.github.lucaspaixaodev.garageservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class GarageServiceApplication

fun main(args: Array<String>) {
    runApplication<GarageServiceApplication>(*args)
}
