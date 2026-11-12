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
import com.wanbaohe.core.ui.utils.navigation.Screen
import com.wanbaohe.core.ui.widget.system.OneBoxDesignSystem
import com.wanbaohe.core.ui.widget.system.OneBoxSectionCard
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
