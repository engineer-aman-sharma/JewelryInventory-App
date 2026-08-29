package com.amansharma.jewelryinventory.data.local

import androidx.room.TypeConverter
import com.amansharma.jewelryinventory.data.model.JewelryCategory
import com.amansharma.jewelryinventory.data.model.MetalType
import com.amansharma.jewelryinventory.data.model.PaymentMethod

class Converters {
    @TypeConverter
    fun fromCategory(value: JewelryCategory): String = value.name

    @TypeConverter
    fun toCategory(value: String): JewelryCategory = JewelryCategory.valueOf(value)

    @TypeConverter
    fun fromMetalType(value: MetalType): String = value.name

    @TypeConverter
    fun toMetalType(value: String): MetalType = MetalType.valueOf(value)

    @TypeConverter
    fun fromPaymentMethod(value: PaymentMethod): String = value.name

    @TypeConverter
    fun toPaymentMethod(value: String): PaymentMethod = PaymentMethod.valueOf(value)
}
