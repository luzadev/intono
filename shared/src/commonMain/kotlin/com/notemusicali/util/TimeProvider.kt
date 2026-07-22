package com.notemusicali.util

expect fun currentTimeMillis(): Long

expect fun formatEpochDate(epochMillis: Long): String

/** Returns current date as "YYYYMMDD" string. */
expect fun currentDateString(): String
