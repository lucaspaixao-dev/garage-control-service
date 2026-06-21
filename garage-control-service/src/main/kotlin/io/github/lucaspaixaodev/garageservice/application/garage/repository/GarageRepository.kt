package io.github.lucaspaixaodev.garageservice.application.garage.repository

import io.github.lucaspaixaodev.garageservice.domain.Id
import io.github.lucaspaixaodev.garageservice.domain.garage.Garage
import io.github.lucaspaixaodev.garageservice.domain.garage.valueobject.GarageSector

interface GarageRepository {

    fun saveAll(garages: List<Garage>): Map<GarageSector, Garage>

    fun findById(id: Id): Garage?
}
