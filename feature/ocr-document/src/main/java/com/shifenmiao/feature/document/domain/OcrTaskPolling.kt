package com.shifenmiao.feature.document.domain

import kotlin.math.ceil
import kotlin.math.max

const val OCR_TASK_POLL_INTERVAL_MS: Long = 30_000L

fun shouldPoll(updatedAt: Long, nowMillis: Long, intervalMs: Long = OCR_TASK_POLL_INTERVAL_MS): Boolean {
    return nowMillis - updatedAt >= intervalMs
}

fun nextPollInSeconds(updatedAt: Long, nowMillis: Long, intervalMs: Long = OCR_TASK_POLL_INTERVAL_MS): Int {
    val remainingMs = (updatedAt + intervalMs) - nowMillis
    return max(0, ceil(remainingMs / 1000.0).toInt())
}

