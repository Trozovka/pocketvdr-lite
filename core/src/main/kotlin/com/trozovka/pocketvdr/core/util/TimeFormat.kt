package com.trozovka.pocketvdr.core.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

fun formatUtcTimestamp(millis: Long): String =
    Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).format(DATE_TIME_FORMATTER) + " UTC"

fun formatLocalTimestamp(millis: Long): String =
    Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).format(DATE_TIME_FORMATTER)

fun formatDurationShort(millis: Long): String {
    val totalSeconds = millis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}
