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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shifenmiao.core.R
import com.shifenmiao.model.channel.FlavorType

@Composable
fun AIContentNotice(
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    // "AI 生成内容"声明是国内合规要求,海外(google / foss)渠道不展示
    val showNotice = isVisible && !FlavorType.fromName().isOverseas
    AnimatedVisibility(
        modifier = modifier.padding(top = 20.dp, bottom = 30.dp),
        visible = showNotice,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Text(
            modifier = modifier.fillMaxWidth(),
            text = stringResource(R.string.ai_content_notice),
            maxLines = 1,
            minLines = 1,
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            ),
            textAlign = TextAlign.Center
        )
    }
}
