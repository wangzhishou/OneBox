package com.t8rin.imagetoolbox.feature.scan_qr_code.data

import com.shifenmiao.model.scan.ScanHistoryEntry
import com.shifenmiao.storage.ScanHistoryStore
import com.t8rin.imagetoolbox.feature.scan_qr_code.domain.ScanHistoryRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class MmkvScanHistoryRepository @Inject constructor() : ScanHistoryRepository {
    override fun loadHistory(): List<ScanHistoryEntry> = ScanHistoryStore.loadHistory()

    override fun loadLast(): ScanHistoryEntry? = ScanHistoryStore.loadLast()

    override fun save(raw: String): ScanHistoryEntry = ScanHistoryStore.save(raw)

    override fun remove(raw: String) {
        ScanHistoryStore.remove(raw)
    }

    override fun clear() {
        ScanHistoryStore.clear()
    }
}

