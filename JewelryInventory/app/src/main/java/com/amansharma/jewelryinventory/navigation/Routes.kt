package com.amansharma.jewelryinventory.navigation

object Routes {
    const val INVENTORY = "inventory"
    const val ADD_ITEM = "item/add"
    const val ITEM_DETAIL = "item/{itemId}"
    const val EDIT_ITEM = "item/{itemId}/edit"
    const val CHECKOUT = "checkout"
    const val INVOICES = "invoices"
    const val INVOICE_DETAIL = "invoices/{invoiceId}"
    const val SCAN = "scan"

    fun itemDetail(itemId: Long) = "item/$itemId"
    fun editItem(itemId: Long) = "item/$itemId/edit"
    fun invoiceDetail(invoiceId: Long) = "invoices/$invoiceId"
}
