package com.amansharma.jewelryinventory.ui.invoice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amansharma.jewelryinventory.data.local.entity.InvoiceLineItemEntity
import com.amansharma.jewelryinventory.data.local.entity.InvoiceWithItems
import com.amansharma.jewelryinventory.ui.components.EmptyState
import com.amansharma.jewelryinventory.utils.DateTimeUtils
import com.amansharma.jewelryinventory.utils.Money
import com.amansharma.jewelryinventory.viewmodel.InvoiceDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceDetailScreen(
    onBack: () -> Unit,
    viewModel: InvoiceDetailViewModel = hiltViewModel()
) {
    val invoice by viewModel.invoice.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invoice") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        val current = invoice
        if (current == null) {
            EmptyState(
                icon = Icons.Default.ReceiptLong,
                title = "Invoice not found",
                message = "It may have been removed from this device.",
                modifier = Modifier.padding(padding)
            )
        } else {
            InvoiceDetailContent(current, Modifier.padding(padding))
        }
    }
}

@Composable
private fun InvoiceDetailContent(invoice: InvoiceWithItems, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "header") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(invoice.invoice.invoiceNumber, style = MaterialTheme.typography.headlineSmall)
                Text(invoice.invoice.customerName?.ifBlank { "Walk-in customer" } ?: "Walk-in customer")
                Text(DateTimeUtils.formatDisplay(invoice.invoice.purchaseDateTime))
                Text("Paid with ${invoice.invoice.paymentMethod.displayName}")
                Text("Subtotal ${Money.format(invoice.invoice.subtotal)}")
                Text("Discount ${Money.format(invoice.invoice.discountTotal)}")
                Text("Total ${Money.format(invoice.invoice.totalAmount)}", style = MaterialTheme.typography.titleLarge)
            }
        }
        items(items = invoice.items, key = { it.id }) { line ->
            InvoiceLineCard(line)
        }
    }
}

@Composable
private fun InvoiceLineCard(line: InvoiceLineItemEntity) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(line.name, style = MaterialTheme.typography.titleMedium)
            Text("SKU ${line.sku}")
            Text("RFID/barcode ${line.rfidBarcode ?: "—"}")
            Text("${line.quantity} × ${Money.format(line.pricePerItem)}")
            Text("Discount ${Money.format(line.discount)}")
            Text("Line total ${Money.format(line.lineTotal)}", style = MaterialTheme.typography.labelLarge)
        }
    }
}
