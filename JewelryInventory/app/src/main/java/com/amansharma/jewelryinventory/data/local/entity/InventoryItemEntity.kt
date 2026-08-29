package com.amansharma.jewelryinventory.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.amansharma.jewelryinventory.data.model.JewelryCategory
import com.amansharma.jewelryinventory.data.model.MetalType

@Entity(
    tableName = "inventory_items",
    indices = [
        Index(value = ["sku"], unique = true),
        Index(value = ["rfidBarcode"], unique = true)
    ]
)
data class InventoryItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sku: String,
    val name: String,
    val category: JewelryCategory,
    val metalType: MetalType,
    val caratWeight: Double?,
    val quantityInStock: Int,
    val costPriceUsd: Double,
    val retailPriceUsd: Double,
    val location: String,
    val dateAdded: Long,
    val notes: String?,
    val rfidBarcode: String?
)
