package com.amansharma.jewelryinventory.ui.checkout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.RemoveShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amansharma.jewelryinventory.data.model.PaymentMethod
import com.amansharma.jewelryinventory.ui.components.EmptyState
import com.amansharma.jewelryinventory.ui.components.ObserveSnackbar
import com.amansharma.jewelryinventory.utils.Money
import com.amansharma.jewelryinventory.utils.PricedCheckoutLine
import com.amansharma.jewelryinventory.viewmodel.CheckoutUiState
import com.amansharma.jewelryinventory.viewmodel.CheckoutViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    onBack: () -> Unit,
    onSaleComplete: (Long) -> Unit,
    viewModel: CheckoutViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    ObserveSnackbar(viewModel.snackbar) {
        snackbarHostState.showSnackbar(it)
    }

    LaunchedEffect(Unit) {
        viewModel.completedInvoiceId.collect(onSaleComplete)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout") },
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
        if (state.isEmpty) {
            EmptyState(
                icon = Icons.Default.RemoveShoppingCart,
                title = "Nothing to check out",
                message = "Select products from inventory or sell from product detail.",
                modifier = Modifier.padding(padding)
            )
        } else {
            CheckoutContent(
                state = state,
                onPaymentMethodChange = viewModel::onPaymentMethodChange,
                onCustomerNameChange = viewModel::onCustomerNameChange,
                onQuantityChange = viewModel::onQuantityChange,
                onRemove = viewModel::removeItem,
                onConfirm = viewModel::confirmSale,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun CheckoutContent(
    state: CheckoutUiState,
    onPaymentMethodChange: (PaymentMethod) -> Unit,
    onCustomerNameChange: (String) -> Unit,
    onQuantityChange: (Long, Int) -> Unit,
    onRemove: (Long) -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = state.totals.lines,
                key = { it.item.id }
            ) { line ->
                CheckoutLineCard(
                    line = line,
                    stockError = state.stockErrors[line.item.id],
                    onQuantityChange = { onQuantityChange(line.item.id, it) },
                    onRemove = { onRemove(line.item.id) }
                )
            }

            item(key = "summary") {
                CheckoutSummaryCard(
                    state = state,
                    onPaymentMethodChange = onPaymentMethodChange,
                    onCustomerNameChange = onCustomerNameChange
                )
            }
        }

        Button(
            onClick = onConfirm,
            enabled = state.canConfirm,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                if (state.isConfirming) "Confirming…" else "Confirm sale"
            )
        }
    }
}

@Composable
private fun CheckoutLineCard(
    line: PricedCheckoutLine,
    stockError: String?,
    onQuantityChange: (Int) -> Unit,
    onRemove: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                line.item.name,
                style = MaterialTheme.typography.titleMedium
            )
            Text("SKU ${line.item.sku} • ${line.item.category.displayName}")
            Text(
                "Unit ${Money.format(line.unitPrice)}  •  " +
                        "Discount ${Money.format(line.discountAmount)}"
            )
            Text(
                "Line total ${Money.format(line.lineTotal)}",
                style = MaterialTheme.typography.labelLarge
            )

            if (stockError != null) {
                Text(
                    stockError,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(
                    onClick = { onQuantityChange(line.quantity - 1) },
                    enabled = line.quantity > 1
                ) {
                    Text("−")
                }

                Text("Qty ${line.quantity} / ${line.item.quantityInStock}")

                TextButton(
                    onClick = { onQuantityChange(line.quantity + 1) },
                    enabled = line.quantity < line.item.quantityInStock
                ) {
                    Text("+")
                }

                TextButton(onClick = onRemove) {
                    Text("Remove")
                }
            }
        }
    }
}

@Composable
private fun CheckoutSummaryCard(
    state: CheckoutUiState,
    onPaymentMethodChange: (PaymentMethod) -> Unit,
    onCustomerNameChange: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "Payment method",
                style = MaterialTheme.typography.titleMedium
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PaymentMethod.entries.forEach { method ->
                    FilterChip(
                        selected = state.paymentMethod == method,
                        onClick = { onPaymentMethodChange(method) },
                        label = { Text(method.displayName) }
                    )
                }
            }

            if (state.paymentMethod == PaymentMethod.ZELLE_WIRE) {
                Text(
                    "Zelle/Wire: 20% off watches, 5% off other jewelry.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            OutlinedTextField(
                value = state.customerName,
                onValueChange = onCustomerNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Customer name (optional)") },
                singleLine = true
            )

            Text("Subtotal ${Money.format(state.totals.subtotal)}")
            Text("Discount ${Money.format(state.totals.discountTotal)}")
            Text(
                "Total ${Money.format(state.totals.grandTotal)}",
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}