package com.shifenmiao.ai.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.ai.R
import com.shifenmiao.model.ai.AIChatMode
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAiChat
import com.t8rin.imagetoolbox.core.resources.icons.line.LineChatBubble
import com.wanbaohe.base.ui.DisableContainer
import com.wanbaohe.core.ui.widget.glass.GlassSegmentedButtonRow

@Composable
fun ChatModeSelector(
    currentMode: AIChatMode,
    onModeSelected: (AIChatMode) -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    enabled: Boolean = true,
) {
    DisableContainer(enabled = enabled) {
        GlassSegmentedButtonRow(
            options = AIChatMode.entries,
            selectedOption = currentMode,
            onOptionSelected = onModeSelected,
            modifier = modifier,
            buttonHeight = if (compact) 44.dp else 64.dp,
            label = { mode ->
                val title = stringResource(
                    when (mode) {
                        AIChatMode.CHAT -> R.string.ai_chat_mode_chat_title
                        AIChatMode.AGENT -> R.string.ai_chat_mode_agent_title
                    }
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = when (mode) {
                            AIChatMode.CHAT -> Icons.Outlined.LineChatBubble
                            AIChatMode.AGENT -> Icons.Outlined.LineAiChat
                        },
                        contentDescription = null,
                        modifier = Modifier.size(if (compact) 20.dp else 24.dp),
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (!compact) {
                            Text(
                                text = stringResource(
                                    when (mode) {
                                        AIChatMode.CHAT -> R.string.ai_chat_mode_chat_description
                                        AIChatMode.AGENT -> R.string.ai_chat_mode_agent_description
                                    }
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        )
    }
}
