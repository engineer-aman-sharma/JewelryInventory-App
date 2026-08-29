package com.amansharma.jewelryinventory.ui.inventory

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amansharma.jewelryinventory.R
import com.amansharma.jewelryinventory.data.local.entity.InventoryItemEntity
import com.amansharma.jewelryinventory.data.model.JewelryCategory
import com.amansharma.jewelryinventory.data.model.MetalType
import com.amansharma.jewelryinventory.ui.components.EmptyState
import com.amansharma.jewelryinventory.ui.components.FullScreenLoading
import com.amansharma.jewelryinventory.ui.components.ObserveSnackbar
import com.amansharma.jewelryinventory.utils.Money
import com.amansharma.jewelryinventory.viewmodel.InventoryListUiState
import com.amansharma.jewelryinventory.viewmodel.InventoryListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryListScreen(
    onAddItem: () -> Unit,
    onItemClick: (Long) -> Unit,
    onCheckout: () -> Unit,
    viewModel: InventoryListViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    ObserveSnackbar(viewModel.snackbar) { snackbarHostState.showSnackbar(it) }

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            if (state.isSelectionMode) {
                TopAppBar(
                    title = { Text("${state.selectedIds.size} selected") },
                    navigationIcon = {
                        IconButton(onClick = viewModel::exitSelectionMode) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Exit selection"
                            )
                        }
                    },
                    actions = {
                        TextButton(
                            onClick = {
                                if (viewModel.prepareCheckout()) onCheckout()
                            }
                        ) {
                            Text("Checkout")
                        }
                    }
                )
            } else {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(R.drawable.ic_app_logo),
                                contentDescription = null,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Inventory")
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (state.isSelectionMode) {
                FloatingActionButton(
                    onClick = {
                        if (viewModel.prepareCheckout()) onCheckout()
                    }
                ) {
                    Icon(
                        Icons.Default.ShoppingCart,
                        contentDescription = "Checkout selected items"
                    )
                }
            } else {
                FloatingActionButton(onClick = onAddItem) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add item"
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        InventoryListContent(
            state = state,
            onSearchChange = viewModel::onSearchChange,
            onCategoryFilter = viewModel::onCategoryFilter,
            onMetalFilter = viewModel::onMetalFilter,
            onClearFilters = viewModel::clearFilters,
            onItemClick = { itemId ->
                if (
                    viewModel.onItemClick(itemId) ==
                    InventoryListViewModel.ItemClickResult.OpenDetail
                ) {
                    onItemClick(itemId)
                }
            },
            onItemLongClick = viewModel::onItemLongClick,
            onAddItem = onAddItem,
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
private fun InventoryListContent(
    state: InventoryListUiState,
    onSearchChange: (String) -> Unit,
    onCategoryFilter: (JewelryCategory?) -> Unit,
    onMetalFilter: (MetalType?) -> Unit,
    onClearFilters: () -> Unit,
    onItemClick: (Long) -> Unit,
    onItemLongClick: (Long) -> Unit,
    onAddItem: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        OutlinedTextField(
            value = state.query.search,
            onValueChange = onSearchChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp, vertical = 3.dp),
            label = { Text("Search name, notes, or SKU") },
            singleLine = true
        )

        val filters = buildList {
            JewelryCategory.entries.forEach { category ->
                add(
                    InventoryFilter(
                        label = category.displayName,
                        selected = state.query.category == category,
                        onClick = {
                            if (state.query.category == category) {
                                onCategoryFilter(null)
                            } else {
                                onMetalFilter(null)
                                onCategoryFilter(category)
                            }
                        }
                    )
                )
            }

            MetalType.entries.forEach { metal ->
                add(
                    InventoryFilter(
                        label = metal.displayName,
                        selected = state.query.metalType == metal,
                        onClick = {
                            if (state.query.metalType == metal) {
                                onMetalFilter(null)
                            } else {
                                onCategoryFilter(null)
                                onMetalFilter(metal)
                            }
                        }
                    )
                )
            }
        }

        val firstRowCount = (filters.size + 1) / 2

        Column(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 1.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                filters.take(firstRowCount).forEach { filter ->
                    FilterChip(
                        selected = filter.selected,
                        onClick = filter.onClick,
                        label = { Text(filter.label) }
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                filters.drop(firstRowCount).forEach { filter ->
                    FilterChip(
                        selected = filter.selected,
                        onClick = filter.onClick,
                        label = { Text(filter.label) }
                    )
                }
            }
        }

        if (state.hasActiveFilters) {
            TextButton(
                onClick = onClearFilters,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text("Clear search and filters")
            }
        }

        when {
            state.isLoading -> FullScreenLoading()

            state.errorMessage != null -> EmptyState(
                icon = Icons.Default.SearchOff,
                title = "Unable to load inventory",
                message = state.errorMessage
            )

            state.items.isEmpty() && state.hasActiveFilters -> EmptyState(
                icon = Icons.Default.SearchOff,
                title = "No matching items",
                message = "Try a different name, SKU, note, or filter."
            )

            state.items.isEmpty() -> EmptyState(
                icon = Icons.Default.Diamond,
                title = "Inventory is empty",
                message = "Add your first jewelry item to start tracking stock.",
                action = {
                    TextButton(onClick = onAddItem) {
                        Text("Add item")
                    }
                }
            )

            else -> LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 15.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = state.items,
                    key = { it.id }
                ) { item ->
                    InventoryItemCard(
                        item = item,
                        isSelectionMode = state.isSelectionMode,
                        isSelected = item.id in state.selectedIds,
                        onClick = { onItemClick(item.id) },
                        onLongClick = { onItemLongClick(item.id) }
                    )
                }
            }
        }
    }
}

private data class InventoryFilter(
    val label: String,
    val selected: Boolean,
    val onClick: () -> Unit
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun InventoryItemCard(
    item: InventoryItemEntity,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { selected = isSelected }
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onClick() }
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    item.name,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    "${item.category.displayName} • SKU ${item.sku}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        "Qty ${item.quantityInStock}",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        Money.format(item.retailPriceUsd),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}