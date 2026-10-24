package com.swipeclean.app.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.log10
import kotlin.math.pow

fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (log10(bytes.toDouble()) / log10(1024.0)).toInt().coerceAtMost(units.size - 1)
    val value = bytes / 1024.0.pow(digitGroups)
    return String.format(Locale.US, "%.1f %s", value, units[digitGroups])
}

/** Formats milliseconds as m:ss (e.g. 125000 -> 2:05). */
fun formatDuration(millis: Long): String {
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%d:%02d", minutes, seconds)
}

fun formatDate(epochSeconds: Long): String {
    val format = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
    return format.format(Date(epochSeconds * 1000))
}
