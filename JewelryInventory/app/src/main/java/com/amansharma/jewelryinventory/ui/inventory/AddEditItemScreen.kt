package com.amansharma.jewelryinventory.ui.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amansharma.jewelryinventory.data.model.JewelryCategory
import com.amansharma.jewelryinventory.data.model.MetalType
import com.amansharma.jewelryinventory.ui.components.FullScreenLoading
import com.amansharma.jewelryinventory.ui.components.ObserveSnackbar
import com.amansharma.jewelryinventory.viewmodel.AddEditItemViewModel
import com.amansharma.jewelryinventory.viewmodel.ItemFormState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditItemScreen(
    onBack: () -> Unit,
    onSaved: (Long) -> Unit,
    viewModel: AddEditItemViewModel = hiltViewModel()
) {
    val form by viewModel.form.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    ObserveSnackbar(viewModel.snackbar) {
        snackbarHostState.showSnackbar(it)
    }

    LaunchedEffect(Unit) {
        viewModel.saved.collect { onSaved(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (form.isEdit) "Edit item" else "Add item") },
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
        if (form.isLoading) {
            FullScreenLoading(
            )
        } else {
            ItemForm(
                state = form,
                onStateChange = viewModel::update,
                onSave = viewModel::save,
                modifier = Modifier
                    .padding(padding)
                    .imePadding()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItemForm(
    state: ItemFormState,
    onStateChange: ((ItemFormState) -> ItemFormState) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FormField(
            value = state.sku,
            onValueChange = { value ->
                onStateChange { it.copy(sku = value, skuError = null) }
            },
            label = "SKU",
            error = state.skuError
        )

        FormField(
            value = state.name,
            onValueChange = { value ->
                onStateChange { it.copy(name = value, nameError = null) }
            },
            label = "Name / title",
            error = state.nameError
        )

        EnumDropdown(
            label = "Category",
            selected = state.category.displayName,
            options = JewelryCategory.entries.map { it.displayName },
            onSelected = { display ->
                onStateChange {
                    it.copy(category = JewelryCategory.fromDisplayName(display))
                }
            }
        )

        EnumDropdown(
            label = "Metal type",
            selected = state.metalType.displayName,
            options = MetalType.entries.map { it.displayName },
            onSelected = { display ->
                onStateChange {
                    it.copy(metalType = MetalType.fromDisplayName(display))
                }
            }
        )

        FormField(
            value = state.quantity,
            onValueChange = { value ->
                onStateChange {
                    it.copy(quantity = value, quantityError = null)
                }
            },
            label = "Quantity in stock",
            error = state.quantityError,
            keyboardType = KeyboardType.Number
        )

        FormField(
            value = state.retailPrice,
            onValueChange = { value ->
                onStateChange {
                    it.copy(retailPrice = value, retailPriceError = null)
                }
            },
            label = "Retail price (USD)",
            error = state.retailPriceError,
            keyboardType = KeyboardType.Decimal
        )

        FormField(
            value = state.costPrice,
            onValueChange = { value ->
                onStateChange {
                    it.copy(costPrice = value, costPriceError = null)
                }
            },
            label = "Cost price (USD)",
            error = state.costPriceError,
            keyboardType = KeyboardType.Decimal
        )

        FormField(
            value = state.caratWeight,
            onValueChange = { value ->
                onStateChange {
                    it.copy(caratWeight = value, caratWeightError = null)
                }
            },
            label = "Carat weight (optional)",
            error = state.caratWeightError,
            keyboardType = KeyboardType.Decimal
        )

        FormField(
            value = state.location,
            onValueChange = { value ->
                onStateChange { it.copy(location = value) }
            },
            label = "Location / showcase"
        )

        FormField(
            value = state.rfidBarcode,
            onValueChange = { value ->
                onStateChange { it.copy(rfidBarcode = value) }
            },
            label = "RFID / barcode (optional)"
        )

        FormField(
            value = state.notes,
            onValueChange = { value ->
                onStateChange { it.copy(notes = value) }
            },
            label = "Notes / description (optional)",
            singleLine = false
        )

        Button(
            onClick = onSave,
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (state.isEdit) "Save changes" else "Add item")
        }
    }
}

@Composable
private fun FormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 3,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnumDropdown(
    label: String,
    selected: String,
    options: List<String>,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}