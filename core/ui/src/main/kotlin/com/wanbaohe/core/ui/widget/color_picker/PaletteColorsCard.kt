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
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.domain.model.ColorPalette
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePaletteTools
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSimpleSettingsInteractor
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.core.ui.widget.system.SelectorField
import com.wanbaohe.core.ui.widget.enhanced.EnhancedChip
import com.wanbaohe.core.ui.widget.enhanced.hapticsClickable
import com.wanbaohe.core.ui.widget.modifier.transparencyChecker
import com.wanbaohe.core.ui.widget.text.TitleItem
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * 选色面板里的「我的调色板」：在「最近使用 / 收藏」之外，多一组用户自己配好的颜色。
 *
 * 面板内自己闭环：新建调色板 → 把当前颜色存进去 → 再多取几个颜色继续加，
 * 不用跳去色彩工具。删色、改名这些重操作留在色彩工具的「调色板」页做。
 */
@Composable
fun PaletteColorsCard(
    currentColor: Color,
    onColorClick: (Color) -> Unit,
) {
    val settingsState = LocalSettingsState.current
    val interactor = LocalSimpleSettingsInteractor.current
    val scope = rememberCoroutineScope()

    val palettes = settingsState.customPalettes
    var selectedPaletteId by rememberSaveable { mutableStateOf<String?>(null) }
    val palette = palettes.firstOrNull { it.id == selectedPaletteId }
        ?: palettes.firstOrNull()

    val currentArgb = currentColor.toArgb()
    val paletteFullMessage = stringResource(R.string.palette_full, ColorPalette.MAX_COLORS)
    // @Composable 取名字，不能落在 createPalette() 这种点击回调里
    val suggestedPaletteName = newPaletteName(palettes)

    fun persist(updated: List<ColorPalette>) {
        scope.launch {
            interactor.setCustomPalettes(updated.take(ColorPalette.MAX_PALETTES))
        }
    }

    fun createPalette() {
        val newPalette = ColorPalette(
            id = UUID.randomUUID().toString(),
            name = suggestedPaletteName,
            colors = listOf(currentArgb),
            updatedAtMillis = System.currentTimeMillis()
        )
        selectedPaletteId = newPalette.id
        persist(palettes + newPalette)
    }

    // 不再套第二层玻璃卡：外层容器的底色 + 16dp 内边距会让调色板这块内容
    // 比下面的色值行、取色器多缩进一层，看着对不齐。这里只保留段落间距。
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TitleItem(
            text = stringResource(R.string.my_palettes),
            icon = Icons.Outlined.LinePaletteTools,
            modifier = Modifier,
            endContent = {
                if (palette != null) {
                    Text(
                        text = pluralStringResource(
                            R.plurals.color_count,
                            palette.colors.size,
                            palette.colors.size
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )

        if (palettes.isEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.palette_empty_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(12.dp))
                OneBoxCompactChip(
                    text = stringResource(R.string.palette_new),
                    onClick = { createPalette() }
                )
            }
        } else if (palette != null) {
            // 名称可能很长：选中行单行省略，菜单里看全名
            SelectorField(
                selected = palette,
                options = palettes,
                optionLabel = { it.name },
                onSelect = { selectedPaletteId = it.id },
                menuFooter = { dismiss ->
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.palette_new)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = null
                            )
                        },
                        enabled = palettes.size < ColorPalette.MAX_PALETTES,
                        onClick = {
                            dismiss()
                            createPalette()
                        }
                    )
                }
            )

            if (palette.colors.isEmpty()) {
                Text(
                    text = stringResource(R.string.palette_no_colors_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                palette.colors.forEach { argb ->
                    PaletteColorCell(
                        argb = argb,
                        selected = argb == currentArgb,
                        onClick = { onColorClick(Color(argb)) }
                    )
                }
                if (palette.colors.size < ColorPalette.MAX_COLORS) {
                    AddColorCell(
                        onClick = {
                            when {
                                palette.contains(currentArgb) ->
                                    AppToastHost.showToast(R.string.palette_already_exists)

                                palette.colors.size >= ColorPalette.MAX_COLORS ->
                                    AppToastHost.showToast(paletteFullMessage)

                                else -> {
                                    val updated = palette.appending(currentArgb)
                                    persist(
                                        palettes.map {
                                            if (it.id == palette.id) updated else it
                                        }
                                    )
                                    AppToastHost.showToast(R.string.palette_added)
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PaletteColorCell(
    argb: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    val color = Color(argb)
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(shape)
            .then(if (color.alpha < 1f) Modifier.transparencyChecker() else Modifier)
            .background(color)
            .colorSwatchOutline(color = color, shape = shape)
            .then(
                if (selected) {
                    Modifier.border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = shape
                    )
                } else {
                    Modifier
                }
            )
            .hapticsClickable(onClick = onClick)
    )
}

@Composable
private fun AddColorCell(
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = shape
            )
            .hapticsClickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = stringResource(R.string.add_to_palette),
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun OneBoxCompactChip(
    text: String,
    onClick: () -> Unit
) {
    EnhancedChip(
        selected = false,
        onClick = onClick,
        selectedColor = MaterialTheme.colorScheme.primaryContainer,
        defaultMinSize = 32.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = null,
                modifier = Modifier.size(14.dp)
            )
            Text(text = text)
        }
    }
}
