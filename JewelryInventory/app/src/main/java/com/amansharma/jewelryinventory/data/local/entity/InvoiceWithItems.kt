package com.amansharma.jewelryinventory.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class InvoiceWithItems(
    @Embedded val invoice: InvoiceEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "invoiceId"
    )
    val items: List<InvoiceLineItemEntity>
)