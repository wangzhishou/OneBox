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

package com.wanbaohe.core.ui.widget.color_picker

import com.wanbaohe.core.ui.widget.glass.GlassOutlinedTextField
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.smarttoolfactory.colordetector.util.ColorUtil.colorToHex
import com.smarttoolfactory.colordetector.util.ColorUtil.colorToHexAlpha
import com.smarttoolfactory.colordetector.util.HexUtil
import com.smarttoolfactory.colorpicker.util.HexVisualTransformation
import com.smarttoolfactory.colorpicker.util.hexRegexSingleChar
import com.smarttoolfactory.colorpicker.util.hexWithAlphaRegex
import com.t8rin.imagetoolbox.core.resources.R
import com.wanbaohe.core.ui.theme.inverse
import com.wanbaohe.core.ui.utils.helper.Clipboard
import com.wanbaohe.core.ui.utils.helper.ContextUtils.pasteColorFromClipboard
import com.wanbaohe.core.ui.widget.enhanced.EnhancedAlertDialog
import com.wanbaohe.core.ui.widget.enhanced.EnhancedButton
import com.wanbaohe.core.ui.widget.enhanced.EnhancedIconButton
import com.wanbaohe.core.ui.widget.enhanced.hapticsClickable
import com.wanbaohe.core.ui.widget.modifier.ShapeDefaults
import com.wanbaohe.core.ui.widget.modifier.container
import com.wanbaohe.core.ui.widget.modifier.transparencyChecker
import com.wanbaohe.core.ui.widget.text.AutoSizeText
import kotlinx.coroutines.delay
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTheme
import com.t8rin.imagetoolbox.core.resources.icons.ContentCopy
import com.t8rin.imagetoolbox.core.resources.icons.Delete
import com.t8rin.imagetoolbox.core.resources.icons.line.LineContentPaste
import com.t8rin.imagetoolbox.core.resources.icons.line.LineShuffle

@Composable
fun ColorInfo(
    color: Color,
    onColorChange: (Color) -> Unit,
    onSupportButtonClick: () -> Unit = {
        onColorChange(
            Color(Random.nextInt()).copy(alpha = color.alpha)
        )
    },
    supportButtonIcon: ImageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineShuffle,
    modifier: Modifier = Modifier,
    infoContainerColor: Color = Color.Unspecified,
    onRemove: (() -> Unit)? = null,
    removeContentDescription: String? = null,
    enabled: Boolean = true,
) {
    val context = LocalContext.current
    val colorPasteError = rememberSaveable { mutableStateOf<String?>(null) }
    val onCopyCustomColor = {
        Clipboard.copy(
            text = getFormattedColor(color),
            message = R.string.color_copied
        )
    }
    val onPasteCustomColor = {
        context.pasteColorFromClipboard(
            onPastedColor = onColorChange,
            onPastedColorFailure = { colorPasteError.value = it },
        )
    }
    LaunchedEffect(colorPasteError.value) {
        delay(1500.milliseconds)
        colorPasteError.value = null
    }

    // 一行里不再嵌套第二层卡片：色块 / 色值 / 图标按钮直接排在同一个玻璃容器内，
    // 视觉层级只有「卡片 → 控件」两层，避免玻璃叠玻璃把色值和图标压糊。
    var expanded by rememberSaveable { mutableStateOf(false) }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.5f),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .container(
                    shape = MaterialTheme.shapes.medium,
                    color = color,
                    resultPadding = 0.dp
                )
                .clip(MaterialTheme.shapes.medium)
                .transparencyChecker()
                .background(
                    color = color,
                    shape = MaterialTheme.shapes.medium
                ),
            contentAlignment = Alignment.Center
        ) {
            EnhancedIconButton(
                onClick = onSupportButtonClick,
                enabled = enabled
            ) {
                Icon(
                    imageVector = supportButtonIcon,
                    contentDescription = stringResource(R.string.edit),
                    tint = animateColorAsState(
                        color.inverse(
                            fraction = { cond ->
                                if (cond) 0.8f
                                else 0.5f
                            },
                            darkMode = color.luminance() < 0.3f
                        )
                    ).value,
                    modifier = Modifier
                        .size(26.dp)
                        .background(
                            color = color.copy(alpha = 1f),
                            shape = ShapeDefaults.mini
                        )
                        .padding(2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .clip(ShapeDefaults.pressed)
                .container(
                    shape = ShapeDefaults.pressed,
                    color = infoContainerColor,
                    resultPadding = 0.dp
                )
                .hapticsClickable(enabled = enabled) {
                    expanded = true
                },
            contentAlignment = Alignment.CenterStart
        ) {
            AutoSizeText(
                text = colorPasteError.value ?: getFormattedColor(color),
                style = MaterialTheme.typography.titleMedium,
                color = if (colorPasteError.value != null) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    LocalContentColor.current.copy(alpha = 0.7f)
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        // 图标按钮固定同宽同高，行宽不够时也不会互相叠压
        EnhancedIconButton(
            onClick = onCopyCustomColor,
            enabled = enabled
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ContentCopy,
                contentDescription = stringResource(R.string.copy)
            )
        }
        EnhancedIconButton(
            onClick = onPasteCustomColor,
            enabled = enabled
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineContentPaste,
                contentDescription = stringResource(R.string.pastel)
            )
        }
        if (onRemove != null) {
            EnhancedIconButton(
                onClick = onRemove,
                enabled = enabled,
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Delete,
                    contentDescription = removeContentDescription
                        ?: stringResource(R.string.delete)
                )
            }
        }
    }

    var value by remember(expanded, color) { mutableStateOf(getFormattedColor(color)) }
    EnhancedAlertDialog(
        visible = expanded,
        onDismissRequest = { expanded = false },
        icon = {
            val hexColorInt by remember(value) {
                derivedStateOf {
                    if (hexWithAlphaRegex.matches(value)) {
                        HexUtil.hexToColor(value).toArgb()
                    } else null
                }
            }
            AnimatedContent(hexColorInt) { colorFromHex ->
                if (colorFromHex != null) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .container(
                                shape = CircleShape,
                                color = Color(colorFromHex),
                                resultPadding = 0.dp
                            )
                    )
                } else {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTheme,
                        contentDescription = null
                    )
                }
            }

        },
        title = {
            Text(stringResource(R.string.color))
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val style =
                    MaterialTheme.typography.titleMedium.copy(textAlign = TextAlign.Center)
                GlassOutlinedTextField(
                    shape = ShapeDefaults.default,
                    textStyle = style,
                    maxLines = 1,
                    value = value.removePrefix("#"),
                    visualTransformation = HexVisualTransformation(true),
                    onValueChange = {
                        val hex = it.replace("#", "")

                        if (hex.length <= 8) {
                            var validHex = true

                            for (index in hex.indices) {
                                validHex =
                                    hexRegexSingleChar.matches(hex[index].toString())
                                if (!validHex) break
                            }

                            if (validHex) {
                                value = "#${hex.uppercase()}"
                            }
                        }
                    },
                    placeholder = {
                        Text(
                            text = "#AARRGGBB",
                            style = style,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                )
            }
        },
        confirmButton = {
            EnhancedButton(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                onClick = {
                    if (hexWithAlphaRegex.matches(value)) {
                        onColorChange(HexUtil.hexToColor(value))
                    }
                    expanded = false
                }
            ) {
                Text(stringResource(R.string.apply))
            }
        }
    )
}

/** Receive the clipboard data. */
private fun getFormattedColor(color: Color): String {
    return if (color.alpha == 1f) {
        colorToHex(color)
    } else {
        colorToHexAlpha(color)
    }.uppercase()
}