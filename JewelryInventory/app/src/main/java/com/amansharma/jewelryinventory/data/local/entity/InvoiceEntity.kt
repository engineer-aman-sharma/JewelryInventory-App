package com.amansharma.jewelryinventory.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.amansharma.jewelryinventory.data.model.PaymentMethod

@Entity(
    tableName = "invoices",
    indices = [Index(value = ["invoiceNumber"], unique = true)]
)
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val invoiceNumber: String,
    val customerName: String?,
    val subtotal: Double,
    val discountTotal: Double,
    val totalAmount: Double,
    val paymentMethod: PaymentMethod,
    val purchaseDateTime: Long
)