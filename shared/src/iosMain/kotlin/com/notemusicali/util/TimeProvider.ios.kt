package com.notemusicali.util

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.timeIntervalSince1970

actual fun currentTimeMillis(): Long =
    (NSDate().timeIntervalSince1970 * 1000).toLong()

actual fun formatEpochDate(epochMillis: Long): String {
    val formatter = NSDateFormatter()
    formatter.dateFormat = "dd/MM/yyyy HH:mm"
    // NSDate reference date is 2001-01-01, Unix epoch is 1970-01-01
    // Difference: 978307200 seconds
    val secondsSince1970 = epochMillis / 1000.0
    val secondsSinceRef = secondsSince1970 - 978307200.0
    val date = NSDate(timeIntervalSinceReferenceDate = secondsSinceRef)
    return formatter.stringFromDate(date)
}

actual fun currentDateString(): String {
    val formatter = NSDateFormatter()
    // Machine-readable format parsed by DailyGoalManager: must stay ASCII (and
    // Gregorian) regardless of the device locale/calendar
    formatter.locale = NSLocale(localeIdentifier = "en_US_POSIX")
    formatter.dateFormat = "yyyyMMdd"
    return formatter.stringFromDate(NSDate())
}
