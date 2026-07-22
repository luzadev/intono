package com.notemusicali.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

actual fun currentTimeMillis(): Long = System.currentTimeMillis()

actual fun formatEpochDate(epochMillis: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(epochMillis))
}

actual fun currentDateString(): String {
    // Machine-readable format parsed by DailyGoalManager: must stay ASCII in every locale
    val sdf = SimpleDateFormat("yyyyMMdd", Locale.US)
    return sdf.format(Date())
}
