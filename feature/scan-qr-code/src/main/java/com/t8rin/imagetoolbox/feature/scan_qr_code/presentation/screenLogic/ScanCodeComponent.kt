package com.t8rin.imagetoolbox.feature.scan_qr_code.presentation.screenLogic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.model.scan.ScanHistoryEntry
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.model.QrType
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.feature.scan_qr_code.domain.ScanCodeContentType
import com.t8rin.imagetoolbox.feature.scan_qr_code.domain.ScanCodeResultResolver
import com.t8rin.imagetoolbox.feature.scan_qr_code.domain.ScanHistoryRepository
import com.t8rin.imagetoolbox.feature.scan_qr_code.presentation.model.ScanCodeHistoryItem
import com.t8rin.imagetoolbox.feature.scan_qr_code.presentation.model.ScanCodeUiState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class ScanCodeComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigate: (Screen) -> Unit,
    dispatchersHolder: DispatchersHolder,
    private val scanHistoryRepository: ScanHistoryRepository,
    private val scanCodeResultResolver: ScanCodeResultResolver
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _state = mutableStateOf(ScanCodeUiState())
    val state by _state

    init {
        refreshState()
    }

    fun onScanned(qrType: QrType) {
        val raw = qrType.raw.trim()
        if (raw.isBlank()) return

        scanHistoryRepository.save(raw)
        refreshState()

        if (scanCodeResultResolver.classify(raw) == ScanCodeContentType.INTERNAL_DEEPLINK) {
            scanCodeResultResolver.resolveScreen(raw)?.let(onNavigate)
        }
    }

    fun openLatest() {
        state.latest?.entry?.raw?.let(::openRaw)
    }

    fun openHistoryItem(item: ScanCodeHistoryItem) {
        openRaw(item.entry.raw)
    }

    fun removeHistoryItem(item: ScanCodeHistoryItem) {
        scanHistoryRepository.remove(item.entry.raw)
        refreshState()
    }

    fun clearHistory() {
        scanHistoryRepository.clear()
        refreshState()
    }


    private fun openRaw(raw: String) {
        scanCodeResultResolver.resolveScreen(raw)?.let(onNavigate)
    }

    private fun refreshState() {
        val history = scanHistoryRepository.loadHistory()
        val latest = scanHistoryRepository.loadLast()

        _state.value = ScanCodeUiState(
            latest = latest?.toUiItem(),
            history = history
                .filterNot { latest?.isSameContent(it) == true }
                .map { it.toUiItem() }
        )
    }

    private fun ScanHistoryEntry.toUiItem(): ScanCodeHistoryItem {
        return ScanCodeHistoryItem(
            entry = this,
            type = scanCodeResultResolver.classify(raw)
        )
    }

    private fun ScanHistoryEntry.isSameContent(other: ScanHistoryEntry): Boolean {
        return raw == other.raw && scannedAtMillis == other.scannedAtMillis
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
        ): ScanCodeComponent
    }
}

