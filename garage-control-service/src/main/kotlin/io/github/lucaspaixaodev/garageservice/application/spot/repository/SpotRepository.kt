package io.github.lucaspaixaodev.garageservice.application.spot.repository

import io.github.lucaspaixaodev.garageservice.domain.spot.Spot

interface SpotRepository {

    fun saveAll(spots: List<Spot>)
}
