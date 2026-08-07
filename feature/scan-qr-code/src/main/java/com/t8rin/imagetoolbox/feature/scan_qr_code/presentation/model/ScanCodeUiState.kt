package com.t8rin.imagetoolbox.feature.scan_qr_code.presentation.model

import com.shifenmiao.model.scan.ScanHistoryEntry
import com.t8rin.imagetoolbox.feature.scan_qr_code.domain.ScanCodeContentType

data class ScanCodeHistoryItem(
    val entry: ScanHistoryEntry,
    val type: ScanCodeContentType
)

data class ScanCodeUiState(
    val latest: ScanCodeHistoryItem? = null,
    val history: List<ScanCodeHistoryItem> = emptyList()
)

