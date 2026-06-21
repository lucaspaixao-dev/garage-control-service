package io.github.lucaspaixaodev.garageservice.application.spot.repository

import io.github.lucaspaixaodev.garageservice.domain.Id
import io.github.lucaspaixaodev.garageservice.domain.spot.Spot

interface SpotRepository {

    fun saveAll(spots: List<Spot>)

    fun save(spot: Spot): Spot

    fun findById(id: Id): Spot?

    fun findByCoordinates(latitude: Double, longitude: Double): Spot?
}
