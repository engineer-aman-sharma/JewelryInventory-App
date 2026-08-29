package com.amansharma.jewelryinventory.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.amansharma.jewelryinventory.data.local.dao.InventoryDao
import com.amansharma.jewelryinventory.data.local.dao.InvoiceDao
import com.amansharma.jewelryinventory.data.local.entity.InventoryItemEntity
import com.amansharma.jewelryinventory.data.local.entity.InvoiceEntity
import com.amansharma.jewelryinventory.data.local.entity.InvoiceLineItemEntity

@Database(
    entities = [
        InventoryItemEntity::class,
        InvoiceEntity::class,
        InvoiceLineItemEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class JewelryDatabase : RoomDatabase() {
    abstract fun inventoryDao(): InventoryDao
    abstract fun invoiceDao(): InvoiceDao
}
