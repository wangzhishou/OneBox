package com.shifenmiao.base.utils

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

object RateLimiter {

    private var lastClickTime: Long = 0
    private val lastCallTimes = ConcurrentHashMap<String, Long>()
    private val mutex = Mutex()

    suspend fun shouldProceed(
        functionIdentifier: String,
        minIntervalMillis: Long = TimeUnit.MINUTES.toMillis(10)
    ): Boolean = mutex.withLock {
        val currentTime = System.currentTimeMillis()
        val lastCallTime = lastCallTimes[functionIdentifier] ?: 0L
        if (currentTime - lastCallTime < minIntervalMillis) {
            false
        } else {
            lastCallTimes[functionIdentifier] = currentTime
            true
        }
    }

    fun isFastClick(): Boolean {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime < 600) {
            return true
        }
        lastClickTime = currentTime
        return false
    }
}