package com.amansharma.jewelryinventory.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.amansharma.jewelryinventory.data.local.entity.InvoiceEntity
import com.amansharma.jewelryinventory.data.local.entity.InvoiceLineItemEntity
import com.amansharma.jewelryinventory.data.local.entity.InvoiceWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {

    @Transaction
    @Query(
        """
        SELECT * FROM invoices
        WHERE (
            :query = ''
            OR invoiceNumber LIKE '%' || :query || '%' ESCAPE '\'
            OR IFNULL(customerName, '') LIKE '%' || :query || '%' ESCAPE '\'
            OR id IN (
                SELECT invoiceId FROM invoice_line_items
                WHERE sku LIKE '%' || :query || '%' ESCAPE '\'
                   OR IFNULL(rfidBarcode, '') LIKE '%' || :query || '%' ESCAPE '\'
            )
        )
        AND (:paymentMethod IS NULL OR paymentMethod = :paymentMethod)
        AND purchaseDateTime >= :fromMillis
        AND purchaseDateTime <= :toMillis
        AND totalAmount >= :minAmount
        AND totalAmount <= :maxAmount
        ORDER BY purchaseDateTime DESC
        """
    )
    fun observeFiltered(
        query: String,
        paymentMethod: String?,
        fromMillis: Long,
        toMillis: Long,
        minAmount: Double,
        maxAmount: Double
    ): Flow<List<InvoiceWithItems>>

    @Transaction
    @Query("SELECT * FROM invoices WHERE id = :id")
    fun observeById(id: Long): Flow<InvoiceWithItems?>

    @Transaction
    @Query("SELECT * FROM invoices WHERE id = :id")
    suspend fun getById(id: Long): InvoiceWithItems?

    @Query(
        "SELECT invoiceNumber FROM invoices " +
                "WHERE invoiceNumber LIKE :prefix || '%' " +
                "ORDER BY invoiceNumber DESC LIMIT 1"
    )
    suspend fun latestInvoiceNumberWithPrefix(prefix: String): String?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertInvoice(invoice: InvoiceEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertLineItems(items: List<InvoiceLineItemEntity>)
}