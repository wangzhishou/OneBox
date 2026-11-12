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

package com.wanbaohe.idphoto.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.model.imageprocess.RetouchParams
import com.wanbaohe.core.ui.widget.glass.GlassFilterChip
import com.wanbaohe.idphoto.R
import com.wanbaohe.idphoto.domain.BeautyLevel

/**
 * AI 人像美化面板(「一键美化」tab):
 * 档位预设只写入草稿(与各分组 tab 的单项调节同一份草稿),
 * 由底部操作栏的「应用」统一发起修图调用;
 * 实际处理走 BaiduImageProcessRepository.retouch()
 */
@Composable
fun BeautyPanel(
    beautyDraft: RetouchParams,
    isProcessing: Boolean,
    pointsCost: Int,
    onLevelSelected: (BeautyLevel) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        // 美化档位(选中态跟随草稿)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BeautyLevel.entries.forEach { level ->
                GlassFilterChip(
                    selected = beautyDraft == level.params,
                    onClick = { onLevelSelected(level) },
                    enabled = !isProcessing,
                    label = {
                        Text(text = stringResource(level.labelRes))
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(R.string.id_photo_beauty_points_tip, pointsCost),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
