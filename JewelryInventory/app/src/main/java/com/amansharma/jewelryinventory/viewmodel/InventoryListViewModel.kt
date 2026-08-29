package com.amansharma.jewelryinventory.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amansharma.jewelryinventory.data.local.entity.InventoryItemEntity
import com.amansharma.jewelryinventory.data.model.JewelryCategory
import com.amansharma.jewelryinventory.data.model.MetalType
import com.amansharma.jewelryinventory.data.repository.CheckoutCart
import com.amansharma.jewelryinventory.data.repository.InventoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class InventoryQuery(
    val search: String = "",
    val category: JewelryCategory? = null,
    val metalType: MetalType? = null
)

data class InventoryListUiState(
    val items: List<InventoryItemEntity> = emptyList(),
    val query: InventoryQuery = InventoryQuery(),
    val selectedIds: Set<Long> = emptySet(),
    val isSelectionMode: Boolean = false,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
) {
    val hasActiveFilters: Boolean
        get() = query.search.isNotBlank() || query.category != null || query.metalType != null
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class InventoryListViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    private val checkoutCart: CheckoutCart
) : ViewModel() {

    private val query = MutableStateFlow(InventoryQuery())
    private val selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    private val selectionMode = MutableStateFlow(false)
    private val loadedOnce = MutableStateFlow(false)

    private val _snackbar = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val snackbar = _snackbar.asSharedFlow()

    private val items = query.flatMapLatest { current ->
        inventoryRepository.observeFiltered(current.search, current.category, current.metalType)
            .map { list ->
                loadedOnce.value = true
                Result.success(list)
            }
            .catch { error ->
                loadedOnce.value = true
                emit(Result.failure(error))
            }
    }

    val uiState: StateFlow<InventoryListUiState> = combine(
        items,
        query,
        selectedIds,
        selectionMode,
        loadedOnce
    ) { itemsResult, currentQuery, selected, selecting, loaded ->
        val list = itemsResult.getOrDefault(emptyList())
        val visibleIds = list.map { it.id }.toSet()
        InventoryListUiState(
            items = list,
            query = currentQuery,
            selectedIds = selected.intersect(visibleIds),
            isSelectionMode = selecting,
            isLoading = !loaded,
            errorMessage = itemsResult.exceptionOrNull()?.message
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = InventoryListUiState()
    )

    fun onSearchChange(value: String) {
        query.update { it.copy(search = value) }
    }

    fun onCategoryFilter(category: JewelryCategory?) {
        query.update { current ->
            current.copy(category = if (current.category == category) null else category)
        }
    }

    fun onMetalFilter(metalType: MetalType?) {
        query.update { current ->
            current.copy(metalType = if (current.metalType == metalType) null else metalType)
        }
    }

    fun clearFilters() {
        query.value = InventoryQuery()
    }

    fun onItemClick(itemId: Long): ItemClickResult {
        return if (selectionMode.value) {
            toggleSelection(itemId)
            ItemClickResult.Toggled
        } else {
            ItemClickResult.OpenDetail
        }
    }

    fun onItemLongClick(itemId: Long) {
        selectionMode.value = true
        selectedIds.update { it + itemId }
    }

    fun toggleSelection(itemId: Long) {
        selectedIds.update { current ->
            if (itemId in current) current - itemId else current + itemId
        }
        if (selectedIds.value.isEmpty()) {
            selectionMode.value = false
        }
    }

    fun exitSelectionMode() {
        selectionMode.value = false
        selectedIds.value = emptySet()
    }

    fun selectedItemIds(): List<Long> = selectedIds.value.toList()

    fun prepareCheckout(): Boolean {
        val inStockIds = uiState.value.items
            .filter { it.id in selectedIds.value && it.quantityInStock > 0 }
            .map { it.id }
        if (inStockIds.isEmpty()) {
            _snackbar.tryEmit("Select at least one in-stock item to check out.")
            return false
        }
        checkoutCart.start(inStockIds)
        exitSelectionMode()
        return true
    }

    fun notify(message: String) {
        _snackbar.tryEmit(message)
    }

    enum class ItemClickResult { OpenDetail, Toggled }
}
