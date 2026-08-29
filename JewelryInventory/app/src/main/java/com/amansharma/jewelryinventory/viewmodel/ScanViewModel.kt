package com.amansharma.jewelryinventory.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amansharma.jewelryinventory.data.repository.InventoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

sealed class ScanEvent {
    data class ItemFound(val itemId: Long) : ScanEvent()
    data class NotFound(val code: String) : ScanEvent()
    data class Error(val message: String) : ScanEvent()
}

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository
) : ViewModel() {

    private val _manualCode = MutableStateFlow("")
    val manualCode = _manualCode.asStateFlow()

    private val _isLookingUp = MutableStateFlow(false)
    val isLookingUp = _isLookingUp.asStateFlow()

    private val _events = MutableSharedFlow<ScanEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    @Volatile
    private var processing = false

    fun onManualCodeChange(value: String) {
        _manualCode.value = value
    }

    fun onBarcodeDetected(code: String) {
        lookup(code, fromScanner = true)
    }

    fun lookupManual() {
        lookup(_manualCode.value, fromScanner = false)
    }

    private fun lookup(rawCode: String, fromScanner: Boolean) {
        val code = rawCode.trim()
        if (code.isEmpty()) {
            _events.tryEmit(ScanEvent.Error("Enter or scan a SKU, RFID, or barcode."))
            return
        }
        if (processing) return
        processing = true
        viewModelScope.launch {
            _isLookingUp.value = true
            try {
                val item = inventoryRepository.findBySkuOrBarcode(code)
                if (item != null) {
                    _events.emit(ScanEvent.ItemFound(item.id))
                } else {
                    _events.emit(ScanEvent.NotFound(code))
                    if (fromScanner) {
                        delay(1_400.milliseconds)
                    }
                    processing = false
                }
            } catch (error: Exception) {
                _events.emit(ScanEvent.Error(error.message ?: "Unable to search inventory."))
                processing = false
            } finally {
                _isLookingUp.value = false
            }
        }
    }

    fun resetProcessing() {
        processing = false
        _isLookingUp.value = false
    }
}
