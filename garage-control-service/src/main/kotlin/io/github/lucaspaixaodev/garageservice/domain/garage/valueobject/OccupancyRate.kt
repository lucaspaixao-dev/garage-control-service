package io.github.lucaspaixaodev.garageservice.domain.garage.valueobject

import java.math.BigDecimal

/**
 * Overall garage occupancy (occupied spots / total spots) used to derive the
 * dynamic-pricing multiplier applied to the base price when a vehicle parks.
 */
@JvmInline
value class OccupancyRate private constructor(val value: Double) {

    /**
     * Dynamic-pricing multiplier:
     * - below 25% occupancy → 10% discount
     * - up to 50%           → base price
     * - up to 75%           → 10% surcharge
     * - up to 100%          → 25% surcharge
     */
    fun priceMultiplier(): BigDecimal =
        when {
            value < LOW -> DISCOUNT
            value <= MID -> BASE
            value <= HIGH -> SURGE
            else -> PEAK
        }

    val isFull: Boolean
        get() = value >= 1.0

    companion object {

        private const val LOW = 0.25
        private const val MID = 0.50
        private const val HIGH = 0.75

        private val DISCOUNT = BigDecimal("0.90")
        private val BASE = BigDecimal("1.00")
        private val SURGE = BigDecimal("1.10")
        private val PEAK = BigDecimal("1.25")

        fun of(occupied: Int, total: Int): OccupancyRate =
            OccupancyRate(if (total <= 0) 0.0 else occupied.toDouble() / total.toDouble())
    }
}
