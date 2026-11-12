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

package com.wanbaohe.aidetect.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wanbaohe.core.ui.widget.glass.GlassButton
import com.wanbaohe.core.ui.widget.glass.GlassCircularProgressIndicator
import com.wanbaohe.core.ui.widget.glass.GlassOutlinedTextField
import com.wanbaohe.aidetect.R
import com.wanbaohe.aidetect.component.AiDetectComponent

/** 文本检测 tab:多行输入框 + 检测按钮 + 结果卡片;[showDetectButton] 为 false 时检测按钮由底部栏提供 */
@Composable
fun TextDetectTab(
    component: AiDetectComponent,
    showDetectButton: Boolean = true,
) {
    val state by component.textState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        val result = state.result
        if (result == null) {
            GlassOutlinedTextField(
                value = state.input,
                onValueChange = component::onTextInputChange,
                modifier = Modifier.fillMaxWidth(),
                minLines = 8,
                enabled = !state.isDetecting,
                placeholder = {
                    Text(stringResource(R.string.ai_detect_text_hint))
                },
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (state.isDetecting) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    GlassCircularProgressIndicator()
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.ai_detect_detecting),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (showDetectButton) {
                GlassButton(
                    onClick = component::detectText,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state.input.isNotBlank() && !state.isDetecting,
                ) {
                    Text(stringResource(R.string.ai_detect_button))
                }
            }
        } else {
            DetectResultCard(
                probability = result.probability,
                isDetailExpanded = state.isDetailExpanded,
                onRecheck = component::resetTextDetect,
                onToggleDetail = component::toggleTextDetail,
            ) {
                TextDetectDetailContent(
                    segments = result.segments,
                    labelsRatio = result.labelsRatio,
                )
            }
        }
    }
}
