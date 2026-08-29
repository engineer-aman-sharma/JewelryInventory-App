package com.amansharma.jewelryinventory.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amansharma.jewelryinventory.data.local.entity.InventoryItemEntity
import com.amansharma.jewelryinventory.data.model.PaymentMethod
import com.amansharma.jewelryinventory.data.repository.CheckoutCart
import com.amansharma.jewelryinventory.data.repository.CheckoutRepository
import com.amansharma.jewelryinventory.data.repository.DataResult
import com.amansharma.jewelryinventory.data.repository.InventoryRepository
import com.amansharma.jewelryinventory.utils.CheckoutCalculator
import com.amansharma.jewelryinventory.utils.CheckoutLine
import com.amansharma.jewelryinventory.utils.CheckoutTotals
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CheckoutUiState(
    val itemsById: Map<Long, InventoryItemEntity> = emptyMap(),
    val quantities: Map<Long, Int> = emptyMap(),
    val paymentMethod: PaymentMethod = PaymentMethod.CREDIT_CARD,
    val customerName: String = "",
    val totals: CheckoutTotals = CheckoutCalculator.price(emptyList(), PaymentMethod.CREDIT_CARD),
    val isConfirming: Boolean = false,
    val stockErrors: Map<Long, String> = emptyMap()
) {
    val isEmpty: Boolean get() = quantities.isEmpty()
    val canConfirm: Boolean get() = !isEmpty && stockErrors.isEmpty() && !isConfirming
}

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val checkoutCart: CheckoutCart,
    private val inventoryRepository: InventoryRepository,
    private val checkoutRepository: CheckoutRepository
) : ViewModel() {

    private val paymentMethod = MutableStateFlow(PaymentMethod.CREDIT_CARD)
    private val customerName = MutableStateFlow("")
    private val confirming = MutableStateFlow(false)
    private val inventorySnapshot = MutableStateFlow<Map<Long, InventoryItemEntity>>(emptyMap())

    private val _snackbar = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val snackbar = _snackbar.asSharedFlow()

    private val _completedInvoiceId = MutableSharedFlow<Long>(extraBufferCapacity = 1)
    val completedInvoiceId = _completedInvoiceId.asSharedFlow()

    val uiState = combine(
        checkoutCart.entries,
        inventorySnapshot,
        paymentMethod,
        customerName,
        confirming
    ) { entries, items, method, name, isConfirming ->
        val quantities = entries.associate { it.itemId to it.quantity }
        val lines = entries.mapNotNull { entry ->
            items[entry.itemId]?.let { CheckoutLine(it, entry.quantity) }
        }
        val stockErrors = buildMap {
            entries.forEach { entry ->
                val item = items[entry.itemId]
                when {
                    item == null -> put(entry.itemId, "This item is no longer available.")
                    item.quantityInStock <= 0 -> put(entry.itemId, "Out of stock.")
                    entry.quantity > item.quantityInStock -> {
                        put(entry.itemId, "Only ${item.quantityInStock} available.")
                    }
                }
            }
        }
        CheckoutUiState(
            itemsById = items,
            quantities = quantities,
            paymentMethod = method,
            customerName = name,
            totals = CheckoutCalculator.price(lines, method),
            isConfirming = isConfirming,
            stockErrors = stockErrors
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CheckoutUiState())

    init {
        viewModelScope.launch {
            checkoutCart.entries.collect { entries ->
                val ids = entries.map { it.itemId }
                inventorySnapshot.value = inventoryRepository.getByIds(ids).associateBy { it.id }
            }
        }
    }

    fun onPaymentMethodChange(method: PaymentMethod) {
        paymentMethod.value = method
    }

    fun onCustomerNameChange(value: String) {
        customerName.value = value
    }

    fun onQuantityChange(itemId: Long, quantity: Int) {
        val stock = inventorySnapshot.value[itemId]?.quantityInStock ?: quantity
        checkoutCart.updateQuantity(itemId, quantity.coerceIn(1, stock.coerceAtLeast(1)))
    }

    fun removeItem(itemId: Long) {
        checkoutCart.remove(itemId)
    }

    fun confirmSale() {
        val state = uiState.value
        if (!state.canConfirm) {
            _snackbar.tryEmit(state.stockErrors.values.firstOrNull() ?: "Unable to confirm this sale.")
            return
        }
        viewModelScope.launch {
            confirming.value = true
            val result = checkoutRepository.confirmSale(
                requestedLines = state.totals.lines.map { CheckoutLine(it.item, it.quantity) },
                paymentMethod = state.paymentMethod,
                customerName = state.customerName
            )
            confirming.value = false
            when (result) {
                is DataResult.Success -> {
                    checkoutCart.clear()
                    _completedInvoiceId.emit(result.data.invoice.id)
                }
                is DataResult.Error -> _snackbar.emit(result.message)
            }
        }
    }
}
