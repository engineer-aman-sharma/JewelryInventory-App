package com.amansharma.jewelryinventory.utils

object InvoiceNumbers {
    fun prefixFor(nowMillis: Long): String = "INV-${DateTimeUtils.compactDate(nowMillis)}-"

    fun next(latestForToday: String?, nowMillis: Long): String {
        val prefix = prefixFor(nowMillis)
        val sequence = latestForToday
            ?.removePrefix(prefix)
            ?.toIntOrNull()
            ?: 0
        return prefix + (sequence + 1).toString().padStart(4, '0')
    }
}
