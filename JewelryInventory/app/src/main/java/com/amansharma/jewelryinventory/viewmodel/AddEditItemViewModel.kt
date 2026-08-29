package com.amansharma.jewelryinventory.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amansharma.jewelryinventory.data.local.entity.InventoryItemEntity
import com.amansharma.jewelryinventory.data.model.JewelryCategory
import com.amansharma.jewelryinventory.data.model.MetalType
import com.amansharma.jewelryinventory.data.repository.DataResult
import com.amansharma.jewelryinventory.data.repository.InventoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ItemFormState(
    val sku: String = "",
    val name: String = "",
    val category: JewelryCategory = JewelryCategory.RING,
    val metalType: MetalType = MetalType.YELLOW_GOLD,
    val caratWeight: String = "",
    val quantity: String = "",
    val costPrice: String = "",
    val retailPrice: String = "",
    val location: String = "",
    val notes: String = "",
    val rfidBarcode: String = "",
    val skuError: String? = null,
    val nameError: String? = null,
    val quantityError: String? = null,
    val retailPriceError: String? = null,
    val costPriceError: String? = null,
    val caratWeightError: String? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isEdit: Boolean = false
)

@HiltViewModel
class AddEditItemViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val itemId: Long = savedStateHandle.get<Long>("itemId") ?: 0L
    private var existingDateAdded: Long = System.currentTimeMillis()

    private val _form = MutableStateFlow(ItemFormState(isEdit = itemId > 0L, isLoading = itemId > 0L))
    val form = _form.asStateFlow()

    private val _snackbar = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val snackbar = _snackbar.asSharedFlow()

    private val _saved = MutableSharedFlow<Long>(extraBufferCapacity = 1)
    val saved = _saved.asSharedFlow()

    init {
        if (itemId > 0L) {
            viewModelScope.launch {
                val item = inventoryRepository.getById(itemId)
                if (item == null) {
                    _form.update { it.copy(isLoading = false) }
                    _snackbar.emit("This item could not be found.")
                } else {
                    existingDateAdded = item.dateAdded
                    _form.value = ItemFormState(
                        sku = item.sku,
                        name = item.name,
                        category = item.category,
                        metalType = item.metalType,
                        caratWeight = item.caratWeight?.toString().orEmpty(),
                        quantity = item.quantityInStock.toString(),
                        costPrice = item.costPriceUsd.toString(),
                        retailPrice = item.retailPriceUsd.toString(),
                        location = item.location,
                        notes = item.notes.orEmpty(),
                        rfidBarcode = item.rfidBarcode.orEmpty(),
                        isLoading = false,
                        isEdit = true
                    )
                }
            }
        }
    }

    fun update(transform: (ItemFormState) -> ItemFormState) {
        _form.update(transform)
    }

    fun save() {
        val current = _form.value
        val validated = validate(current)
        _form.value = validated
        if (listOf(
                validated.skuError,
                validated.nameError,
                validated.quantityError,
                validated.retailPriceError,
                validated.costPriceError,
                validated.caratWeightError
            ).any { it != null }
        ) {
            _snackbar.tryEmit("Please fix the highlighted fields.")
            return
        }

        viewModelScope.launch {
            _form.update { it.copy(isSaving = true) }
            val result = inventoryRepository.save(toEntity(validated))
            _form.update { it.copy(isSaving = false) }
            when (result) {
                is DataResult.Success -> {
                    _snackbar.tryEmit(if (validated.isEdit) "Item updated." else "Item added to inventory.")
                    _saved.emit(result.data)
                }
                is DataResult.Error -> {
                    if (result.message.contains("SKU", ignoreCase = true)) {
                        _form.update { it.copy(skuError = result.message) }
                    }
                    _snackbar.emit(result.message)
                }
            }
        }
    }

    private fun validate(state: ItemFormState): ItemFormState {
        val skuError = if (state.sku.isBlank()) "SKU is required." else null
        val nameError = if (state.name.isBlank()) "Name cannot be empty." else null
        val quantity = state.quantity.trim().toIntOrNull()
        val quantityError = when {
            state.quantity.isBlank() -> "Quantity is required."
            quantity == null -> "Enter a whole number."
            quantity < 0 -> "Quantity cannot be negative."
            else -> null
        }
        val retail = state.retailPrice.trim().toDoubleOrNull()
        val retailError = when {
            state.retailPrice.isBlank() -> "Retail price is required."
            retail == null -> "Enter a valid price."
            retail <= 0.0 -> "Retail price must be greater than zero."
            else -> null
        }
        val cost = state.costPrice.trim().ifBlank { "0" }.toDoubleOrNull()
        val costError = when {
            cost == null -> "Enter a valid cost price."
            cost < 0.0 -> "Cost price cannot be negative."
            else -> null
        }
        val carat = state.caratWeight.trim()
        val caratValue = carat.toDoubleOrNull()
        val caratError = when {
            carat.isBlank() -> null
            caratValue == null -> "Enter a valid carat weight."
            caratValue <= 0.0 -> "Carat weight must be greater than zero."
            else -> null
        }
        return state.copy(
            skuError = skuError,
            nameError = nameError,
            quantityError = quantityError,
            retailPriceError = retailError,
            costPriceError = costError,
            caratWeightError = caratError
        )
    }

    private fun toEntity(state: ItemFormState): InventoryItemEntity {
        return InventoryItemEntity(
            id = if (state.isEdit) itemId else 0L,
            sku = state.sku,
            name = state.name,
            category = state.category,
            metalType = state.metalType,
            caratWeight = state.caratWeight.trim().toDoubleOrNull(),
            quantityInStock = state.quantity.trim().toInt(),
            costPriceUsd = state.costPrice.trim().ifBlank { "0" }.toDouble(),
            retailPriceUsd = state.retailPrice.trim().toDouble(),
            location = state.location,
            dateAdded = if (state.isEdit) existingDateAdded else System.currentTimeMillis(),
            notes = state.notes,
            rfidBarcode = state.rfidBarcode
        )
    }
}
