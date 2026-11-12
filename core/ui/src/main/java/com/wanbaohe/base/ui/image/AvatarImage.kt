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

package com.wanbaohe.base.ui.image

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import com.wanbaohe.base.ui.painter.TextPainter
import com.wanbaohe.base.ui.painter.TextPainterNoBg
import com.shifenmiao.theme.AppTheme

@Composable
fun AvatarImage(
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    textColor: Color,
    text: String,
    size: Dp = AppTheme.dimens.cardIconSize,
    shape: Shape = RoundedCornerShape(AppTheme.dimens.cornerRadiusNormal)
) {
    val textMeasurer = rememberTextMeasurer()
    val avatarText = remember(backgroundColor, text, textColor) {
        TextPainter(
            backgroundColor = backgroundColor,
            textMeasurer = textMeasurer,
            text = text,
            textColor = textColor,
            size = Size(size.value, size.value)
        )
    }
    Image(
        modifier = modifier
            .size(size)
            .clip(shape),
        painter = avatarText,
        contentDescription = null
    )
}

@Composable
fun AvatarImage(
    modifier: Modifier = Modifier,
    textColor: Color,
    text: String,
    alpha: Float = DefaultAlpha,
    textSize: Dp = AppTheme.dimens.cardIconSize,
    shape: Shape = RoundedCornerShape(AppTheme.dimens.cornerRadiusNormal)
) {
    val textMeasurer = rememberTextMeasurer()
    val avatarText = remember(text, textColor) {
        TextPainterNoBg(
            textMeasurer = textMeasurer,
            text = text,
            textColor = textColor,
            size = Size(textSize.value, textSize.value)
        )
    }
    Image(
        modifier = modifier.clip(shape),
        painter = avatarText,
        alpha = alpha,
        contentDescription = null
    )
}