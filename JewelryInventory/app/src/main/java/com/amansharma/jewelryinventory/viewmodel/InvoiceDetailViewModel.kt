package com.amansharma.jewelryinventory.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amansharma.jewelryinventory.data.local.entity.InvoiceWithItems
import com.amansharma.jewelryinventory.data.repository.InvoiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class InvoiceDetailViewModel @Inject constructor(
    invoiceRepository: InvoiceRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val invoiceId: Long = savedStateHandle["invoiceId"] ?: 0L

    val invoice: StateFlow<InvoiceWithItems?> = invoiceRepository.observeById(invoiceId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
