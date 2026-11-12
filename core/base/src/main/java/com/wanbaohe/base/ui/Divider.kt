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

package com.wanbaohe.base.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shifenmiao.theme.AppTheme

@Composable
fun GrayHorizontalDivider(
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
    thickness: Dp = 1.dp,
    modifier: Modifier = Modifier
) {
    HorizontalDivider(
        color = color,
        thickness = thickness,
        modifier = modifier
    )
}

@Composable
fun BoxHorizontalDivider(
    modifier: Modifier = Modifier.padding(vertical = AppTheme.dimens.paddingLarge),
    color: Color = MaterialTheme.colorScheme.outlineVariant,
    backgroundColor: Color = Color.Transparent,
    content: @Composable () -> Unit
) {
    Row(
        modifier = modifier.background(backgroundColor),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = color,
        )
        content()
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = color,
        )
    }
}