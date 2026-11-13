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

package com.wanbaohe.core.ui.widget.color_picker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.sp
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.resources.icons.ContentCopy
import com.wanbaohe.core.ui.theme.inverse
import com.wanbaohe.core.ui.utils.helper.toHex
import com.wanbaohe.core.ui.widget.enhanced.hapticsClickable
import com.wanbaohe.core.ui.widget.enhanced.hapticsCombinedClickable
import com.wanbaohe.core.ui.widget.modifier.transparencyChecker

/**
 * 大色块：整块铺满颜色，色值/名称压在色块上。
 *
 * 色彩工具里原本有三份一模一样的实现（颜色信息、颜色混合、色彩调和），
 * 取色历史改色块网格后也复用它，保证全 App 的「一块颜色」长同一个样子。
 *
 * 文字颜色按色块亮度自动反色（深色配浅字、浅色配深字），色值本身不参与
 * RTL 段落重排，否则阿拉伯语/希伯来语里 `#4F5B92` 会被 bidi 拆反。
 */
@Composable
fun LargeColorBlock(
    color: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    showCopyIcon: Boolean = true,
    primaryText: String = color.toHex(),
    secondaryText: String? = null,
    shape: Shape = MaterialTheme.shapes.medium,
    contentPadding: Dp = 8.dp,
    textStyle: TextStyle = MaterialTheme.typography.labelMedium,
    secondaryTextStyle: TextStyle = MaterialTheme.typography.labelSmall,
    badge: (@Composable BoxScope.() -> Unit)? = null,
) {
    val contentColor = color.contentColor()
    val textBackground = color.copy(alpha = 1f)

    Box(
        modifier = modifier
            .clip(shape)
            .transparencyChecker()
            .background(color)
            .colorSwatchOutline(color = color, shape = shape)
            .then(
                when {
                    onLongClick != null -> Modifier.hapticsCombinedClickable(
                        onLongClick = onLongClick,
                        onClick = { onClick?.invoke() }
                    )

                    onClick != null -> Modifier.hapticsClickable(onClick = onClick)
                    else -> Modifier
                }
            )
    ) {
        if (showCopyIcon) {
            Icon(
                imageVector = Icons.Rounded.ContentCopy,
                contentDescription = stringResource(R.string.copy),
                tint = contentColor,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(contentPadding)
                    .size(26.dp)
                    .background(
                        color = textBackground,
                        shape = MaterialTheme.shapes.medium
                    )
                    .padding(4.dp)
            )
        }

        badge?.invoke(this)

        Text(
            text = primaryText,
            color = contentColor,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(contentPadding)
                .background(
                    color = textBackground,
                    shape = MaterialTheme.shapes.medium
                )
                .padding(horizontal = 8.dp, vertical = 4.dp),
            style = textStyle.copy(textDirection = TextDirection.Ltr),
            maxLines = 1,
            // 大色块宽度随列数变化(4 色调和里一格只有 80dp),色值不能被截断,
            // 因此按可用宽度自动缩小字号而不是 ellipsis。
            autoSize = TextAutoSize.StepBased(
                minFontSize = 8.sp,
                maxFontSize = textStyle.fontSize.takeIf { it.isSpecified } ?: 12.sp,
                stepSize = 0.5.sp
            )
        )

        if (secondaryText != null) {
            Text(
                text = secondaryText,
                color = contentColor,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(contentPadding)
                    .background(
                        color = textBackground,
                        shape = MaterialTheme.shapes.medium
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                style = secondaryTextStyle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * 色块上的前景色：深色块配浅字、浅色块配深字。
 *
 * 与色彩工具原有实现同一套系数（深色混 0.8 的白、浅色混 0.5 的黑），
 * 换成这两个行为一致，不重新调。
 */
@Composable
fun Color.contentColor(): Color = inverse(
    fraction = { if (it) 0.8f else 0.5f },
    darkMode = luminance() < 0.3f
)
