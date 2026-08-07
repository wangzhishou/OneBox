package com.t8rin.imagetoolbox.feature.scan_qr_code.domain

import com.shifenmiao.model.scan.ScanHistoryEntry

interface ScanHistoryRepository {
    fun loadHistory(): List<ScanHistoryEntry>
    fun loadLast(): ScanHistoryEntry?
    fun save(raw: String): ScanHistoryEntry
    fun remove(raw: String)
    fun clear()
}

