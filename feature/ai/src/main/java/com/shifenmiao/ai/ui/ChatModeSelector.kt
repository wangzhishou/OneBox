/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
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
