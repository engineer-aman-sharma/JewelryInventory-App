package com.amansharma.jewelryinventory.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.amansharma.jewelryinventory.ui.checkout.CheckoutScreen
import com.amansharma.jewelryinventory.ui.detail.ProductDetailScreen
import com.amansharma.jewelryinventory.ui.inventory.AddEditItemScreen
import com.amansharma.jewelryinventory.ui.inventory.InventoryListScreen
import com.amansharma.jewelryinventory.ui.invoice.InvoiceDetailScreen
import com.amansharma.jewelryinventory.ui.invoice.InvoiceListScreen
import com.amansharma.jewelryinventory.ui.scan.ScanScreen
import com.amansharma.jewelryinventory.ui.splash.SplashScreen

private data class BottomDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val bottomDestinations = listOf(
    BottomDestination(Routes.INVENTORY, "Inventory", Icons.Default.Diamond),
    BottomDestination(Routes.SCAN, "Scan", Icons.Default.QrCodeScanner),
    BottomDestination(Routes.INVOICES, "Invoices", Icons.Default.ReceiptLong)
)

private val bottomRoutes = bottomDestinations.map { it.route }.toSet()

@Composable
fun JewelryNavHost(){
    var showSplash by rememberSaveable { mutableStateOf(true) }

    if (showSplash) {
        SplashScreen(onFinished = { showSplash = false })
        return
    }

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomRoutes

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomDestinations.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    destination.icon,
                                    contentDescription = destination.label
                                )
                            },
                            label = { Text(destination.label) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
//        NavHost(
//            navController = navController,
//            startDestination = Routes.INVENTORY,
//            modifier = Modifier.padding(padding)
//        ) {
        NavHost(
            navController = navController,
            startDestination = Routes.INVENTORY,
            modifier = Modifier.padding(
                top = 0.dp,
                bottom = padding.calculateBottomPadding()
            )
        ) {
            composable(Routes.INVENTORY) {
                InventoryListScreen(
                    onAddItem = { navController.navigate(Routes.ADD_ITEM) },
                    onItemClick = { navController.navigate(Routes.itemDetail(it)) },
                    onCheckout = { navController.navigate(Routes.CHECKOUT) }
                )
            }

            composable(Routes.ADD_ITEM) {
                AddEditItemScreen(
                    onBack = { navController.popBackStack() },
                    onSaved = { itemId ->
                        navController.navigate(Routes.itemDetail(itemId)) {
                            popUpTo(Routes.INVENTORY)
                        }
                    }
                )
            }

            composable(
                route = Routes.ITEM_DETAIL,
                arguments = listOf(navArgument("itemId") { type = NavType.LongType })
            ) {
                ProductDetailScreen(
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate(Routes.editItem(it)) },
                    onCheckout = { navController.navigate(Routes.CHECKOUT) }
                )
            }

            composable(
                route = Routes.EDIT_ITEM,
                arguments = listOf(navArgument("itemId") { type = NavType.LongType })
            ) {
                AddEditItemScreen(
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }

            composable(Routes.CHECKOUT) {
                CheckoutScreen(
                    onBack = { navController.popBackStack() },
                    onSaleComplete = { invoiceId ->
                        navController.navigate(Routes.invoiceDetail(invoiceId)) {
                            popUpTo(Routes.INVENTORY)
                        }
                    }
                )
            }

            composable(Routes.INVOICES) {
                InvoiceListScreen(
                    onInvoiceClick = { navController.navigate(Routes.invoiceDetail(it)) }
                )
            }

            composable(
                route = Routes.INVOICE_DETAIL,
                arguments = listOf(navArgument("invoiceId") { type = NavType.LongType })
            ) {
                InvoiceDetailScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.SCAN) {
                ScanScreen(
                    onItemFound = { itemId ->
                        navController.navigate(Routes.itemDetail(itemId))
                    }
                )
            }
        }
    }
}