package com.wanbaohe.gomoku.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.shifenmiao.common.ui.ai.AIModelsPickerBottomSheet
import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.model.ai.AiModel
import com.shifenmiao.model.ai.AiRequestProtocol
import com.wanbaohe.boardgame.model.AiPickerItem
import com.wanbaohe.boardgame.model.AiSourceTag
import com.wanbaohe.boardgame.ui.AiPickerBottomSheet
import com.wanbaohe.gomoku.R
import com.wanbaohe.gomoku.application.dto.GameAiPlayerConfig
import com.wanbaohe.gomoku.application.port.outbound.GomokuAiSource

@Composable
fun GomokuOpponentPicker(
    config: GameAiPlayerConfig,
    workingEngine: AiEngine,
    allEngines: List<AiEngine>,
    modelsByProvider: Map<String, List<AiModel>>,
    onSourceSelected: (GomokuAiSource) -> Unit,
    onModelSelected: (AiEngine, AiModel) -> Unit,
    onDismiss: () -> Unit,
) {
    var pickingModel by remember { mutableStateOf(false) }
    if (pickingModel) {
        AIModelsPickerBottomSheet(
            visible = true,
            allEngines = allEngines.filter { !it.requestProtocol.isNonChat && it.requestProtocol != AiRequestProtocol.LOCAL_ON_DEVICE },
            modelsByProvider = modelsByProvider,
            selectedEngineName = if (config.engineName.isNotBlank())
                AiEngine.buildIdentityKey(config.engineName, AiRequestProtocol.fromValue(config.engineProtocol))
            else workingEngine.identityKey(),
            selectedModelName = config.model?.name ?: workingEngine.model.name,
            title = stringResource(R.string.gomoku_pick_chat_model),
            onSelected = { engine, model -> onModelSelected(engine, model); onDismiss() },
            onDismiss = { selected -> if (!selected) pickingModel = false },
        )
    } else {
        val sources = GomokuAiSource.presets
        val items = sources.map { source ->
            when (source) {
                GomokuAiSource.WorkingModel -> AiPickerItem(
                    title = stringResource(R.string.gomoku_pick_chat_model),
                    subtitle = config.model?.let { it.title.ifBlank { it.name } }.orEmpty(),
                    tags = listOf(AiSourceTag(stringResource(R.string.gomoku_ai_tag_login), Color(0xFFF08A5D))),
                )
                is GomokuAiSource.RemoteEngine -> AiPickerItem(
                    title = if (source.engineId == GomokuAiSource.RemoteEngine.RAPFI)
                        stringResource(R.string.gomoku_ai_source_engine_name) else source.engineId,
                    subtitle = stringResource(R.string.gomoku_ai_source_engine_desc),
                    tags = listOf(AiSourceTag(stringResource(R.string.gomoku_ai_tag_free), Color(0xFF3D8B7A))),
                )
            }
        }
        AiPickerBottomSheet(
            visible = true,
            title = stringResource(R.string.gomoku_settings_ai_picker_title),
            description = stringResource(R.string.gomoku_ai_picker_desc),
            items = items,
            selectedItem = config.takeIf { it.isSupported }?.let { items.getOrNull(sources.indexOf(it.source)) },
            onSelected = { item ->
                val source = sources[items.indexOf(item)]
                if (source == GomokuAiSource.WorkingModel) pickingModel = true
                else onSourceSelected(source)
            },
            // The shared source picker dismisses after selection; keep this dialog
            // alive while moving to the concrete provider/model picker.
            onDismiss = { if (!pickingModel) onDismiss() },
        )
    }
}
