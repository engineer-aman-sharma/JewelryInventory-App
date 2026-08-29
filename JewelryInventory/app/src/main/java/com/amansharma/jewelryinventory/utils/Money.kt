package com.amansharma.jewelryinventory.utils

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

object Money {
    private val usd: NumberFormat = NumberFormat.getCurrencyInstance(Locale.US)

    fun round(amount: Double): Double =
        BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP).toDouble()

    fun format(amount: Double): String = usd.format(round(amount))
}
