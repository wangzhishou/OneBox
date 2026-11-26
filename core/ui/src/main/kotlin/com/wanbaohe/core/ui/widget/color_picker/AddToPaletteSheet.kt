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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import kotlinx.coroutines.CancellationException
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
    // 默认落在上次用过的调色板；用户这次选了别的，也记下来，下次继续用这一组。
    // pending 只用来接住 DataStore 回流前的一两帧，避免选中态闪回第一组。
    var pendingPaletteId by rememberSaveable { mutableStateOf<String?>(null) }
    val effectivePaletteId = pendingPaletteId ?: settingsState.selectedPaletteId
    val selectedPalette = palettes.firstOrNull { it.id == effectivePaletteId }
        ?: palettes.firstOrNull()
    val selectedMissingCount = selectedPalette?.let { palette ->
        distinctColors.count { !palette.contains(it) }
    } ?: 0
    val selectedAddCount = minOf(
        selectedMissingCount,
        (ColorPalette.MAX_COLORS - (selectedPalette?.colors?.size ?: 0)).coerceAtLeast(0)
    )
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(settingsState.selectedPaletteId, pendingPaletteId) {
        if (pendingPaletteId == settingsState.selectedPaletteId) {
            pendingPaletteId = null
        }
    }

    fun rememberSelection(id: String?) {
        if (isSaving) return
        pendingPaletteId = id
        scope.launch {
            interactor.setSelectedPaletteId(id)
        }
    }

    fun savePalette(
        updated: List<ColorPalette>,
        selectedPaletteId: String,
        successMessage: String
    ) {
        if (isSaving) return
        scope.launch {
            isSaving = true
            try {
                interactor.setCustomPalettes(updated.take(ColorPalette.MAX_PALETTES))
                pendingPaletteId = selectedPaletteId
                interactor.setSelectedPaletteId(selectedPaletteId)
                AppToastHost.showToast(successMessage)
                onDismiss()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                AppToastHost.showToast(R.string.something_went_wrong)
            } finally {
                isSaving = false
            }
        }
    }

    fun createPalette() {
        if (palettes.size >= ColorPalette.MAX_PALETTES) {
            AppToastHost.showToast(maxPalettesMessage)
            return
        }
        val newPalette = ColorPalette(
            id = UUID.randomUUID().toString(),
            name = suggestedPaletteName,
            colors = distinctColors,
            updatedAtMillis = System.currentTimeMillis()
        )
        savePalette(
            updated = palettes + newPalette,
            selectedPaletteId = newPalette.id,
            successMessage = context.getString(R.string.palette_created)
        )
    }

    fun addTo(palette: ColorPalette) {
        val missing = distinctColors.filterNot { palette.contains(it) }
        val addCount = minOf(
            missing.size,
            (ColorPalette.MAX_COLORS - palette.colors.size).coerceAtLeast(0)
        )
        when {
            missing.isEmpty() -> AppToastHost.showToast(R.string.palette_already_exists)

            addCount == 0 -> AppToastHost.showToast(fullMessage)

            else -> {
                val updated = palette.prependingAll(missing)
                val successMessage = when {
                    !isGroup -> context.getString(R.string.palette_added)
                    addCount < missing.size -> context.getString(
                        R.string.palette_added_count_of_total,
                        addCount,
                        missing.size
                    )
                    else -> context.getString(R.string.palette_added_count, addCount)
                }
                savePalette(
                    updated = palettes.map { if (it.id == palette.id) updated else it },
                    selectedPaletteId = palette.id,
                    successMessage = successMessage
                )
            }
        }
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
                },
                enabled = !isSaving
            )
            Spacer(Modifier.width(8.dp))
            OnePrimaryButton(
                text = if (selectedAddCount in 1 until selectedMissingCount) {
                    stringResource(
                        R.string.palette_add_count_of_total,
                        selectedAddCount,
                        selectedMissingCount
                    )
                } else {
                    stringResource(R.string.ok)
                },
                enabled = !isSaving && selectedAddCount > 0,
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
                    onClick = if (isSaving) null else ::createPalette,
                    shape = ShapeDefaults.default
                )
            } else {
                palettes.forEach { palette ->
                    val isSelected = palette.id == selectedPalette?.id
                    val alreadyContains = palette.containsAll(distinctColors)
                    val newColorCount = distinctColors.count { !palette.contains(it) }
                    val addCount = minOf(
                        newColorCount,
                        (ColorPalette.MAX_COLORS - palette.colors.size).coerceAtLeast(0)
                    )
                    val countText = pluralStringResource(
                        R.plurals.color_count,
                        palette.colors.size,
                        palette.colors.size
                    )
                    val capacityText = when {
                        alreadyContains -> stringResource(R.string.palette_already_contains)
                        addCount == 0 -> fullMessage
                        addCount < newColorCount -> stringResource(
                            R.string.palette_add_count_of_total,
                            addCount,
                            newColorCount
                        )
                        else -> null
                    }
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
                                text = capacityText?.let { "$countText · $it" } ?: countText
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
                        onClick = if (isSaving) {
                            null
                        } else {
                            { rememberSelection(palette.id) }
                        },
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
                    onClick = if (isSaving) null else ::createPalette,
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
