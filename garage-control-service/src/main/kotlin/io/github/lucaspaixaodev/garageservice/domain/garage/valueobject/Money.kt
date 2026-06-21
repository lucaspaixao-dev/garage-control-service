package io.github.lucaspaixaodev.garageservice.domain.garage.valueobject

import io.github.lucaspaixaodev.garageservice.domain.exception.MoneyException
import java.math.BigDecimal
import java.math.RoundingMode

@JvmInline
value class Money private constructor(val amount: BigDecimal) {

    init {
        if (amount.signum() < 0) throw MoneyException.NegativeAmount(amount)
    }

    companion object {

        private const val SCALE = 2
        private val ROUNDING = RoundingMode.HALF_EVEN

        val ZERO: Money = of(BigDecimal.ZERO)

        fun of(amount: BigDecimal): Money = Money(amount.setScale(SCALE, ROUNDING))

        fun of(amount: String): Money = of(BigDecimal(amount))
    }

    fun multipliedBy(factor: BigDecimal): Money = of(amount.multiply(factor))

    fun times(quantity: Long): Money = of(amount.multiply(BigDecimal.valueOf(quantity)))

    override fun toString(): String = amount.toPlainString()
}
