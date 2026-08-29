# Jewelry Stock & Inventory Management App

A native Android application built to manage jewelry inventory, product lookup, checkout, stock updates, and invoices from a mobile device.

This project was developed as an Android Developer technical challenge for Doris Infotech, with a focus on clean architecture, local data management, practical UI, and a complete inventory-to-sale workflow.

## Features

- Add, edit, and delete inventory items
- Store product details such as SKU, category, metal type, price, quantity, and location
- Search inventory by name, notes, or SKU
- Filter products by category and metal type
- View detailed product information
- Select multiple inventory items
- Scan products using barcode / RFID
- Find products from the local database using scanned codes
- Sell products directly from product details
- Checkout with payment method selection
- Apply discounts based on payment method
- Automatically update stock after a sale
- Generate and save invoices locally
- View invoice details
- Search and filter invoices

## Tech Stack

- Kotlin
- Jetpack Compose
- Material 3
- MVVM Architecture
- Repository Pattern
- Room Database
- StateFlow
- Kotlin Coroutines
- RecyclerView
- ListAdapter
- DiffUtil

## Architecture

The application follows a structured MVVM architecture:

UI → ViewModel → Repository → Room Database

Database operations are handled asynchronously using Kotlin Coroutines, while UI state is managed reactively.

## Inventory Management

The inventory section provides a central place to manage jewelry products.

Each product can contain information such as:

- SKU / Product Code
- Item Name
- Category
- Metal Type
- Carat Weight
- Quantity
- Cost Price
- Retail Price
- Location / Showcase
- Notes
- Barcode / RFID

## Search & Filtering

Inventory can be searched using:

- Product name
- Notes / description
- SKU / product code

Additional filters are available for:

- Category
- Metal Type

## Scan & Sell Flow

The application supports a practical product lookup and selling workflow:

Scan Barcode / RFID
↓
Find Product
↓
Product Details
↓
Sell Product
↓
Checkout
↓
Confirm Sale
↓
Update Stock
↓
Generate Invoice
↓
Invoice Details

## Checkout & Invoice

The checkout screen displays selected products, quantities, prices, payment method, discounts, and the final amount.

After a successful sale:

- Sold quantity is deducted from inventory
- Inventory updates automatically
- An invoice is generated
- The invoice is stored locally
- Invoice details can be viewed later

## My Role

Designed and developed the application independently, including the UI, database structure, inventory management, search and filtering, scanning flow, checkout logic, stock updates, and invoice workflow.

## Project Focus

This project demonstrates practical Android development skills through a real-world inventory use case, with an emphasis on maintainable architecture, local persistence, reactive state management, and reliable business logic.

## Developer

**Aman Sharma**

Android Developer | Kotlin | Jetpack Compose | MVVM