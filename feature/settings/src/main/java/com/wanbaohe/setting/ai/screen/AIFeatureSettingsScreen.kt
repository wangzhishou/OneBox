package com.wanbaohe.setting.ai.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSwitch
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDesignSystem
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxGroupDivider
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxLeadingIconBadge
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxNumberStepperField
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxSectionCard
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxSectionHeader
import com.wanbaohe.setting.ai.component.AIFeatureSettingsComponent
import com.wanbaohe.settings.R
import com.shifenmiao.core.R as CoreR
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRobot
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.t8rin.imagetoolbox.core.resources.icons.line.LineBuild
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRecordVoiceOver
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSettingsSuggest
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMessage

@Composable
fun AIFeatureSettingsScreen(
    component: AIFeatureSettingsComponent,
) {
    val isConversationTitleSummaryEnabled by component.isConversationTitleSummaryEnabled.collectAsState()
    val isExpandedReasoningChat by component.isExpandedReasoningChat.collectAsState()
    val isExpandedPrompt by component.isExpandedPrompt.collectAsState()
    val isExpandedToolCall by component.isExpandedToolCall.collectAsState()
    val maxAgentIterations by component.maxAgentIterations.collectAsState()

    BaseScreen(
        title = stringResource(CoreR.string.profile_item_ai_feature_settings),
        onGoBack = component.onGoBack,
        supportGlassEffect = true,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = OneBoxDesignSystem.screenPadding),
            verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.blockSpacing),
        ) {
            Spacer(modifier = Modifier.height(OneBoxDesignSystem.microSpacing))

            OneBoxSectionCard {
                OneBoxSectionHeader(
                    title = stringResource(R.string.ai_feature_settings_heading),
                    supporting = stringResource(R.string.ai_feature_settings_desc),
                )
            }

            OneBoxSectionCard {
                OneBoxSectionHeader(
                    title = stringResource(R.string.ai_feature_settings_toggle_section_title),
                    supporting = stringResource(R.string.ai_feature_settings_toggle_section_desc),
                )
                AIFeatureToggleRow(
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineRobot,
                    title = stringResource(R.string.ai_feature_settings_title_summary_toggle_title),
                    description = stringResource(R.string.ai_feature_settings_title_summary_toggle_desc),
                    checked = isConversationTitleSummaryEnabled,
                    onCheckedChange = component::updateConversationTitleSummaryEnabled,
                )
                OneBoxGroupDivider()
                AIFeatureToggleRow(
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineRecordVoiceOver,
                    title = stringResource(R.string.ai_feature_settings_reasoning_expand_toggle_title),
                    description = stringResource(R.string.ai_feature_settings_reasoning_expand_toggle_desc),
                    checked = isExpandedReasoningChat,
                    onCheckedChange = component::updateExpandedReasoningChat,
                )
                OneBoxGroupDivider()
                AIFeatureToggleRow(
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMessage,
                    title = stringResource(R.string.ai_feature_settings_prompt_expand_toggle_title),
                    description = stringResource(R.string.ai_feature_settings_prompt_expand_toggle_desc),
                    checked = isExpandedPrompt,
                    onCheckedChange = component::updateExpandedPrompt,
                )
                OneBoxGroupDivider()
                AIFeatureToggleRow(
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineBuild,
                    title = stringResource(R.string.ai_feature_settings_tool_call_expand_toggle_title),
                    description = stringResource(R.string.ai_feature_settings_tool_call_expand_toggle_desc),
                    checked = isExpandedToolCall,
                    onCheckedChange = component::updateExpandedToolCall,
                )
            }

            OneBoxSectionCard {
                OneBoxSectionHeader(
                    title = stringResource(R.string.ai_feature_settings_agent_section_title),
                    supporting = stringResource(R.string.ai_feature_settings_agent_section_desc),
                )
                AIFeatureStepperRow(
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSettingsSuggest,
                    title = stringResource(R.string.ai_feature_settings_max_iterations_title),
                    description = stringResource(
                        R.string.ai_feature_settings_max_iterations_desc,
                        component.maxAgentIterationsRange.first,
                        component.maxAgentIterationsRange.last,
                    ),
                    value = maxAgentIterations,
                    valueRange = component.maxAgentIterationsRange,
                    onValueChange = component::updateMaxAgentIterations,
                    decrementContentDescription = stringResource(
                        R.string.ai_feature_settings_max_iterations_decrement
                    ),
                    incrementContentDescription = stringResource(
                        R.string.ai_feature_settings_max_iterations_increment
                    ),
                )
            }

            Spacer(modifier = Modifier.height(OneBoxDesignSystem.sectionSpacing))
        }
    }
}

@Composable
private fun AIFeatureToggleRow(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.itemSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OneBoxLeadingIconBadge(icon = icon)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.microSpacing),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        GlassSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            thumbContent = {
                if (checked) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check,
                        contentDescription = null,
                        modifier = Modifier.size(SwitchDefaults.IconSize),
                    )
                }
            },
            colors = AppTheme.colors.switchColors(),
        )
    }
}

@Composable
private fun AIFeatureStepperRow(
    icon: ImageVector,
    title: String,
    description: String,
    value: Int,
    valueRange: IntRange,
    onValueChange: (Int) -> Unit,
    decrementContentDescription: String,
    incrementContentDescription: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.itemSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OneBoxLeadingIconBadge(icon = icon)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.microSpacing),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        OneBoxNumberStepperField(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            decrementContentDescription = decrementContentDescription,
            incrementContentDescription = incrementContentDescription,
        )
    }
}


