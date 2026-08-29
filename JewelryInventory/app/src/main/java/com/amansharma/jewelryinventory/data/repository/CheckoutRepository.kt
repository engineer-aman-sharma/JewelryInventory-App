package com.amansharma.jewelryinventory.data.repository

import android.database.sqlite.SQLiteConstraintException
import androidx.room.withTransaction
import com.amansharma.jewelryinventory.data.local.JewelryDatabase
import com.amansharma.jewelryinventory.data.local.dao.InventoryDao
import com.amansharma.jewelryinventory.data.local.dao.InvoiceDao
import com.amansharma.jewelryinventory.data.local.entity.InvoiceEntity
import com.amansharma.jewelryinventory.data.local.entity.InvoiceLineItemEntity
import com.amansharma.jewelryinventory.data.local.entity.InvoiceWithItems
import com.amansharma.jewelryinventory.data.model.PaymentMethod
import com.amansharma.jewelryinventory.utils.CheckoutCalculator
import com.amansharma.jewelryinventory.utils.CheckoutLine
import com.amansharma.jewelryinventory.utils.InvoiceNumbers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CheckoutRepository @Inject constructor(
    private val database: JewelryDatabase,
    private val inventoryDao: InventoryDao,
    private val invoiceDao: InvoiceDao
) {
    suspend fun confirmSale(
        requestedLines: List<CheckoutLine>,
        paymentMethod: PaymentMethod,
        customerName: String?
    ): DataResult<InvoiceWithItems> {
        if (requestedLines.isEmpty()) {
            return DataResult.Error("Select at least one product to check out.")
        }

        return try {
            val invoice = database.withTransaction {
                completeSale(requestedLines, paymentMethod, customerName)
            }
            DataResult.Success(invoice)
        } catch (error: InsufficientStockException) {
            DataResult.Error(
                error.message ?: "Not enough stock for one or more items.",
                error
            )
        } catch (error: IllegalArgumentException) {
            DataResult.Error(error.message ?: "Invalid checkout details.", error)
        } catch (error: SQLiteConstraintException) {
            DataResult.Error(
                "Unable to create a unique invoice. Please try again.",
                error
            )
        }
    }

    private suspend fun completeSale(
        requestedLines: List<CheckoutLine>,
        paymentMethod: PaymentMethod,
        customerName: String?
    ): InvoiceWithItems {
        val freshItems = requestedLines.map { line ->
            val current = inventoryDao.getById(line.item.id)
                ?: throw IllegalStateException(
                    "Item ${line.item.sku} is no longer in inventory."
                )

            if (line.quantity <= 0) {
                throw IllegalArgumentException(
                    "Quantity for ${current.name} must be at least 1."
                )
            }

            if (line.quantity > current.quantityInStock) {
                throw InsufficientStockException(
                    "Only ${current.quantityInStock} in stock for " +
                            "${current.name} (${current.sku})."
                )
            }

            CheckoutLine(
                item = current,
                quantity = line.quantity
            )
        }

        val totals = CheckoutCalculator.price(freshItems, paymentMethod)
        val now = System.currentTimeMillis()
        val invoiceNumber = InvoiceNumbers.next(
            latestForToday = invoiceDao.latestInvoiceNumberWithPrefix(
                InvoiceNumbers.prefixFor(now)
            ),
            nowMillis = now
        )

        val invoiceId = invoiceDao.insertInvoice(
            InvoiceEntity(
                invoiceNumber = invoiceNumber,
                customerName = customerName?.trim()?.takeIf { it.isNotEmpty() },
                subtotal = totals.subtotal,
                discountTotal = totals.discountTotal,
                totalAmount = totals.grandTotal,
                paymentMethod = paymentMethod,
                purchaseDateTime = now
            )
        )

        val lineEntities = totals.lines.map { priced ->
            val updated = inventoryDao.decrementStock(
                priced.item.id,
                priced.quantity
            )

            if (updated != 1) {
                throw InsufficientStockException(
                    "Stock for ${priced.item.name} changed before checkout completed."
                )
            }

            InvoiceLineItemEntity(
                invoiceId = invoiceId,
                inventoryItemId = priced.item.id,
                name = priced.item.name,
                sku = priced.item.sku,
                rfidBarcode = priced.item.rfidBarcode,
                category = priced.item.category.displayName,
                quantity = priced.quantity,
                pricePerItem = priced.unitPrice,
                discount = priced.discountAmount,
                lineTotal = priced.lineTotal
            )
        }

        invoiceDao.insertLineItems(lineEntities)

        return invoiceDao.getById(invoiceId)
            ?: throw IllegalStateException(
                "Invoice was created but could not be loaded."
            )
    }
}

class InsufficientStockException(message: String) : IllegalStateException(message)