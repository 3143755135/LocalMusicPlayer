package com.localmusic.player.ui.util

import java.util.Locale

/** 3:07 / 1:02:45 */
fun formatDuration(durationMs: Long): String {
    if (durationMs <= 0L) return "--:--"
    val totalSeconds = durationMs / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
    }
}

/** 用于专辑信息之类的长文本，例如 “48 分钟”。 */
fun formatDurationText(durationMs: Long): String {
    val totalMinutes = durationMs / 60_000
    return when {
        totalMinutes >= 60 -> "${totalMinutes / 60} 小时 ${totalMinutes % 60} 分钟"
        totalMinutes >= 1 -> "$totalMinutes 分钟"
        else -> "${durationMs / 1000} 秒"
    }
}

fun formatSongCount(count: Int): String = "$count 首"
