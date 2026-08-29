package com.amansharma.jewelryinventory.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amansharma.jewelryinventory.data.local.entity.InvoiceWithItems
import com.amansharma.jewelryinventory.data.model.PaymentMethod
import com.amansharma.jewelryinventory.data.repository.InvoiceRepository
import com.amansharma.jewelryinventory.utils.DateTimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

enum class InvoiceDateFilter {
    ALL, TODAY, LAST_7_DAYS, LAST_30_DAYS
}

data class InvoiceQuery(
    val search: String = "",
    val paymentMethod: PaymentMethod? = null,
    val dateFilter: InvoiceDateFilter = InvoiceDateFilter.ALL,
    val minAmount: String = "",
    val maxAmount: String = ""
)

data class InvoiceListUiState(
    val invoices: List<InvoiceWithItems> = emptyList(),
    val query: InvoiceQuery = InvoiceQuery(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
) {
    val hasActiveFilters: Boolean
        get() = query.search.isNotBlank() ||
            query.paymentMethod != null ||
            query.dateFilter != InvoiceDateFilter.ALL ||
            query.minAmount.isNotBlank() ||
            query.maxAmount.isNotBlank()
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class InvoiceListViewModel @Inject constructor(
    private val invoiceRepository: InvoiceRepository
) : ViewModel() {

    private val query = MutableStateFlow(InvoiceQuery())
    private val loadedOnce = MutableStateFlow(false)

    private val invoices = query.flatMapLatest { current ->
        val now = System.currentTimeMillis()
        val fromMillis = when (current.dateFilter) {
            InvoiceDateFilter.ALL -> 0L
            InvoiceDateFilter.TODAY -> DateTimeUtils.startOfDay(now)
            InvoiceDateFilter.LAST_7_DAYS -> DateTimeUtils.daysAgoStart(6)
            InvoiceDateFilter.LAST_30_DAYS -> DateTimeUtils.daysAgoStart(29)
        }
        val minAmount = current.minAmount.toDoubleOrNull() ?: 0.0
        val maxAmount = current.maxAmount.toDoubleOrNull() ?: Double.MAX_VALUE
        invoiceRepository.observeFiltered(
            query = current.search,
            paymentMethod = current.paymentMethod,
            fromMillis = fromMillis,
            toMillis = now + 1_000,
            minAmount = minAmount,
            maxAmount = maxAmount
        ).map { list ->
            loadedOnce.value = true
            Result.success(list)
        }.catch { error ->
            loadedOnce.value = true
            emit(Result.failure(error))
        }
    }

    val uiState = combine(invoices, query, loadedOnce) { result, currentQuery, loaded ->
        InvoiceListUiState(
            invoices = result.getOrDefault(emptyList()),
            query = currentQuery,
            isLoading = !loaded,
            errorMessage = result.exceptionOrNull()?.message
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InvoiceListUiState())

    fun onSearchChange(value: String) {
        query.update { it.copy(search = value) }
    }

    fun onPaymentMethodFilter(method: PaymentMethod?) {
        query.update { current ->
            current.copy(paymentMethod = if (current.paymentMethod == method) null else method)
        }
    }

    fun onDateFilter(filter: InvoiceDateFilter) {
        query.update { it.copy(dateFilter = filter) }
    }

    fun onMinAmountChange(value: String) {
        query.update { it.copy(minAmount = value) }
    }

    fun onMaxAmountChange(value: String) {
        query.update { it.copy(maxAmount = value) }
    }

    fun clearFilters() {
        query.value = InvoiceQuery()
    }
}
