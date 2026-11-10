/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2024 T8RIN (Malik Mukhametzyanov)
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

package com.wanbaohe.core.ui.widget.buttons

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wanbaohe.core.ui.theme.suggestContainerColorBy
import com.wanbaohe.core.ui.widget.enhanced.EnhancedIconButton
import com.wanbaohe.core.ui.widget.text.AutoSizeText
import com.t8rin.imagetoolbox.core.resources.icons.CheckCircle
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCircle

@Composable
fun MediaCheckBox(
    modifier: Modifier = Modifier,
    isChecked: Boolean,
    selectionIndex: Int = -1,
    onCheck: (() -> Unit)? = null,
    checkedIcon: ImageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.CheckCircle,
    checkedColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    uncheckedColor: Color = MaterialTheme.colorScheme.onSurface,
    addContainer: Boolean = false
) {
    val image = if (isChecked) {
        checkedIcon
    } else com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCircle
    val color by animateColorAsState(
        if (isChecked) checkedColor
        else uncheckedColor
    )
    if (onCheck != null) {
        EnhancedIconButton(
            onClick = onCheck,
            modifier = modifier,
            containerColor = animateColorAsState(
                if (addContainer) MaterialTheme.colorScheme.suggestContainerColorBy(color)
                else Color.Transparent
            ).value
        ) {
            AnimatedContent(
                targetState = image,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                }
            ) { icon ->
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color
                )
            }
        }
    } else {
        AnimatedContent(
            targetState = Triple(isChecked, image, selectionIndex),
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            }
        ) { (isChecked, image, selectionIndex) ->
            if (selectionIndex >= 0) {
                if (isChecked) {
                    Box(
                        modifier = modifier
                            // 一位数是圆点, 两位数/三位数自动长成胶囊, 不会被切掉
                            .height(24.dp)
                            .widthIn(min = 24.dp)
                            .clip(CircleShape)
                            .background(color)
                            .padding(horizontal = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // 底色是 checkedColor(通常是深色的 onXxxContainer), 数字必须走亮色。
                        // 不能用可组合版 contentColorFor: 它在配色表里查不到 onXxx 这类"前景"
                        // 角色时会回落到 LocalContentColor(深色 onSurface), 于是深底配深字,
                        // 多选时的序号就几乎看不见。这里用 ColorScheme.contentColorFor(查不到
                        // 返回 Unspecified)再按底色明度兜底黑/白, 任何主题下都有对比度。
                        val counterColor = MaterialTheme.colorScheme
                            .contentColorFor(color)
                            .takeOrElse {
                                if (color.luminance() > 0.5f) Color.Black else Color.White
                            }
                        AutoSizeText(
                            text = (selectionIndex + 1).toString(),
                            color = counterColor,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                } else {
                    Icon(
                        imageVector = image,
                        modifier = modifier,
                        contentDescription = null,
                        tint = color
                    )
                }
            } else {
                Icon(
                    imageVector = image,
                    modifier = modifier,
                    contentDescription = null,
                    tint = color
                )
            }
        }
    }
}