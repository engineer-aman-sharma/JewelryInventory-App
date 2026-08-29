package com.amansharma.jewelryinventory.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amansharma.jewelryinventory.data.local.entity.InventoryItemEntity
import com.amansharma.jewelryinventory.data.repository.CheckoutCart
import com.amansharma.jewelryinventory.data.repository.DataResult
import com.amansharma.jewelryinventory.data.repository.InventoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    private val checkoutCart: CheckoutCart,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val itemId: Long = savedStateHandle["itemId"] ?: 0L

    val item: StateFlow<InventoryItemEntity?> = inventoryRepository.observeById(itemId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _snackbar = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val snackbar = _snackbar.asSharedFlow()

    private val _deleted = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val deleted = _deleted.asSharedFlow()

    private val _sellReady = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val sellReady = _sellReady.asSharedFlow()

    fun delete() {
        val current = item.value ?: return
        viewModelScope.launch {
            when (val result = inventoryRepository.delete(current)) {
                is DataResult.Success -> _deleted.emit(Unit)
                is DataResult.Error -> _snackbar.emit(result.message)
            }
        }
    }

    fun sellProduct() {
        val current = item.value
        if (current == null) {
            _snackbar.tryEmit("This item could not be found.")
            return
        }
        if (current.quantityInStock <= 0) {
            _snackbar.tryEmit("This item is out of stock.")
            return
        }
        checkoutCart.startSingle(current)
        _sellReady.tryEmit(Unit)
    }
}
