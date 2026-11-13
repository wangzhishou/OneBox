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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.domain.model.ColorPalette
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRadioChecked
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRadioUnchecked
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePaletteTools
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSimpleSettingsInteractor
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.core.ui.utils.navigation.LocalOnNavigate
import com.wanbaohe.core.ui.utils.navigation.Screen
import com.wanbaohe.core.ui.utils.helper.toHex
import com.wanbaohe.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.wanbaohe.core.ui.widget.modifier.ShapeDefaults
import com.wanbaohe.core.ui.widget.system.OneBoxListItem
import com.wanbaohe.core.ui.widget.system.OnePrimaryButton
import com.wanbaohe.core.ui.widget.system.OneSecondaryButton
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * 「加入调色板」选择面板：把一个颜色、或一整组颜色存进某组调色板，或当场新建一组。
 *
 * 取色页 / 取色历史 / 色彩工具 / 图片配色都会调它，所以放在 core 里统一一份。
 * 传入 [colors] 非空即按整组处理：一次把缺的颜色都补进去，返回实际加了几色。
 */
@Composable
fun AddToPaletteSheet(
    colors: List<Int>,
    visible: Boolean,
    onDismiss: () -> Unit,
) {
    if (!visible || colors.isEmpty()) return

    val settingsState = LocalSettingsState.current
    val interactor = LocalSimpleSettingsInteractor.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val palettes = settingsState.customPalettes
    val distinctColors = colors.distinct().take(ColorPalette.MAX_COLORS)
    val isGroup = distinctColors.size > 1
    val fullMessage = stringResource(R.string.palette_full, ColorPalette.MAX_COLORS)
    val maxPalettesMessage = stringResource(
        R.string.palette_max_reached,
        ColorPalette.MAX_PALETTES
    )
    // @Composable 取名字，不能落在点击回调里现算
    val suggestedPaletteName = newPaletteName(palettes)
    val onNavigate = LocalOnNavigate.current
    var selectedPaletteId by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedPalette = palettes.firstOrNull { it.id == selectedPaletteId }
        ?: palettes.firstOrNull()

    fun persist(updated: List<ColorPalette>) {
        scope.launch {
            interactor.setCustomPalettes(updated.take(ColorPalette.MAX_PALETTES))
        }
    }

    fun addTo(palette: ColorPalette) {
        val missing = distinctColors.filterNot { palette.contains(it) }
        when {
            missing.isEmpty() -> AppToastHost.showToast(R.string.palette_already_exists)

            palette.colors.size >= ColorPalette.MAX_COLORS ->
                AppToastHost.showToast(fullMessage)

            else -> {
                var updated = palette
                missing.take(ColorPalette.MAX_COLORS - palette.colors.size).forEach { argb ->
                    updated = updated.appending(argb)
                }
                persist(palettes.map { if (it.id == palette.id) updated else it })
                if (isGroup) {
                    AppToastHost.showToast(
                        context.getString(
                            R.string.palette_added_count,
                            updated.colors.size - palette.colors.size
                        )
                    )
                } else {
                    AppToastHost.showToast(R.string.palette_added)
                }
            }
        }
        onDismiss()
    }

    val confirmBar: (@Composable RowScope.() -> Unit)? = if (palettes.isEmpty()) {
        null
    } else {
        {
            OneSecondaryButton(
                text = stringResource(R.string.my_palettes),
                onClick = {
                    onDismiss()
                    onNavigate(Screen.ColorTools(initialTab = Screen.ColorTools.TAB_PALETTE))
                }
            )
            Spacer(Modifier.width(8.dp))
            OnePrimaryButton(
                text = stringResource(R.string.ok),
                enabled = selectedPalette != null,
                onClick = { selectedPalette?.let(::addTo) }
            )
        }
    }

    EnhancedModalBottomSheet(
        visible = true,
        onDismiss = { onDismiss() },
        title = stringResource(R.string.add_to_palette),
        confirmButton = confirmBar,
        // 这张面板的确认按钮贴着屏幕底边时会被手势导航的死区吃掉一半点击，
        // 垫 16dp 把按钮抬到可点区域（底栏和弹层同色，看不出多了一条底边）
        confirmBarModifier = Modifier.padding(bottom = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 8.dp)
                .padding(bottom = 16.dp)
        ) {
            AddToPaletteHeader(colors = distinctColors)

            if (palettes.isEmpty()) {
                OneBoxListItem(
                    contained = true,
                    headlineContent = {
                        Text(
                            text = stringResource(R.string.palette_new),
                            fontWeight = FontWeight.Medium
                        )
                    },
                    subtitle = {
                        Text(
                            text = stringResource(R.string.palette_empty_desc),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Outlined.LinePaletteTools,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        val newPalette = ColorPalette(
                            id = UUID.randomUUID().toString(),
                            name = suggestedPaletteName,
                            colors = distinctColors,
                            updatedAtMillis = System.currentTimeMillis()
                        )
                        persist(palettes + newPalette)
                        AppToastHost.showToast(R.string.palette_created)
                        onDismiss()
                    },
                    shape = ShapeDefaults.default
                )
            } else {
                palettes.forEach { palette ->
                    val isSelected = palette.id == selectedPalette?.id
                    val alreadyContains = palette.containsAll(distinctColors)
                    val countText = pluralStringResource(
                        R.plurals.color_count,
                        palette.colors.size,
                        palette.colors.size
                    )
                    OneBoxListItem(
                        contained = true,
                        selected = isSelected,
                        headlineContent = {
                            Text(
                                text = palette.name,
                                fontWeight = FontWeight.Medium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        subtitle = {
                            Text(
                                text = if (alreadyContains) {
                                    "$countText · " + stringResource(
                                        R.string.palette_already_contains
                                    )
                                } else {
                                    countText
                                }
                            )
                        },
                        leadingContent = {
                            PalettePreviewStrip(colors = palette.colors)
                        },
                        trailingContent = {
                            Icon(
                                imageVector = if (isSelected) {
                                    Icons.Outlined.LineRadioChecked
                                } else {
                                    Icons.Outlined.LineRadioUnchecked
                                },
                                contentDescription = null,
                                tint = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        },
                        onClick = { selectedPaletteId = palette.id },
                        shape = ShapeDefaults.default
                    )
                }
                OneBoxListItem(
                    contained = true,
                    headlineContent = {
                        Text(
                            text = stringResource(R.string.palette_new),
                            fontWeight = FontWeight.Medium
                        )
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        if (palettes.size >= ColorPalette.MAX_PALETTES) {
                            AppToastHost.showToast(maxPalettesMessage)
                        } else {
                            val newPalette = ColorPalette(
                                id = UUID.randomUUID().toString(),
                                name = suggestedPaletteName,
                                colors = distinctColors,
                                updatedAtMillis = System.currentTimeMillis()
                            )
                            persist(palettes + newPalette)
                            AppToastHost.showToast(R.string.palette_created)
                            onDismiss()
                        }
                    },
                    shape = ShapeDefaults.default
                )
            }
        }
    }
}

/**
 * 单色快捷重载：取色卡片、取色历史仍然只丢一个颜色进来。
 */
@Composable
fun AddToPaletteSheet(
    color: Color,
    visible: Boolean,
    onDismiss: () -> Unit,
) = AddToPaletteSheet(
    colors = listOf(color.toArgb()),
    visible = visible,
    onDismiss = onDismiss
)

@Composable
private fun AddToPaletteHeader(colors: List<Int>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (colors.size == 1) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(colors.first()))
                    .colorSwatchOutline(
                        color = Color(colors.first()),
                        shape = CircleShape
                    )
            )
            Spacer(Modifier.width(2.dp))
            Text(
                text = Color(colors.first()).toHex(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            colors.take(8).forEach { argb ->
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(argb))
                        .colorSwatchOutline(
                            color = Color(argb),
                            shape = RoundedCornerShape(8.dp)
                        )
                )
            }
            Spacer(Modifier.width(2.dp))
            Text(
                text = pluralStringResource(
                    R.plurals.color_count,
                    colors.size,
                    colors.size
                ),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 一行小色条：让用户在「选哪组调色板」时直接看到这组的配色，而不是只读名字。
 */
@Composable
private fun PalettePreviewStrip(
    colors: List<Int>
) {
    Row(
        modifier = Modifier
            .width(72.dp)
            .heightIn(min = 32.dp)
            .clip(RoundedCornerShape(10.dp)),
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        if (colors.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
            )
        } else {
            colors.take(4).forEach { argb ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp)
                        .background(Color(argb))
                )
            }
        }
    }
}

private fun ColorPalette.containsAll(colors: List<Int>): Boolean =
    colors.all { contains(it) }
