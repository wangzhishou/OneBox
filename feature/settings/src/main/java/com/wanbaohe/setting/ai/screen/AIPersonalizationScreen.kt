package com.wanbaohe.setting.ai.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.ui.BaseScreen
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDesignSystem
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxGroupDivider
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxLeadingIconBadge
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxSectionCard
import com.shifenmiao.core.R as CoreR
import com.t8rin.imagetoolbox.core.resources.icons.line.LineChevronRight
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMemory
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePrompt
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePsychology

// 「提示词与个性化」聚合入口: 系统提示词 / AI 记忆 / AI 技能 三个独立页面的跳转列表
@Composable
fun AIPersonalizationScreen(
    onGoBack: () -> Unit,
    onNavigate: (Screen) -> Unit,
) {
    BaseScreen(
        title = stringResource(CoreR.string.profile_item_ai_personalization),
        onGoBack = onGoBack,
        supportGlassEffect = true,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = OneBoxDesignSystem.screenPadding),
            verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.itemSpacing),
        ) {
            Spacer(modifier = Modifier.height(OneBoxDesignSystem.microSpacing))

            OneBoxSectionCard {
                PersonalizationRow(
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePrompt,
                    title = stringResource(CoreR.string.profile_item_ai_reply_style),
                    onClick = { onNavigate(Screen.SystemPromptManagement) },
                )
                OneBoxGroupDivider()
                PersonalizationRow(
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMemory,
                    title = stringResource(CoreR.string.profile_item_ai_memory),
                    onClick = { onNavigate(Screen.MemoryManagement) },
                )
                OneBoxGroupDivider()
                PersonalizationRow(
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePsychology,
                    title = stringResource(CoreR.string.profile_item_ai_skill),
                    onClick = { onNavigate(Screen.SkillManagement) },
                )
            }

            Spacer(modifier = Modifier.height(OneBoxDesignSystem.sectionSpacing))
        }
    }
}

@Composable
private fun PersonalizationRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(OneBoxDesignSystem.sectionCardShape)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.itemSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OneBoxLeadingIconBadge(icon = icon)
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Icon(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineChevronRight,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
