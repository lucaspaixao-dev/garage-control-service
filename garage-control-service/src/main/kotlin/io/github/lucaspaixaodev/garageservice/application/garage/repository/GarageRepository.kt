package io.github.lucaspaixaodev.garageservice.application.garage.repository

import io.github.lucaspaixaodev.garageservice.domain.garage.valueobject.GarageSector
import io.github.lucaspaixaodev.garageservice.domain.garage.Garage

interface GarageRepository {

    /**
     * Persists the given garages (upserting by sector) and returns the persisted
     * garages keyed by sector, each carrying its authoritative id, so spots can be
     * linked to the garage that actually lives in the database.
     */
    fun saveAll(garages: List<Garage>): Map<GarageSector, Garage>
}
