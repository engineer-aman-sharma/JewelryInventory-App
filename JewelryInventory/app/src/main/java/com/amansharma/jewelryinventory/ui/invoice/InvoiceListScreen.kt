package com.amansharma.jewelryinventory.ui.invoice

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amansharma.jewelryinventory.data.local.entity.InvoiceWithItems
import com.amansharma.jewelryinventory.data.model.PaymentMethod
import com.amansharma.jewelryinventory.ui.components.EmptyState
import com.amansharma.jewelryinventory.ui.components.FullScreenLoading
import com.amansharma.jewelryinventory.utils.DateTimeUtils
import com.amansharma.jewelryinventory.utils.Money
import com.amansharma.jewelryinventory.viewmodel.InvoiceDateFilter
import com.amansharma.jewelryinventory.viewmodel.InvoiceListUiState
import com.amansharma.jewelryinventory.viewmodel.InvoiceListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceListScreen(
    onInvoiceClick: (Long) -> Unit,
    viewModel: InvoiceListViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            TopAppBar(title = { Text("Invoices") })
        }
    ) { padding ->
        InvoiceListContent(
            state = state,
            onSearchChange = viewModel::onSearchChange,
            onPaymentMethodFilter = viewModel::onPaymentMethodFilter,
            onDateFilter = viewModel::onDateFilter,
            onMinAmountChange = viewModel::onMinAmountChange,
            onMaxAmountChange = viewModel::onMaxAmountChange,
            onClearFilters = viewModel::clearFilters,
            onInvoiceClick = onInvoiceClick,
            modifier = Modifier.padding(top = padding.calculateTopPadding())
        )
    }
}

@Composable
private fun InvoiceListContent(
    state: InvoiceListUiState,
    onSearchChange: (String) -> Unit,
    onPaymentMethodFilter: (PaymentMethod?) -> Unit,
    onDateFilter: (InvoiceDateFilter) -> Unit,
    onMinAmountChange: (String) -> Unit,
    onMaxAmountChange: (String) -> Unit,
    onClearFilters: () -> Unit,
    onInvoiceClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        OutlinedTextField(
            value = state.query.search,
            onValueChange = onSearchChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
            label = { Text("Search invoice #, customer, or SKU") },
            singleLine = true
        )

        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 1.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            InvoiceDateFilter.entries.forEach { filter ->
                FilterChip(
                    selected = state.query.dateFilter == filter,
                    onClick = { onDateFilter(filter) },
                    label = { Text(filter.label(), maxLines = 1) }
                )
            }

            PaymentMethod.entries.forEach { method ->
                FilterChip(
                    selected = state.query.paymentMethod == method,
                    onClick = { onPaymentMethodFilter(method) },
                    label = { Text(method.displayName, maxLines = 1) }
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 1.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = state.query.minAmount,
                onValueChange = onMinAmountChange,
                modifier = Modifier.weight(1f),
                label = { Text("Min total") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                )
            )

            OutlinedTextField(
                value = state.query.maxAmount,
                onValueChange = onMaxAmountChange,
                modifier = Modifier.weight(1f),
                label = { Text("Max total") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                )
            )
        }

        if (state.hasActiveFilters) {
            TextButton(
                onClick = onClearFilters,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 0.dp)
            ) {
                Text("Clear search and filters")
            }
        }

        when {
            state.isLoading -> FullScreenLoading()

            state.errorMessage != null -> EmptyState(
                icon = Icons.Default.SearchOff,
                title = "Unable to load invoices",
                message = state.errorMessage
            )

            state.invoices.isEmpty() && state.hasActiveFilters -> EmptyState(
                icon = Icons.Default.SearchOff,
                title = "No matching invoices",
                message = "Try a different invoice number, customer, SKU, date, or amount."
            )

            state.invoices.isEmpty() -> EmptyState(
                icon = Icons.Default.ReceiptLong,
                title = "No invoices yet",
                message = "Completed sales will appear here with a unique invoice number."
            )

            else -> LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 0.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = state.invoices,
                    key = { it.invoice.id }
                ) { invoice ->
                    InvoiceCard(
                        invoice = invoice,
                        onClick = {
                            onInvoiceClick(invoice.invoice.id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun InvoiceCard(
    invoice: InvoiceWithItems,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                invoice.invoice.invoiceNumber,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                invoice.invoice.customerName
                    ?.ifBlank { "Walk-in customer" }
                    ?: "Walk-in customer"
            )

            Text(
                "${DateTimeUtils.formatDisplay(invoice.invoice.purchaseDateTime)} • " +
                        invoice.invoice.paymentMethod.displayName,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                Money.format(invoice.invoice.totalAmount),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

private fun InvoiceDateFilter.label(): String = when (this) {
    InvoiceDateFilter.ALL -> "All dates"
    InvoiceDateFilter.TODAY -> "Today"
    InvoiceDateFilter.LAST_7_DAYS -> "Last 7 days"
    InvoiceDateFilter.LAST_30_DAYS -> "Last 30 days"
}
