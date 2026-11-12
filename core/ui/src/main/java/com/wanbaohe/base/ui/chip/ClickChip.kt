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

package com.wanbaohe.base.ui.chip

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.theme.AppTheme
import com.wanbaohe.core.ui.widget.system.OneBoxDesignSystem

@Composable
fun ClickChip(
    text: String,
    icon: ImageVector?,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    onClick: () -> Unit,
) {
    val shape = OneBoxDesignSystem.chipShape
    Row(
        modifier = Modifier
            .clip(shape)
            .clickable {
                onClick.invoke()
            }
            .background(
                color = colors.containerColor,
                shape = shape
            )
            .padding(
                horizontal = AppTheme.dimens.paddingSmall,
                vertical = 2.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = "Icon",
                modifier = Modifier
                    .size(12.dp)
                    .padding(end = AppTheme.dimens.paddingTooSmall),
                tint = colors.contentColor
            )
        }
        Text(
            modifier = Modifier.wrapContentWidth(),
            text = text,
            color = colors.contentColor,
            maxLines = 1,
            minLines = 1,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp
            ),
            textAlign = TextAlign.Start,
            overflow = TextOverflow.Ellipsis
        )
    }
}