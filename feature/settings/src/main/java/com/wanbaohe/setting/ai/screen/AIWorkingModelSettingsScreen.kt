package com.wanbaohe.setting.ai.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.common.ui.ai.AIModelsPickerBottomSheet
import com.shifenmiao.common.ui.ai.EngineFilterChip
import com.shifenmiao.model.ai.AiEngine
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDesignSystem
import com.wanbaohe.setting.ai.component.AIWorkingModelSettingsComponent
import com.wanbaohe.settings.R
import com.shifenmiao.core.R as CoreR

@Composable
fun AIWorkingModelSettingsScreen(
    component: AIWorkingModelSettingsComponent,
) {
    val allEngines by component.allEngines.collectAsState()
    val currentAIEngine by component.currentAIEngine.collectAsState()
    val fastAIEngine by component.fastAIEngine.collectAsState()
    val duelEngineA by component.duelEngineA.collectAsState()
    val duelEngineB by component.duelEngineB.collectAsState()
    val modelsByProvider by component.modelsByProvider.collectAsState()
    var selectingSlot by remember { mutableStateOf<AIWorkingModelSettingsComponent.WorkingModelSlot?>(null) }

    val slotEngine = when (selectingSlot) {
        AIWorkingModelSettingsComponent.WorkingModelSlot.DEFAULT -> currentAIEngine
        AIWorkingModelSettingsComponent.WorkingModelSlot.FAST -> fastAIEngine
        AIWorkingModelSettingsComponent.WorkingModelSlot.DUEL_A -> duelEngineA
        AIWorkingModelSettingsComponent.WorkingModelSlot.DUEL_B -> duelEngineB
        null -> null
    }

    if (selectingSlot != null && slotEngine != null) {
        val selectedEngine = slotEngine
        val selectedSlot = selectingSlot!!
        AIModelsPickerBottomSheet(
            visible = true,
            allEngines = allEngines,
            modelsByProvider = modelsByProvider,
            selectedEngineName = selectedEngine.identityKey(),
            selectedModelName = selectedEngine.model.name,
            title = stringResource(R.string.ai_working_model_picker_title, selectedEngine.title),
            onSelected = { engine, model ->
                component.switchModel(selectedSlot, engine, model)
                selectingSlot = null
            },
            onDismiss = { selectingSlot = null },
        )
    }

    BaseScreen(
        title = stringResource(CoreR.string.profile_item_ai_workflow_models),
        onGoBack = component.onGoBack,
        supportGlassEffect = true,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = OneBoxDesignSystem.screenPadding),
            verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.blockSpacing),
        ) {
            Spacer(modifier = Modifier.height(OneBoxDesignSystem.microSpacing))
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(OneBoxDesignSystem.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
                ) {
                    Text(
                        text = stringResource(R.string.ai_working_model_settings_heading),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(R.string.ai_working_model_settings_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            WorkingEngineSummaryCard(
                title = stringResource(R.string.ai_working_model_default_title),
                description = stringResource(R.string.ai_working_model_default_desc),
                engine = currentAIEngine,
                onClick = { selectingSlot = AIWorkingModelSettingsComponent.WorkingModelSlot.DEFAULT }
            )

            WorkingEngineSummaryCard(
                title = stringResource(R.string.ai_working_model_fast_title),
                description = stringResource(R.string.ai_working_model_fast_desc),
                engine = fastAIEngine,
                onClick = { selectingSlot = AIWorkingModelSettingsComponent.WorkingModelSlot.FAST }
            )

            WorkingEngineSummaryCard(
                title = stringResource(R.string.ai_working_model_duel_a_title),
                description = stringResource(R.string.ai_working_model_duel_a_desc),
                engine = duelEngineA,
                onClick = { selectingSlot = AIWorkingModelSettingsComponent.WorkingModelSlot.DUEL_A }
            )

            WorkingEngineSummaryCard(
                title = stringResource(R.string.ai_working_model_duel_b_title),
                description = stringResource(R.string.ai_working_model_duel_b_desc),
                engine = duelEngineB,
                onClick = { selectingSlot = AIWorkingModelSettingsComponent.WorkingModelSlot.DUEL_B }
            )
        }
    }
}

@Composable
private fun WorkingEngineSummaryCard(
    title: String,
    description: String,
    engine: AiEngine,
    onClick: () -> Unit,
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(OneBoxDesignSystem.cardPadding),
            verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.itemSpacing),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.microSpacing)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.microSpacing)) {
                    Text(
                        text = stringResource(R.string.ai_working_model_current_engine, engine.title),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(R.string.ai_working_model_current_model, engine.model.title.ifBlank { engine.model.name }),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = stringResource(
                            when {
                                engine.canChatDirectly() -> R.string.ai_working_model_status_direct
                                engine.hasProxyRouteConfigured() -> R.string.ai_working_model_status_proxy
                                else -> R.string.ai_working_model_status_unavailable
                            }
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (engine.hasAvailableChatRoute()) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                    )
                }
                EngineFilterChip(
                    text = stringResource(R.string.ai_working_model_change),
                    isSelected = false,
                    onClick = onClick,
                )
            }
        }
    }
}

