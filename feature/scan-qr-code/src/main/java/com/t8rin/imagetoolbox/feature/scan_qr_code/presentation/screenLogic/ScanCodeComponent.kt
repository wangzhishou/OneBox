/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * This file was newly written for OneBox and is not derived
 * from ImageToolbox source. The com.t8rin.* package name is retained
 * only for compatibility with the existing upstream module layout.
 *
 * Origin: OneBox original work (upstream package name retained)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.t8rin.imagetoolbox.feature.scan_qr_code.presentation.screenLogic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.model.scan.ScanHistoryEntry
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.model.QrType
import com.wanbaohe.core.ui.utils.BaseComponent
import com.wanbaohe.core.ui.utils.navigation.Screen
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

