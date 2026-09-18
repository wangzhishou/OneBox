package com.wanbaohe.setting.prompt.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.core.R
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDesignSystem
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxSectionCard
import com.wanbaohe.setting.prompt.component.SystemPromptManagementComponent

@Composable
fun SystemPromptManagementScreen(
    component: SystemPromptManagementComponent,
) {
    BaseScreen(
        title = stringResource(R.string.profile_item_ai_reply_style),
        onGoBack = component.onGoBack,
        supportGlassEffect = true,
    ) {
        SystemPromptManagementContent(
            component = component,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
fun SystemPromptManagementContent(
    component: SystemPromptManagementComponent,
    modifier: Modifier = Modifier,
) {
    val systemPrompts by component.systemPrompts.collectAsState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = OneBoxDesignSystem.screenPadding),
        verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.itemSpacing),
    ) {
        Spacer(modifier = Modifier.height(OneBoxDesignSystem.microSpacing))

        systemPrompts.forEach { prompt ->
            OneBoxSectionCard(
                onClick = {
                    component.onNavigate(Screen.SystemPromptDetail(promptId = prompt.id))
                }
            ) {
                Text(
                    text = prompt.title ?: "",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = prompt.description ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
