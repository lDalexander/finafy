package com.finanzas.app.core.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateFormatter {

    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val fullDateFormat = SimpleDateFormat("dd 'de' MMMM, yyyy", Locale("es", "ES"))
    private val dateOnlyFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

    fun formatDateOnly(timestamp: Long): String {
        return dateOnlyFormat.format(Date(timestamp))
    }

    fun formatTime(timestamp: Long): String {
        return timeFormat.format(Date(timestamp))
    }

    fun formatIso(timestamp: Long): String {
        return isoFormat.format(Date(timestamp))
    }

    fun formatHeaderDate(timestamp: Long): String {
        val now = Calendar.getInstance()
        val itemDate = Calendar.getInstance().apply { timeInMillis = timestamp }

        val isToday = now.get(Calendar.YEAR) == itemDate.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == itemDate.get(Calendar.DAY_OF_YEAR)

        if (isToday) return "Hoy"

        now.add(Calendar.DAY_OF_YEAR, -1)
        val isYesterday = now.get(Calendar.YEAR) == itemDate.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == itemDate.get(Calendar.DAY_OF_YEAR)

        if (isYesterday) return "Ayer"

        return fullDateFormat.format(Date(timestamp))
    }
}
