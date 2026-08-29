package com.amansharma.jewelryinventory.di

import android.content.Context
import androidx.room.Room
import com.amansharma.jewelryinventory.data.local.JewelryDatabase
import com.amansharma.jewelryinventory.data.local.dao.InventoryDao
import com.amansharma.jewelryinventory.data.local.dao.InvoiceDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): JewelryDatabase {
        return Room.databaseBuilder(
            context,
            JewelryDatabase::class.java,
            "jewelry_inventory.db"
        ).build()
    }

    @Provides
    fun provideInventoryDao(database: JewelryDatabase): InventoryDao = database.inventoryDao()

    @Provides
    fun provideInvoiceDao(database: JewelryDatabase): InvoiceDao = database.invoiceDao()
}
