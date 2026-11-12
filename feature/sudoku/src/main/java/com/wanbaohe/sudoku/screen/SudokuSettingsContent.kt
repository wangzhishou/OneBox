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

package com.wanbaohe.sudoku.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.line.LineChevronRight
import com.wanbaohe.core.ui.widget.enhanced.EnhancedAlertDialog
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.core.ui.widget.glass.GlassSwitch
import com.wanbaohe.core.ui.widget.glass.glassBackground
import com.wanbaohe.core.ui.widget.system.OneBoxDesignSystem
import com.wanbaohe.core.ui.widget.system.OneBoxSectionCard
import com.wanbaohe.core.ui.widget.system.OneBoxSectionHeader
import com.wanbaohe.sudoku.R
import com.wanbaohe.sudoku.component.SudokuComponent
import com.wanbaohe.sudoku.component.SudokuGamePhase
import com.wanbaohe.sudoku.data.SudokuTimerMode
import com.wanbaohe.sudoku.logic.SudokuDifficulty

/**
 * 配置层: 设置页。改动即时生效并整体落盘(SudokuStorage)。
 * 难度选择弹单选对话框; 计时模式行内三段单选; BGM 开关立即播/停。
 */
@Composable
fun SudokuSettingsContent(
    component: SudokuComponent,
    bottomInset: Dp = 0.dp
) {
    val state by component.uiState.collectAsState()
    val settings = state.settings
    var showDifficultyDialog by remember { mutableStateOf(false) }

    BaseScreen(
        modifier = Modifier.padding(bottom = bottomInset),
        title = stringResource(R.string.sudoku_tab_settings),
        onGoBack = component::onTabBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = OneBoxDesignSystem.screenPadding),
            verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.blockSpacing)
        ) {
            Spacer(modifier = Modifier.height(OneBoxDesignSystem.screenTopSpacing))

            SudokuSectionHeader(
                title = stringResource(R.string.sudoku_settings_section_game),
                theme = SudokuTint.PRIMARY
            )
            OneBoxSectionCard(
                verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.settingRowSpacing)
            ) {
                // 有进行中的局: 显示"难度 · X/81", 点击回对局; 否则显示默认难度, 点击弹选择(选定即开新局)
                val ongoing = state.phase == SudokuGamePhase.PLAYING || state.hasSavedGame
                ValueSetting(
                    title = stringResource(R.string.sudoku_settings_difficulty),
                    value = when {
                        state.phase == SudokuGamePhase.PLAYING -> stringResource(
                            R.string.sudoku_settings_difficulty_progress,
                            difficultyLabel(state.difficulty),
                            state.entries.count { it != 0 }
                        )

                        state.hasSavedGame -> stringResource(
                            R.string.sudoku_continue_subtitle,
                            state.savedFilledCount
                        )

                        else -> difficultyLabel(settings.defaultDifficulty)
                    },
                    onClick = {
                        if (ongoing) component.openOngoingGame() else showDifficultyDialog = true
                    }
                )
                ToggleSetting(
                    title = stringResource(R.string.sudoku_settings_auto_check),
                    subtitle = stringResource(R.string.sudoku_settings_auto_check_sub),
                    checked = settings.autoCheck,
                    onCheckedChange = component::updateAutoCheck
                )
                ToggleSetting(
                    title = stringResource(R.string.sudoku_settings_hint),
                    subtitle = stringResource(R.string.sudoku_settings_hint_sub),
                    checked = settings.hintEnabled,
                    onCheckedChange = component::updateHintEnabled
                )
                TimerModeSetting(
                    selected = settings.timerMode,
                    onSelect = component::updateTimerMode
                )
            }

            SudokuSectionHeader(
                title = stringResource(R.string.sudoku_settings_section_sound),
                theme = SudokuTint.TERTIARY
            )
            OneBoxSectionCard(
                verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.settingRowSpacing)
            ) {
                ToggleSetting(
                    title = stringResource(R.string.sudoku_settings_sound),
                    subtitle = stringResource(R.string.sudoku_settings_sound_sub),
                    checked = settings.soundEnabled,
                    onCheckedChange = component::updateSoundEnabled
                )
                ToggleSetting(
                    title = stringResource(R.string.sudoku_settings_bgm),
                    subtitle = stringResource(R.string.sudoku_settings_bgm_sub),
                    checked = settings.bgmEnabled,
                    onCheckedChange = component::updateBgmEnabled
                )
            }

            Spacer(modifier = Modifier.height(OneBoxDesignSystem.blockSpacing))
        }
    }

    if (showDifficultyDialog) {
        DifficultyDialog(
            selected = settings.defaultDifficulty,
            onSelect = {
                component.startNewGameFromSettings(it)
                showDifficultyDialog = false
            },
            onDismiss = { showDifficultyDialog = false }
        )
    }
}

// ─── 行组件 ────────────────────────────────────────────────────────────────

/** 分区标题前的主题色竖条, 让两个分组各自带一个识别色。 */
@Composable
private fun SudokuSectionHeader(
    title: String,
    theme: SudokuTint
) {
    val palette = sudokuPalette(theme)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing)
    ) {
        Box(
            modifier = Modifier
                .size(width = 4.dp, height = 18.dp)
                .clip(RoundedCornerShape(OneBoxDesignSystem.pillRadius))
                .background(palette.icon)
        )
        OneBoxSectionHeader(title = title)
    }
}

@Composable
private fun ValueSetting(
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        val palette = sudokuPalette(SudokuTint.PRIMARY)
        Box(
            modifier = Modifier
                .size(28.dp)
                .glassBackground(
                    style = GlassStyle.Thin,
                    color = palette.tint,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.LineChevronRight,
                contentDescription = null,
                tint = palette.icon,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun ToggleSetting(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.itemSpacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        GlassSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = AppTheme.colors.switchColors()
        )
    }
}

@Composable
private fun TimerModeSetting(
    selected: SudokuTimerMode,
    onSelect: (SudokuTimerMode) -> Unit
) {
    val shape = RoundedCornerShape(OneBoxDesignSystem.pillRadius)
    val selectedPalette = sudokuPalette(SudokuTint.SECONDARY)
    Column(verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.itemSpacing)) {
        Column {
            Text(
                text = stringResource(R.string.sudoku_settings_timer_mode),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(R.string.sudoku_settings_timer_mode_sub),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        // 三段玻璃分段控件: 比三个 RadioButton 挤在一行更整齐, 选中态走主题色
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .glassBackground(
                    style = GlassStyle.Thin,
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = shape
                )
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            SudokuTimerMode.entries.forEach { mode ->
                val isSelected = mode == selected
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .then(
                            if (isSelected) {
                                Modifier.glassBackground(
                                    style = GlassStyle.Thick,
                                    color = selectedPalette.tint,
                                    shape = shape
                                )
                            } else {
                                Modifier
                            }
                        )
                        .clip(shape)
                        .clickable { onSelect(mode) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = timerModeLabel(mode),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) {
                            selectedPalette.ink
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun timerModeLabel(mode: SudokuTimerMode): String = when (mode) {
    SudokuTimerMode.OFF -> stringResource(R.string.sudoku_timer_off)
    SudokuTimerMode.AUTO -> stringResource(R.string.sudoku_timer_auto)
    SudokuTimerMode.MANUAL -> stringResource(R.string.sudoku_timer_manual)
}

// ─── 默认难度单选对话框 ─────────────────────────────────────────────────────

@Composable
private fun DifficultyDialog(
    selected: SudokuDifficulty,
    onSelect: (SudokuDifficulty) -> Unit,
    onDismiss: () -> Unit
) {
    EnhancedAlertDialog(
        visible = true,
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.sudoku_settings_choose_difficulty))
        },
        text = {
            Column {
                SudokuDifficulty.entries.forEach { difficulty ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(difficulty) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = difficulty == selected,
                            onClick = { onSelect(difficulty) }
                        )
                        Text(
                            text = difficultyLabel(difficulty),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.sudoku_cancel))
            }
        }
    )
}
