package com.amansharma.jewelryinventory.data.repository

import com.amansharma.jewelryinventory.data.local.entity.InventoryItemEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

data class CartEntry(
    val itemId: Long,
    val quantity: Int
)

@Singleton
class CheckoutCart @Inject constructor() {
    private val _entries = MutableStateFlow<List<CartEntry>>(emptyList())
    val entries: StateFlow<List<CartEntry>> = _entries.asStateFlow()

    fun start(itemIds: List<Long>) {
        _entries.value = itemIds.distinct().map { CartEntry(it, 1) }
    }

    fun startSingle(item: InventoryItemEntity, quantity: Int = 1) {
        if (item.quantityInStock <= 0) {
            _entries.value = emptyList()
            return
        }

        val safeQuantity = quantity.coerceIn(1, item.quantityInStock)
        _entries.value = listOf(CartEntry(item.id, safeQuantity))
    }

    fun updateQuantity(itemId: Long, quantity: Int) {
        _entries.update { current ->
            current.map { entry ->
                if (entry.itemId == itemId) {
                    entry.copy(quantity = quantity.coerceAtLeast(1))
                } else {
                    entry
                }
            }
        }
    }

    fun remove(itemId: Long) {
        _entries.update { current ->
            current.filterNot { it.itemId == itemId }
        }
    }

    fun clear() {
        _entries.value = emptyList()
    }
}