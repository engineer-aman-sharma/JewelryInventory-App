package com.amansharma.jewelryinventory.data.repository

import com.amansharma.jewelryinventory.data.local.dao.InvoiceDao
import com.amansharma.jewelryinventory.data.local.entity.InvoiceWithItems
import com.amansharma.jewelryinventory.data.model.PaymentMethod
import com.amansharma.jewelryinventory.utils.LikeQuery
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InvoiceRepository @Inject constructor(
    private val invoiceDao: InvoiceDao
) {
    fun observeFiltered(
        query: String,
        paymentMethod: PaymentMethod?,
        fromMillis: Long,
        toMillis: Long,
        minAmount: Double,
        maxAmount: Double
    ): Flow<List<InvoiceWithItems>> {
        return invoiceDao.observeFiltered(
            query = LikeQuery.escape(query),
            paymentMethod = paymentMethod?.name,
            fromMillis = fromMillis,
            toMillis = toMillis,
            minAmount = minAmount,
            maxAmount = maxAmount
        )
    }

    fun observeById(id: Long): Flow<InvoiceWithItems?> = invoiceDao.observeById(id)
}
