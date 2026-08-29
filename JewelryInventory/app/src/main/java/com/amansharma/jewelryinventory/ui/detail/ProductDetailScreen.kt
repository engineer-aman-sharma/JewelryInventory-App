package com.amansharma.jewelryinventory.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amansharma.jewelryinventory.data.local.entity.InventoryItemEntity
import com.amansharma.jewelryinventory.ui.components.ConfirmDialog
import com.amansharma.jewelryinventory.ui.components.EmptyState
import com.amansharma.jewelryinventory.ui.components.ObserveSnackbar
import com.amansharma.jewelryinventory.utils.DateTimeUtils
import com.amansharma.jewelryinventory.utils.Money
import com.amansharma.jewelryinventory.viewmodel.ProductDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    onCheckout: () -> Unit,
    viewModel: ProductDetailViewModel = hiltViewModel()
) {
    val item by viewModel.item.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    ObserveSnackbar(viewModel.snackbar) {
        snackbarHostState.showSnackbar(it)
    }

    LaunchedEffect(Unit) {

        viewModel.sellReady.collect {
            onCheckout()
        }
    }
    LaunchedEffect(Unit) {

        viewModel.sellReady.collect {
            onCheckout()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Product detail") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        val current = item

        if (current == null) {
            EmptyState(
                icon = Icons.Default.SearchOff,
                title = "Item not found",
                message = "It may have been deleted.",
                modifier = Modifier.padding(padding)
            )
        } else {
            ProductDetailContent(
                item = current,
                onEdit = { onEdit(current.id) },
                onDelete = { showDeleteConfirm = true },
                onSell = viewModel::sellProduct,
                modifier = Modifier.padding(padding)
            )
        }
    }

    if (showDeleteConfirm) {
        ConfirmDialog(
            title = "Delete item?",
            message = "This removes ${item?.name ?: "the item"} from inventory. This cannot be undone.",
            confirmLabel = "Delete",
            onConfirm = {
                showDeleteConfirm = false
                viewModel.delete()
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }
}

@Composable
private fun ProductDetailContent(
    item: InventoryItemEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSell: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            item.name,
            style = MaterialTheme.typography.headlineSmall
        )

        DetailRow("SKU", item.sku)
        DetailRow("Category", item.category.displayName)
        DetailRow("Metal type", item.metalType.displayName)
        DetailRow("Carat weight", item.caratWeight?.toString() ?: "—")
        DetailRow("Quantity in stock", item.quantityInStock.toString())
        DetailRow("Cost price", Money.format(item.costPriceUsd))
        DetailRow("Retail price", Money.format(item.retailPriceUsd))
        DetailRow("Location / showcase", item.location.ifBlank { "—" })
        DetailRow("Date added", DateTimeUtils.formatDisplay(item.dateAdded))
        DetailRow("RFID / barcode", item.rfidBarcode ?: "—")
        DetailRow("Notes", item.notes ?: "—")

        Button(
            onClick = onSell,
            modifier = Modifier.fillMaxWidth(),
            enabled = item.quantityInStock > 0
        ) {
            Text("Sell product")
        }

        OutlinedButton(
            onClick = onEdit,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Edit")
        }

        OutlinedButton(
            onClick = onDelete,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Delete")
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            value,
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}