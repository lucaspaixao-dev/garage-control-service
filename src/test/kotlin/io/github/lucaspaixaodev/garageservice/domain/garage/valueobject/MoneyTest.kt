package io.github.lucaspaixaodev.garageservice.domain.garage.valueobject

import io.github.lucaspaixaodev.garageservice.domain.exception.MoneyException
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MoneyTest {

    @Test
    fun `of scales the amount to two decimals`() {
        val money = Money.of(amount = BigDecimal("40.5"))

        assertEquals("40.50", money.amount.toPlainString())
        assertEquals("40.50", money.toString())
    }

    @Test
    fun `of accepts a string amount`() {
        assertEquals("4.10", Money.of(amount = "4.1").toString())
    }

    @Test
    fun `ZERO is zero with scale two`() {
        assertEquals("0.00", Money.ZERO.toString())
    }

    @Test
    fun `of rejects a negative amount`() {
        val exception =
            assertFailsWith<MoneyException.NegativeAmount> {
                Money.of(amount = BigDecimal("-0.01"))
            }

        assertEquals("Money amount must be greater than or equal to 0, got: -0.01", exception.message)
    }
}
