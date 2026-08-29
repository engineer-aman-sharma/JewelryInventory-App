package com.amansharma.jewelryinventory.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "invoice_line_items",
    foreignKeys = [
        ForeignKey(
            entity = InvoiceEntity::class,
            parentColumns = ["id"],
            childColumns = ["invoiceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("invoiceId"), Index("sku")]
)
data class InvoiceLineItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val invoiceId: Long,
    val inventoryItemId: Long,
    val name: String,
    val sku: String,
    val rfidBarcode: String?,
    val category: String,
    val quantity: Int,
    val pricePerItem: Double,
    val discount: Double,
    val lineTotal: Double
)