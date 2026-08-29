package com.amansharma.jewelryinventory.utils

import com.amansharma.jewelryinventory.data.local.entity.InventoryItemEntity
import com.amansharma.jewelryinventory.data.model.JewelryCategory
import com.amansharma.jewelryinventory.data.model.PaymentMethod

data class CheckoutLine(
    val item: InventoryItemEntity,
    val quantity: Int
)

data class PricedCheckoutLine(
    val item: InventoryItemEntity,
    val quantity: Int,
    val unitPrice: Double,
    val discountAmount: Double,
    val lineTotal: Double
)

data class CheckoutTotals(
    val lines: List<PricedCheckoutLine>,
    val subtotal: Double,
    val discountTotal: Double,
    val grandTotal: Double,
    val paymentMethod: PaymentMethod
)

object CheckoutCalculator {
    const val WATCH_ZELLE_DISCOUNT = 0.20
    const val JEWELRY_ZELLE_DISCOUNT = 0.05

    fun discountRate(category: JewelryCategory, paymentMethod: PaymentMethod): Double {
        if (paymentMethod != PaymentMethod.ZELLE_WIRE) return 0.0
        return if (category == JewelryCategory.WATCH) {
            WATCH_ZELLE_DISCOUNT
        } else {
            JEWELRY_ZELLE_DISCOUNT
        }
    }

    fun price(lines: List<CheckoutLine>, paymentMethod: PaymentMethod): CheckoutTotals {
        val priced = lines.map { line ->
            val rate = discountRate(line.item.category, paymentMethod)
            val lineSubtotal = Money.round(line.item.retailPriceUsd * line.quantity)
            val discount = Money.round(lineSubtotal * rate)
            PricedCheckoutLine(
                item = line.item,
                quantity = line.quantity,
                unitPrice = line.item.retailPriceUsd,
                discountAmount = discount,
                lineTotal = Money.round(lineSubtotal - discount)
            )
        }
        return CheckoutTotals(
            lines = priced,
            subtotal = Money.round(priced.sumOf { it.unitPrice * it.quantity }),
            discountTotal = Money.round(priced.sumOf { it.discountAmount }),
            grandTotal = Money.round(priced.sumOf { it.lineTotal }),
            paymentMethod = paymentMethod
        )
    }
}
