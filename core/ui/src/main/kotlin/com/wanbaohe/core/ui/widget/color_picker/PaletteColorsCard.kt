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
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.t8rin.imagetoolbox.core.resources.icons.Close
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePaletteTools
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSimpleSettingsInteractor
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.core.ui.widget.system.SelectorField
import com.wanbaohe.core.ui.widget.enhanced.EnhancedChip
import com.wanbaohe.core.ui.widget.enhanced.EnhancedIconButton
import com.wanbaohe.core.ui.widget.enhanced.hapticsClickable
import com.wanbaohe.core.ui.widget.modifier.transparencyChecker
import com.wanbaohe.core.ui.widget.text.TitleItem
import kotlinx.coroutines.launch
import java.util.UUID
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.t8rin.imagetoolbox.core.resources.icons.Edit

/**
 * 选色面板里的「我的调色板」：在「最近使用 / 收藏」之外，多一组用户自己配好的颜色。
 *
 * 面板内自己闭环：新建调色板 → 把当前颜色存进去 → 再多取几个颜色继续加，
 * 不用跳去色彩工具。改名、删组这些重操作仍留在色彩工具里做；
 * 但删单个颜色必须在这里能完成 —— 选色面板是用户最常停留的地方，
 * 发现某个颜色不想要了却要退出去改，体验是断的。
 *
 * 选中的调色板 id 持久化在设置里：色彩工具里切过哪一组，下次打开还是那一组。
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
    // DataStore 回流有一到两帧延迟：新建调色板后先在本地记住它，避免闪一下第一组。
    // 设置值追平后清掉 pending，之后的切换完全以持久化为准。
    var pendingPaletteId by remember { mutableStateOf<String?>(null) }
    val effectivePaletteId = pendingPaletteId ?: settingsState.selectedPaletteId
    val palette = palettes.firstOrNull { it.id == effectivePaletteId }
        ?: palettes.firstOrNull()

    var editMode by remember { mutableStateOf(false) }

    LaunchedEffect(settingsState.selectedPaletteId, pendingPaletteId) {
        if (pendingPaletteId == settingsState.selectedPaletteId) {
            pendingPaletteId = null
        }
    }
    // 切组后退出编辑态，避免手滑把另一组的颜色删了
    LaunchedEffect(palette?.id) { editMode = false }
    LaunchedEffect(palette?.colors?.isEmpty()) {
        if (palette?.colors?.isEmpty() == true) editMode = false
    }

    val currentArgb = currentColor.toArgb()
    val paletteFullMessage = stringResource(R.string.palette_full, ColorPalette.MAX_COLORS)
    // @Composable 取名字，不能落在 createPalette() 这种点击回调里
    val suggestedPaletteName = newPaletteName(palettes)

    fun persist(updated: List<ColorPalette>) {
        scope.launch {
            interactor.setCustomPalettes(updated.take(ColorPalette.MAX_PALETTES))
        }
    }

    fun selectPalette(id: String?) {
        pendingPaletteId = id
        editMode = false
        scope.launch {
            interactor.setSelectedPaletteId(id)
        }
    }

    fun createPalette() {
        val newPalette = ColorPalette(
            id = UUID.randomUUID().toString(),
            name = suggestedPaletteName,
            colors = listOf(currentArgb),
            updatedAtMillis = System.currentTimeMillis()
        )
        pendingPaletteId = newPalette.id
        scope.launch {
            interactor.setCustomPalettes(
                (palettes + newPalette).take(ColorPalette.MAX_PALETTES)
            )
            interactor.setSelectedPaletteId(newPalette.id)
        }
    }

    fun removeColor(argb: Int) {
        val target = palette ?: return
        val updated = target.removing(argb)
        persist(palettes.map { if (it.id == target.id) updated else it })
        AppToastHost.showToast(R.string.palette_color_removed)
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
                    if (palette.colors.isNotEmpty()) {
                        EnhancedIconButton(
                            onClick = { editMode = !editMode },
                            containerColor = Color.Transparent,
                            contentColor = if (editMode) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        ) {
                            Icon(
                                imageVector = if (editMode) {
                                    Icons.Outlined.Check
                                } else {
                                    Icons.Outlined.Edit
                                },
                                contentDescription = stringResource(
                                    if (editMode) R.string.ok else R.string.edit
                                )
                            )
                        }
                    }
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
                onSelect = { selectPalette(it.id) },
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
                        editMode = editMode,
                        onClick = { onColorClick(Color(argb)) },
                        onRemove = { removeColor(argb) }
                    )
                }
                if (!editMode && palette.colors.size < ColorPalette.MAX_COLORS) {
                    AddColorCell(
                        onClick = {
                            when {
                                palette.contains(currentArgb) ->
                                    AppToastHost.showToast(R.string.palette_already_exists)

                                palette.colors.size >= ColorPalette.MAX_COLORS ->
                                    AppToastHost.showToast(paletteFullMessage)

                                else -> {
                                    val updated = palette.prepending(currentArgb)
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
    editMode: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    val color = Color(argb)
    val shape = MaterialTheme.shapes.large
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(shape)
            .then(if (color.alpha < 1f) Modifier.transparencyChecker() else Modifier)
            .background(color)
            .colorSwatchOutline(color = color, shape = shape)
            .hapticsClickable(onClick = if (editMode) onRemove else onClick)
    ) {
        if (selected && !editMode) {
            // 扁平选中态：不加描边、不换形状，只在色块中心压一个对勾
            Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = null,
                tint = if (color.alpha < 1f) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    color.contentColor()
                },
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(20.dp)
            )
        }
        if (editMode) {
            // 编辑态：每个色块右上角挂一个删除标记，点整块即删。
            // 标记用错误色，和正常选中态区分开，避免误以为是在选色。
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(3.dp)
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = stringResource(R.string.palette_remove_color),
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
private fun AddColorCell(
    onClick: () -> Unit
) {
    val shape = MaterialTheme.shapes.large
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainer)
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
