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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import com.wanbaohe.base.ui.shapes.ChatBubbleShape
import com.wanbaohe.base.ui.shapes.LeftChatBubbleShape
import com.wanbaohe.core.ui.widget.glass.GlassCard

@Composable
fun CustomChatCard(
    isHuman: Boolean,
    showAvatar: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = cardShapeFor(isHuman, showAvatar)
    GlassCard(
        modifier = modifier.fillMaxWidth().clip(shape).clickable {
            onClick.invoke()
        },
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = when {
                isHuman -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surfaceContainerLow
            },
            contentColor = when {
                isHuman -> MaterialTheme.colorScheme.onPrimaryContainer
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        ),
        content = content
    )
}

@Composable
fun cardShapeFor(isHuman: Boolean, showAvatar: Boolean): Shape {
    if (showAvatar) {
        return when {
            isHuman -> ChatBubbleShape()
            else -> LeftChatBubbleShape()
        }
    } else {
        val roundedCorners = MaterialTheme.shapes.medium
        return when {
            isHuman -> roundedCorners.copy(bottomEnd = CornerSize(0))
            else -> roundedCorners.copy(bottomStart = CornerSize(0))
        }
    }
}