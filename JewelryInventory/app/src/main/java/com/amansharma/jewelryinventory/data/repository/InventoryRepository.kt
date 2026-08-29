package com.amansharma.jewelryinventory.data.repository

import android.database.sqlite.SQLiteConstraintException
import com.amansharma.jewelryinventory.data.local.dao.InventoryDao
import com.amansharma.jewelryinventory.data.local.entity.InventoryItemEntity
import com.amansharma.jewelryinventory.data.model.JewelryCategory
import com.amansharma.jewelryinventory.data.model.MetalType
import com.amansharma.jewelryinventory.utils.LikeQuery
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InventoryRepository @Inject constructor(
    private val inventoryDao: InventoryDao
) {
    fun observeFiltered(
        query: String,
        category: JewelryCategory?,
        metalType: MetalType?
    ): Flow<List<InventoryItemEntity>> {
        return inventoryDao.observeFiltered(
            query = LikeQuery.escape(query),
            category = category?.name,
            metalType = metalType?.name
        )
    }

    fun observeById(id: Long): Flow<InventoryItemEntity?> =
        inventoryDao.observeById(id)

    suspend fun getById(id: Long): InventoryItemEntity? =
        inventoryDao.getById(id)

    suspend fun getByIds(ids: List<Long>): List<InventoryItemEntity> {
        if (ids.isEmpty()) return emptyList()
        return inventoryDao.getByIds(ids)
    }

    suspend fun findBySkuOrBarcode(code: String): InventoryItemEntity? {
        val trimmed = code.trim()
        if (trimmed.isEmpty()) return null
        return inventoryDao.findBySkuOrBarcode(trimmed)
    }

    suspend fun save(item: InventoryItemEntity): DataResult<Long> {
        val sku = item.sku.trim()

        if (sku.isEmpty()) {
            return DataResult.Error("SKU is required.")
        }

        if (item.name.isBlank()) {
            return DataResult.Error("Name is required.")
        }

        if (item.quantityInStock < 0) {
            return DataResult.Error("Quantity cannot be negative.")
        }

        if (item.costPriceUsd < 0.0) {
            return DataResult.Error("Cost price cannot be negative.")
        }

        if (item.retailPriceUsd <= 0.0) {
            return DataResult.Error("Retail price must be greater than zero.")
        }

        val skuCount = inventoryDao.countBySku(sku, item.id)
        if (skuCount > 0) {
            return DataResult.Error(
                "SKU \"$sku\" already exists. Each item must have a unique SKU."
            )
        }

        val barcode = item.rfidBarcode
            ?.trim()
            ?.takeIf { it.isNotEmpty() }

        if (barcode != null && inventoryDao.countByBarcode(barcode, item.id) > 0) {
            return DataResult.Error(
                "RFID/barcode \"$barcode\" is already assigned to another item."
            )
        }

        val sanitized = item.copy(
            sku = sku,
            name = item.name.trim(),
            location = item.location.trim(),
            notes = item.notes?.trim()?.takeIf { it.isNotEmpty() },
            rfidBarcode = barcode
        )

        return try {
            val id = if (sanitized.id == 0L) {
                inventoryDao.insert(sanitized)
            } else {
                inventoryDao.update(sanitized)
                sanitized.id
            }

            DataResult.Success(id)
        } catch (error: SQLiteConstraintException) {
            DataResult.Error(
                "This SKU or RFID/barcode is already in use.",
                error
            )
        }
    }

    suspend fun delete(item: InventoryItemEntity): DataResult<Unit> {
        return try {
            inventoryDao.delete(item)
            DataResult.Success(Unit)
        } catch (error: SQLiteConstraintException) {
            DataResult.Error(
                "Unable to delete this item because it is being used.",
                error
            )
        }
    }
}