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

package com.wanbaohe.core.ui.widget.modifier

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.isUnspecified
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.takeOrElse
import com.wanbaohe.core.ui.utils.provider.LocalContainerShape
import com.wanbaohe.core.ui.utils.provider.SafeLocalContainerColor
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.core.ui.widget.glass.glassBackground

@Composable
fun Modifier.flatGlassContainer(
    shape: Shape = ShapeDefaults.default,
    color: Color = Color.Unspecified,
    resultPadding: Dp = 4.dp,
    borderWidth: Dp = 0.dp,
    borderColor: Color? = null,
    @Suppress("UNUSED_PARAMETER") autoShadowElevation: Dp = 0.dp,
    clip: Boolean = true,
    composeColorOnTopOfBackground: Boolean = true,
    @Suppress("UNUSED_PARAMETER") isShadowClip: Boolean = false,
    @Suppress("UNUSED_PARAMETER") isStandaloneContainer: Boolean = true,
    @Suppress("UNUSED_PARAMETER") shadowColor: Color = Color.Black,
    style: GlassStyle = GlassStyle.Regular,
) = this.then(
    Modifier.run {
        val resultShape = LocalContainerShape.current ?: shape
        val colorScheme = MaterialTheme.colorScheme
        val containerColor = if (color.isUnspecified) {
            SafeLocalContainerColor
        } else {
            if (composeColorOnTopOfBackground) color.compositeOver(colorScheme.background)
            else color
        }
        val resolvedBorderWidth = borderWidth.takeOrElse { 0.dp }

        Modifier
            .glassBackground(
                style = style,
                shape = resultShape,
                color = containerColor,
                borderWidth = if (borderColor != null) 0.dp else resolvedBorderWidth,
            )
            .then(
                if (borderColor != null && resolvedBorderWidth > 0.dp) {
                    Modifier.border(resolvedBorderWidth, borderColor, resultShape)
                } else {
                    Modifier
                }
            )
            .then(if (clip) Modifier.clip(resultShape) else Modifier)
            .then(if (resultPadding > 0.dp) Modifier.padding(resultPadding) else Modifier)
    }
)
