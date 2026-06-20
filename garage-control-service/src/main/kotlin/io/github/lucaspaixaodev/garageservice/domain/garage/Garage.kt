package io.github.lucaspaixaodev.garageservice.domain.garage

import io.github.lucaspaixaodev.garageservice.domain.garage.valueobject.CloseHour
import io.github.lucaspaixaodev.garageservice.domain.exception.GarageException
import io.github.lucaspaixaodev.garageservice.domain.garage.valueobject.DurationLimit
import io.github.lucaspaixaodev.garageservice.domain.garage.valueobject.GarageSector
import io.github.lucaspaixaodev.garageservice.domain.Id
import io.github.lucaspaixaodev.garageservice.domain.garage.valueobject.Money
import io.github.lucaspaixaodev.garageservice.domain.garage.valueobject.OpenHour
import java.math.BigDecimal

class Garage private constructor(
    val id: Id,
    val sector: GarageSector,
    val basePrice: Money,
    val openHour: OpenHour,
    val closeHour: CloseHour,
    val durationLimit: DurationLimit
) {

    companion object Factory {
        fun create(
            sector: String,
            basePrice: BigDecimal,
            open: String,
            close: String,
            durationLimit: Int
        ): Garage =
            build(
                id = Id.generate(),
                sector = sector,
                basePrice = basePrice,
                open = open,
                close = close,
                durationLimit = durationLimit,
            )

        fun restore(
            id: String,
            sector: String,
            basePrice: BigDecimal,
            open: String,
            close: String,
            durationLimit: Int
        ): Garage =
            build(
                id = Id.of(id),
                sector = sector,
                basePrice = basePrice,
                open = open,
                close = close,
                durationLimit = durationLimit,
            )

        private fun build(
            id: Id,
            sector: String,
            basePrice: BigDecimal,
            open: String,
            close: String,
            durationLimit: Int
        ): Garage {
            val openHour = OpenHour.of(open)
            val closeHour = CloseHour.of(close)

            if (closeHour.isBefore(openHour)) {
                throw GarageException.CloseHourBeforeOpenHour(
                    openHour = openHour.toString(),
                    closeHour = closeHour.toString(),
                )
            }

            return Garage(
                id = id,
                sector = GarageSector.of(sector),
                basePrice = Money.of(basePrice),
                openHour = openHour,
                closeHour = closeHour,
                durationLimit = DurationLimit(durationLimit)
            )
        }
    }
}
