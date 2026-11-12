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

package com.wanbaohe.core.ui.widget.glass

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.wanbaohe.core.ui.utils.helper.ProvidesValue

@Composable
fun GlassTonalButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(12.dp),
    color: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    style: GlassStyle = GlassStyle.Medium,
    borderWidth: Dp = 1.dp,
    @Suppress("UNUSED_PARAMETER")
    blurRadius: Dp = 20.dp,
    colors: ButtonColors? = null,
    border: BorderStroke? = null,
    elevation: ButtonElevation? = ButtonDefaults.filledTonalButtonElevation(),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val settingsState = LocalSettingsState.current

    if (!settingsState.isGlassAlphaEnabled) {
        FilledTonalButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = shape,
            colors = colors ?: ButtonDefaults.filledTonalButtonColors(
                containerColor = color,
                contentColor = contentColor,
            ),
            elevation = elevation,
            border = border,
            contentPadding = contentPadding,
            interactionSource = interactionSource,
            content = content,
        )
        return
    }

    @Suppress("NAME_SHADOWING")
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    val resolvedContainerColor = colors.containerColor(enabled = enabled).takeIfSpecifiedOrElse(color)
    val resolvedContentColor = colors.contentColor(enabled = enabled).takeIfSpecifiedOrElse(contentColor)
    val disabledContentColor = colors.contentColor(enabled = false).takeIfSpecifiedOrElse(
        contentColor.copy(alpha = 0.38f),
    )

    LocalMinimumInteractiveComponentSize.ProvidesValue(Dp.Unspecified) {
        FilledTonalButton(
            onClick = onClick,
            modifier = modifier.glassControlStyle(
                style = style,
                backgroundAlpha = (style.backgroundAlpha + 0.03f).coerceAtMost(1f),
                shape = shape,
                color = resolvedContainerColor,
                borderWidth = borderWidth,
                enabled = enabled,
            ),
            enabled = enabled,
            shape = shape,
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = Color.Transparent,
                contentColor = resolvedContentColor,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = disabledContentColor,
            ),
            elevation = ButtonDefaults.filledTonalButtonElevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp,
                focusedElevation = 0.dp,
                hoveredElevation = 0.dp,
                disabledElevation = 0.dp,
            ),
            border = null,
            contentPadding = contentPadding,
            interactionSource = interactionSource,
            content = content,
        )
    }
}

@Stable
private fun ButtonColors?.containerColor(enabled: Boolean): Color = when {
    this == null -> Color.Unspecified
    enabled -> containerColor
    else -> disabledContainerColor
}

@Stable
private fun ButtonColors?.contentColor(enabled: Boolean): Color = when {
    this == null -> Color.Unspecified
    enabled -> contentColor
    else -> disabledContentColor
}

private fun Color.takeIfSpecifiedOrElse(fallback: Color): Color {
    return if (this != Color.Unspecified && this != Color.Transparent) this else fallback
}
