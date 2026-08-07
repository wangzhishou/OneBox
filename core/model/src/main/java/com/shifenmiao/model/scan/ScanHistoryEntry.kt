package com.shifenmiao.model.scan

import kotlinx.serialization.Serializable

@Serializable
data class ScanHistoryEntry(
    val raw: String,
    val scannedAtMillis: Long
)

