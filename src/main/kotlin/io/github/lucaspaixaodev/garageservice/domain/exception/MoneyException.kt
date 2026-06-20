package io.github.lucaspaixaodev.garageservice.domain.exception

import java.math.BigDecimal

sealed class MoneyException(
    message: String,
) : BaseException(message) {

    class NegativeAmount(
        amount: BigDecimal,
    ) : MoneyException("Money amount must be greater than or equal to 0, got: $amount")
}
