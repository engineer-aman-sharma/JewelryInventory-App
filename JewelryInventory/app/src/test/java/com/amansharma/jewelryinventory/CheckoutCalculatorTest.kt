package com.amansharma.jewelryinventory

import com.amansharma.jewelryinventory.data.local.entity.InventoryItemEntity
import com.amansharma.jewelryinventory.data.model.JewelryCategory
import com.amansharma.jewelryinventory.data.model.MetalType
import com.amansharma.jewelryinventory.data.model.PaymentMethod
import com.amansharma.jewelryinventory.utils.CheckoutCalculator
import com.amansharma.jewelryinventory.utils.CheckoutLine
import com.amansharma.jewelryinventory.utils.InvoiceNumbers
import org.junit.Assert.assertEquals
import org.junit.Test

class CheckoutCalculatorTest {

    @Test
    fun creditCardAppliesNoDiscount() {
        val totals = CheckoutCalculator.price(
            lines = listOf(line(JewelryCategory.RING, 100.0, 2)),
            paymentMethod = PaymentMethod.CREDIT_CARD
        )
        assertEquals(0.0, totals.discountTotal, 0.001)
        assertEquals(200.0, totals.grandTotal, 0.001)
    }

    @Test
    fun zelleAppliesTwentyPercentToWatches() {
        val totals = CheckoutCalculator.price(
            lines = listOf(line(JewelryCategory.WATCH, 500.0, 1)),
            paymentMethod = PaymentMethod.ZELLE_WIRE
        )
        assertEquals(100.0, totals.discountTotal, 0.001)
        assertEquals(400.0, totals.grandTotal, 0.001)
    }

    @Test
    fun zelleAppliesFivePercentToOtherJewelry() {
        val totals = CheckoutCalculator.price(
            lines = listOf(line(JewelryCategory.NECKLACE, 200.0, 1)),
            paymentMethod = PaymentMethod.ZELLE_WIRE
        )
        assertEquals(10.0, totals.discountTotal, 0.001)
        assertEquals(190.0, totals.grandTotal, 0.001)
    }

    @Test
    fun invoiceNumbersIncrementPerDay() {
        val now = System.currentTimeMillis()
        val prefix = InvoiceNumbers.prefixFor(now)
        assertEquals(prefix + "0001", InvoiceNumbers.next(null, now))
        assertEquals(prefix + "0002", InvoiceNumbers.next(prefix + "0001", now))
    }

    private fun line(category: JewelryCategory, price: Double, quantity: Int): CheckoutLine {
        return CheckoutLine(
            item = InventoryItemEntity(
                id = 1,
                sku = "SKU-1",
                name = "Test",
                category = category,
                metalType = MetalType.YELLOW_GOLD,
                caratWeight = null,
                quantityInStock = 5,
                costPriceUsd = 50.0,
                retailPriceUsd = price,
                location = "Case A",
                dateAdded = 0L,
                notes = null,
                rfidBarcode = null
            ),
            quantity = quantity
        )
    }
}
