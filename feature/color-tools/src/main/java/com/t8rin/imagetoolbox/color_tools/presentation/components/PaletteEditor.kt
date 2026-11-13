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

package com.t8rin.imagetoolbox.color_tools.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.smarttoolfactory.colordetector.parser.ColorNameParser
import com.t8rin.imagetoolbox.core.domain.model.ColorPalette
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.Close
import com.t8rin.imagetoolbox.core.resources.icons.Delete
import com.t8rin.imagetoolbox.core.resources.icons.line.LineContentPasteGo
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDeleteForever
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFavorite
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePaletteTools
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRename
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSimpleSettingsInteractor
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.core.ui.utils.helper.Clipboard
import com.wanbaohe.core.ui.utils.helper.toHex
import com.wanbaohe.core.ui.widget.color_picker.LargeColorBlock
import com.wanbaohe.core.ui.widget.color_picker.contentColor
import com.wanbaohe.core.ui.widget.color_picker.newPaletteName
import com.wanbaohe.core.ui.widget.enhanced.EnhancedAlertDialog
import com.wanbaohe.core.ui.widget.enhanced.EnhancedIconButton
import com.wanbaohe.core.ui.widget.enhanced.hapticsClickable
import com.wanbaohe.core.ui.widget.system.SelectorField
import com.wanbaohe.core.ui.widget.system.OneBoxDangerButton
import com.wanbaohe.core.ui.widget.system.OneBoxOutlinedTextField
import com.wanbaohe.core.ui.widget.system.OnePrimaryButton
import com.wanbaohe.core.ui.widget.system.OneSecondaryButton
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * 「调色板」页：用户把自己的色彩搭配存成一组组，选色面板里可以直接调出来。
 *
 * 数据通过 [LocalSettingsState] 读、[LocalSimpleSettingsInteractor] 写，
 * 写入后 DataStore 回流到 settingsState，整页与全局选色面板同时刷新。
 */
@Composable
internal fun PaletteEditor(
    selectedColor: Color,
    onColorChange: (Color) -> Unit,
    selectedPaletteId: String?,
    onSelectPalette: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val settings = LocalSettingsState.current
    val interactor = LocalSimpleSettingsInteractor.current
    val scope = rememberCoroutineScope()

    val palettes = settings.customPalettes
    val favoriteColors = settings.favoriteColors

    var nameEditTarget by remember { mutableStateOf<PaletteNameEdit?>(null) }
    var deleteTarget by remember { mutableStateOf<ColorPalette?>(null) }

    val currentPalette = palettes.firstOrNull { it.id == selectedPaletteId }
        ?: palettes.firstOrNull()

    val paletteFullToast = stringResource(
        R.string.palette_full,
        ColorPalette.MAX_COLORS
    )
    // 命名规则是「调色板 N」；@Composable 取字符串，不能放进点击回调里现算
    val defaultName = newPaletteName(palettes)

    // 最多 24 色：两列分组一次算好，LazyColumn 只组合可见行
    val colorRows = remember(currentPalette?.colors) {
        currentPalette?.colors?.chunked(2).orEmpty()
    }

    fun persist(updated: List<ColorPalette>) {
        scope.launch {
            interactor.setCustomPalettes(updated.take(ColorPalette.MAX_PALETTES))
        }
    }

    fun savePalette(palette: ColorPalette) {
        val exists = palettes.any { it.id == palette.id }
        val updated = if (exists) {
            palettes.map { if (it.id == palette.id) palette else it }
        } else {
            palettes + palette
        }
        onSelectPalette(palette.id)
        persist(updated)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (palettes.isEmpty()) {
            item(key = "empty") {
                PaletteEmptyContent(
                    canCreateFromFavorites = favoriteColors.isNotEmpty(),
                    onCreate = {
                        nameEditTarget = PaletteNameEdit(
                            palette = null,
                            initialName = defaultName
                        )
                    },
                    onCreateFromFavorites = {
                        if (favoriteColors.isNotEmpty()) {
                            savePalette(
                                ColorPalette(
                                    id = UUID.randomUUID().toString(),
                                    name = defaultName,
                                    colors = favoriteColors
                                        .map { it.toArgb() }
                                        .distinct()
                                        .take(ColorPalette.MAX_COLORS),
                                    updatedAtMillis = System.currentTimeMillis()
                                )
                            )
                            AppToastHost.showToast(R.string.palette_created)
                        }
                    }
                )
            }
        } else {
            val palette = currentPalette
            if (palette != null) {
                item(key = "selector") {
                    // 调色板名可能很长（用户会写「2026 春季品牌视觉 主色系」这种），
                    // 用下拉选择器：选中行单行省略，菜单里看全名，也不会把一行撑爆。
                    PaletteSelector(
                        selected = palette,
                        palettes = palettes,
                        favorites = favoriteColors,
                        onSelect = { onSelectPalette(it.id) },
                        onCreate = {
                            nameEditTarget = PaletteNameEdit(
                                palette = null,
                                initialName = defaultName
                            )
                        },
                        onCreateFromFavorites = favoriteColors
                            .takeIf { it.isNotEmpty() }
                            ?.let { favorites ->
                                {
                                    savePalette(
                                        ColorPalette(
                                            id = UUID.randomUUID().toString(),
                                            name = defaultName,
                                            colors = favorites
                                                .map { color -> color.toArgb() }
                                                .distinct()
                                                .take(ColorPalette.MAX_COLORS),
                                            updatedAtMillis = System.currentTimeMillis()
                                        )
                                    )
                                }
                            }
                    )
                }

                item(key = "header") {
                    PaletteHeader(
                        palette = palette,
                        onRename = {
                            nameEditTarget = PaletteNameEdit(
                                palette = palette,
                                initialName = palette.name
                            )
                        },
                        onCopyAll = {
                            if (palette.colors.isNotEmpty()) {
                                Clipboard.copy(
                                    text = palette.colors
                                        .joinToString(", ") { Color(it).toHex() },
                                    message = R.string.palette_copied
                                )
                            }
                        },
                        onDelete = { deleteTarget = palette }
                    )
                }

                if (palette.colors.isEmpty()) {
                    item(key = "empty_colors") {
                        PaletteEmptyColors()
                    }
                } else {
                    items(
                        items = colorRows,
                        key = { row -> row.firstOrNull() ?: Int.MIN_VALUE }
                    ) { rowColors ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowColors.forEach { argb ->
                                PaletteColorBlock(
                                    argb = argb,
                                    onClick = { onColorChange(Color(argb)) },
                                    onRemove = {
                                        val updated = palette.removing(argb)
                                        persist(
                                            palettes.map {
                                                if (it.id == palette.id) updated else it
                                            }
                                        )
                                        AppToastHost.showToast(
                                            R.string.palette_color_removed
                                        )
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (rowColors.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                item(key = "add_current") {
                    OnePrimaryButton(
                        text = stringResource(R.string.palette_add_current),
                        onClick = {
                            if (palette.colors.size >= ColorPalette.MAX_COLORS) {
                                AppToastHost.showToast(paletteFullToast)
                            } else if (palette.contains(selectedColor.toArgb())) {
                                AppToastHost.showToast(R.string.palette_already_exists)
                            } else {
                                val updated = palette.appending(selectedColor.toArgb())
                                persist(
                                    palettes.map {
                                        if (it.id == palette.id) updated else it
                                    }
                                )
                                AppToastHost.showToast(R.string.palette_added)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp),
                        leadingIcon = Icons.Outlined.LinePaletteTools
                    )
                }
            }
        }
    }

    nameEditTarget?.let { target ->
        PaletteNameDialog(
            title = stringResource(
                if (target.palette == null) R.string.palette_new else R.string.palette_rename
            ),
            initialName = target.initialName,
            onDismiss = { nameEditTarget = null },
            onConfirm = { name ->
                nameEditTarget = null
                val palette = target.palette
                if (palette == null) {
                    savePalette(
                        ColorPalette(
                            id = UUID.randomUUID().toString(),
                            name = name,
                            updatedAtMillis = System.currentTimeMillis()
                        )
                    )
                    AppToastHost.showToast(R.string.palette_created)
                } else {
                    savePalette(
                        palette.copy(
                            name = name,
                            updatedAtMillis = System.currentTimeMillis()
                        )
                    )
                }
            }
        )
    }

    deleteTarget?.let { target ->
        EnhancedAlertDialog(
            visible = true,
            onDismissRequest = { deleteTarget = null },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.LineDeleteForever,
                    contentDescription = null
                )
            },
            title = { Text(stringResource(R.string.palette_delete)) },
            text = {
                Text(
                    stringResource(
                        R.string.palette_delete_confirm,
                        target.name
                    )
                )
            },
            confirmButton = {
                OneBoxDangerButton(
                    text = stringResource(R.string.delete),
                    onClick = {
                        deleteTarget = null
                        onSelectPalette(
                            palettes.firstOrNull { it.id != target.id }?.id
                        )
                        persist(palettes.filterNot { it.id == target.id })
                    }
                )
            },
            dismissButton = {
                OneSecondaryButton(
                    text = stringResource(R.string.cancel),
                    onClick = { deleteTarget = null }
                )
            }
        )
    }
}

private data class PaletteNameEdit(
    val palette: ColorPalette?,
    val initialName: String
)

/**
 * 调色板选择器：选中行显示当前组名（单行省略），全部组名收进下拉菜单。
 *
 * 直接用 [SelectorField] 而不是自拼 chip 行，是因为调色板名可以很长，
 * chip 行既读不全也容易被长名字撑得很难看。
 */
@Composable
private fun PaletteSelector(
    selected: ColorPalette,
    palettes: List<ColorPalette>,
    favorites: List<Color>,
    onSelect: (ColorPalette) -> Unit,
    onCreate: () -> Unit,
    onCreateFromFavorites: (() -> Unit)?
) {
    SelectorField(
        label = stringResource(R.string.my_palettes),
        selected = selected,
        options = palettes,
        optionLabel = { it.name },
        onSelect = onSelect,
        menuFooter = { dismiss ->
            MenuActionRow(
                icon = Icons.Rounded.Add,
                text = stringResource(R.string.palette_new),
                enabled = palettes.size < ColorPalette.MAX_PALETTES,
                onClick = {
                    dismiss()
                    onCreate()
                }
            )
            if (onCreateFromFavorites != null && favorites.isNotEmpty()) {
                MenuActionRow(
                    icon = Icons.Outlined.LineFavorite,
                    text = stringResource(R.string.palette_from_favorites),
                    enabled = palettes.size < ColorPalette.MAX_PALETTES,
                    onClick = {
                        dismiss()
                        onCreateFromFavorites()
                    }
                )
            }
        }
    )
}

@Composable
private fun MenuActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = { Text(text) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null
            )
        },
        enabled = enabled,
        onClick = onClick
    )
}

@Composable
private fun PaletteHeader(
    palette: ColorPalette,
    onRename: () -> Unit,
    onCopyAll: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = palette.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
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
        EnhancedIconButton(
            onClick = onCopyAll,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ) {
            Icon(
                imageVector = Icons.Outlined.LineContentPasteGo,
                contentDescription = stringResource(R.string.palette_copy_all)
            )
        }
        EnhancedIconButton(
            onClick = onRename,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ) {
            Icon(
                imageVector = Icons.Outlined.LineRename,
                contentDescription = stringResource(R.string.rename)
            )
        }
        EnhancedIconButton(
            onClick = onDelete,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        ) {
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = stringResource(R.string.palette_delete)
            )
        }
    }
}

@Composable
private fun PaletteColorBlock(
    argb: Int,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = Color(argb)
    // 色名解析是 O(n) 的最近色扫描，别每次重组都跑一遍
    val colorName = remember(color) { ColorNameParser.parseColorName(color) }
    LargeColorBlock(
        color = color,
        onClick = onClick,
        modifier = modifier.heightIn(min = 96.dp),
        primaryText = color.toHex(),
        secondaryText = colorName,
        badge = {
            // 移除按钮压在左上角：右下角留给色值，右上角留给复制图标
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 1f))
                    .hapticsClickable(onClick = onRemove),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = stringResource(R.string.palette_remove_color),
                    modifier = Modifier.size(16.dp),
                    tint = color.contentColor()
                )
            }
        }
    )
}

@Composable
private fun PaletteEmptyContent(
    canCreateFromFavorites: Boolean,
    onCreate: () -> Unit,
    onCreateFromFavorites: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.LinePaletteTools,
                contentDescription = null,
                modifier = Modifier.size(34.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.palette_empty_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.palette_empty_desc),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OnePrimaryButton(
                text = stringResource(R.string.palette_new),
                onClick = onCreate,
                modifier = Modifier
                    .weight(1f, fill = false)
                    .heightIn(min = 48.dp),
                leadingIcon = Icons.Rounded.Add
            )
            if (canCreateFromFavorites) {
                OneSecondaryButton(
                    text = stringResource(R.string.palette_from_favorites),
                    onClick = onCreateFromFavorites,
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .heightIn(min = 48.dp),
                    leadingIcon = Icons.Outlined.LineFavorite
                )
            }
        }
    }
}

@Composable
private fun PaletteEmptyColors() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.palette_no_colors),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.palette_no_colors_desc),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PaletteNameDialog(
    title: String,
    initialName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember(initialName) { mutableStateOf(initialName) }
    val trimmed = name.trim()

    EnhancedAlertDialog(
        visible = true,
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Outlined.LinePaletteTools,
                contentDescription = null
            )
        },
        title = { Text(title) },
        text = {
            OneBoxOutlinedTextField(
                value = name,
                onValueChange = {
                    if (it.length <= ColorPalette.MAX_NAME_LENGTH) name = it
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.palette_name)) },
                singleLine = true
            )
        },
        confirmButton = {
            OnePrimaryButton(
                text = stringResource(R.string.ok),
                onClick = { onConfirm(trimmed) },
                enabled = trimmed.isNotEmpty()
            )
        },
        dismissButton = {
            OneSecondaryButton(
                text = stringResource(R.string.cancel),
                onClick = onDismiss
            )
        }
    )
}

