package com.wanbaohe.chess.ui

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
import com.wanbaohe.chess.R
import com.wanbaohe.chess.application.dto.GameAiPlayerConfig
import com.wanbaohe.chess.application.port.outbound.ChessAiSource

@Composable
fun ChessOpponentPicker(
    config: GameAiPlayerConfig,
    workingEngine: AiEngine,
    allEngines: List<AiEngine>,
    modelsByProvider: Map<String, List<AiModel>>,
    onSourceSelected: (ChessAiSource) -> Unit,
    onModelSelected: (AiEngine, AiModel) -> Unit,
    onDismiss: () -> Unit,
) {
    var pickingModel by remember { mutableStateOf(false) }
    if (pickingModel) {
        AIModelsPickerBottomSheet(
            visible = true,
            allEngines = allEngines.filter {
                it.requestProtocol != AiRequestProtocol.LOCAL_ON_DEVICE && !it.requestProtocol.isNonChat
            },
            modelsByProvider = modelsByProvider,
            selectedEngineName = if (config.engineName.isNotBlank()) {
                AiEngine.buildIdentityKey(config.engineName, AiRequestProtocol.fromValue(config.engineProtocol))
            } else workingEngine.identityKey(),
            selectedModelName = config.model?.name ?: workingEngine.model.name,
            title = stringResource(R.string.chess_settings_pick_ai_model),
            onSelected = { engine, model -> onModelSelected(engine, model); onDismiss() },
            onDismiss = { selected -> if (!selected) pickingModel = false },
        )
    } else {
        val sources = ChessAiSource.presets
        val items = sources.map { source ->
            when (source) {
                ChessAiSource.WorkingModel -> AiPickerItem(
                    title = stringResource(R.string.chess_ai_source_working_model),
                    subtitle = (config.model ?: workingEngine.model).let { it.title.ifBlank { it.name } },
                    tags = listOf(
                        AiSourceTag(stringResource(R.string.chess_ai_tag_login), Color(0xFFF08A5D)),
                        AiSourceTag(stringResource(R.string.chess_ai_tag_points), Color(0xFF4F46E5)),
                    ),
                )
                is ChessAiSource.RemoteEngine -> AiPickerItem(
                    title = stringResource(R.string.chess_ai_source_engine_name),
                    subtitle = stringResource(R.string.chess_ai_source_engine_desc),
                    tags = listOf(AiSourceTag(stringResource(R.string.chess_ai_tag_free), Color(0xFF3D8B7A))),
                )
            }
        }
        AiPickerBottomSheet(
            visible = true,
            title = stringResource(R.string.chess_settings_ai_picker_title),
            description = stringResource(R.string.chess_ai_picker_desc),
            items = items,
            selectedItem = config.takeIf { it.isSupported }?.let { items.getOrNull(sources.indexOf(it.source)) },
            onSelected = { item ->
                val source = sources[items.indexOf(item)]
                if (source == ChessAiSource.WorkingModel) pickingModel = true
                else { onSourceSelected(source); onDismiss() }
            },
            onDismiss = { if (!pickingModel) onDismiss() },
        )
    }
}
