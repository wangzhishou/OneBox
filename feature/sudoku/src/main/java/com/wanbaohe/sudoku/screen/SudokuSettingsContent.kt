package com.wanbaohe.sudoku.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.line.LineChevronRight
import com.wanbaohe.core.ui.widget.glass.GlassSwitch
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

            OneBoxSectionHeader(title = stringResource(R.string.sudoku_settings_section_game))
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

            OneBoxSectionHeader(title = stringResource(R.string.sudoku_settings_section_sound))
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
        Icon(
            imageVector = Icons.Outlined.LineChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
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
        SudokuTimerMode.entries.forEach { mode ->
            Row(
                modifier = Modifier.clickable { onSelect(mode) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = mode == selected,
                    onClick = { onSelect(mode) }
                )
                Text(
                    text = timerModeLabel(mode),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
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
    AlertDialog(
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
