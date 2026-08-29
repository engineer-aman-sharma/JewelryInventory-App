package com.amansharma.jewelryinventory.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateTimeUtils {
    fun formatDisplay(millis: Long): String {
        val formatter = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
        return formatter.format(Date(millis))
    }

    fun formatDate(millis: Long): String {
        val formatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        return formatter.format(Date(millis))
    }

    fun compactDate(millis: Long): String {
        val formatter = SimpleDateFormat("yyyyMMdd", Locale.US)
        return formatter.format(Date(millis))
    }

    fun startOfDay(millis: Long = System.currentTimeMillis()): Long {
        return Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    fun daysAgoStart(days: Int): Long {
        return Calendar.getInstance().apply {
            timeInMillis = startOfDay()
            add(Calendar.DAY_OF_YEAR, -days)
        }.timeInMillis
    }
}
