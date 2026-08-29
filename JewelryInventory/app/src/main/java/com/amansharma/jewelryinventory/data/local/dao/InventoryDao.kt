package com.amansharma.jewelryinventory.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.amansharma.jewelryinventory.data.local.entity.InventoryItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {

    @Query(
        """
        SELECT * FROM inventory_items
        WHERE (
            :query = ''
            OR name LIKE '%' || :query || '%' ESCAPE '\'
            OR IFNULL(notes, '') LIKE '%' || :query || '%' ESCAPE '\'
            OR sku LIKE '%' || :query || '%' ESCAPE '\'
        )
        AND (:category IS NULL OR category = :category)
        AND (:metalType IS NULL OR metalType = :metalType)
        ORDER BY dateAdded DESC
        """
    )
    fun observeFiltered(
        query: String,
        category: String?,
        metalType: String?
    ): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory_items WHERE id = :id")
    fun observeById(id: Long): Flow<InventoryItemEntity?>

    @Query("SELECT * FROM inventory_items WHERE id = :id")
    suspend fun getById(id: Long): InventoryItemEntity?

    @Query("SELECT * FROM inventory_items WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<Long>): List<InventoryItemEntity>

    @Query("SELECT * FROM inventory_items WHERE sku = :code OR rfidBarcode = :code LIMIT 1")
    suspend fun findBySkuOrBarcode(code: String): InventoryItemEntity?

    @Query("SELECT COUNT(*) FROM inventory_items WHERE sku = :sku AND id != :excludeId")
    suspend fun countBySku(sku: String, excludeId: Long): Int

    @Query("SELECT COUNT(*) FROM inventory_items WHERE rfidBarcode = :code AND id != :excludeId")
    suspend fun countByBarcode(code: String, excludeId: Long): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(item: InventoryItemEntity): Long

    @Update
    suspend fun update(item: InventoryItemEntity)

    @Delete
    suspend fun delete(item: InventoryItemEntity)

    @Query(
        """
        UPDATE inventory_items
        SET quantityInStock = quantityInStock - :quantity
        WHERE id = :id AND quantityInStock >= :quantity
        """
    )
    suspend fun decrementStock(id: Long, quantity: Int): Int
}