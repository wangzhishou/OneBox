package com.wanbaohe.sudoku.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.common.ui.BaseScreen
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.Lightbulb
import com.t8rin.imagetoolbox.core.resources.icons.line.LineBackspace
import com.t8rin.imagetoolbox.core.resources.icons.line.LineBarChart
import com.t8rin.imagetoolbox.core.resources.icons.line.LineGrid4x4
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePause
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePlay
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTimer
import com.t8rin.imagetoolbox.core.resources.icons.line.LineUndo
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassThick
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassThin
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDesignSystem
import com.wanbaohe.sudoku.R
import com.wanbaohe.sudoku.component.SudokuComponent
import com.wanbaohe.sudoku.component.SudokuUiState
import com.wanbaohe.sudoku.data.SudokuTimerMode
import com.wanbaohe.sudoku.logic.SudokuEngine
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedDropdownMenu
import androidx.compose.material.icons.Icons as MaterialIcons
import androidx.compose.material.icons.filled.MoreVert

/**
 * 核心层: 对局页。顶栏右侧 = 提示/撤销/溢出菜单;
 * 9×9 棋盘 + 信息行三卡(难度/已填/用时) + 两行数字键盘。
 */
@Composable
fun SudokuGameContent(
    component: SudokuComponent,
    bottomInset: Dp = 0.dp
) {
    val state by component.uiState.collectAsState()

    BaseScreen(
        modifier = Modifier.padding(bottom = bottomInset),
        title = stringResource(R.string.sudoku_title),
        onGoBack = component::onTabBack,
        actions = {
            IconButton(
                onClick = component::onHint,
                enabled = state.settings.hintEnabled
            ) {
                Icon(
                    imageVector = Icons.Rounded.Lightbulb,
                    contentDescription = stringResource(R.string.sudoku_hint)
                )
            }
            IconButton(
                onClick = component::onUndo,
                enabled = state.undoDepth > 0
            ) {
                Icon(
                    imageVector = Icons.Outlined.LineUndo,
                    contentDescription = stringResource(R.string.sudoku_undo)
                )
            }
            GameOverflowMenu(component = component)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = OneBoxDesignSystem.screenPadding)
        ) {
            Spacer(modifier = Modifier.weight(1f))

            SudokuBoard(
                state = state,
                onCellClick = component::onCellSelected,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )

            Spacer(modifier = Modifier.height(OneBoxDesignSystem.blockSpacing))

            InfoRow(state = state, onTimeCardClick = component::onTimeCardClick)

            Spacer(modifier = Modifier.weight(1f))

            NumberPad(state = state, component = component)

            Spacer(modifier = Modifier.height(OneBoxDesignSystem.blockSpacing))
        }
    }
}

@Composable
private fun GameOverflowMenu(component: SudokuComponent) {
    var showMenu by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { showMenu = true }) {
            Icon(
                imageVector = MaterialIcons.Filled.MoreVert,
                contentDescription = stringResource(R.string.sudoku_more)
            )
        }
        EnhancedDropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ) {
            DropdownMenuItem(
                text = { Text(text = stringResource(R.string.sudoku_menu_restart)) },
                onClick = {
                    showMenu = false
                    component.onRestart()
                }
            )
            DropdownMenuItem(
                text = { Text(text = stringResource(R.string.sudoku_menu_change_difficulty)) },
                onClick = {
                    showMenu = false
                    component.onChangeDifficulty()
                }
            )
        }
    }
}

// ─── 棋盘 ──────────────────────────────────────────────────────────────────

/** 格子间距; 宫与宫之间在这个基础上再加一条, 形成 3×3 粗间隔。 */
private val CELL_GAP = 4.dp
private val BOX_EXTRA_GAP = 4.dp
private val BOARD_PADDING = 10.dp

@Composable
private fun SudokuBoard(
    state: SudokuUiState,
    onCellClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .glassThick(shape = RoundedCornerShape(OneBoxDesignSystem.largeRadius))
            .padding(BOARD_PADDING)
    ) {
        // maxWidth 已是扣掉 BOARD_PADDING 之后的内容宽度, 不用再减一遍
        val totalGap = CELL_GAP * 8 + BOX_EXTRA_GAP * 2
        val fontSize = ((maxWidth - totalGap) / 9).value * 0.45f

        Column(verticalArrangement = Arrangement.spacedBy(CELL_GAP)) {
            for (row in 0 until 9) {
                if (row == 3 || row == 6) {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(BOX_EXTRA_GAP)
                    )
                }
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(CELL_GAP)
                ) {
                    for (col in 0 until 9) {
                        if (col == 3 || col == 6) {
                            Spacer(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(BOX_EXTRA_GAP)
                            )
                        }
                        SudokuCell(
                            state = state,
                            index = row * 9 + col,
                            fontSize = fontSize,
                            onClick = onCellClick,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SudokuCell(
    state: SudokuUiState,
    index: Int,
    fontSize: Float,
    onClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val puzzle = state.puzzle
    val entries = state.entries
    if (puzzle.size != SudokuEngine.BOARD_CELLS || entries.size != SudokuEngine.BOARD_CELLS) return

    val given = puzzle[index] != 0
    val entry = entries[index]
    val selected = state.selectedIndex == index
    val selectedEntry = state.selectedIndex?.let { entries.getOrNull(it) } ?: 0
    val related = state.selectedIndex?.let { isRelated(index, it) } == true
    val sameNumber = !selected && entry != 0 && selectedEntry != 0 && entry == selectedEntry
    val isError = state.settings.autoCheck && !given && entry != 0 &&
        state.solution.getOrNull(index)?.let { it != entry } == true

    val containerColor = when {
        selected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.32f)
        isError -> MaterialTheme.colorScheme.errorContainer
        sameNumber -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f)
        related -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.08f)
        else -> MaterialTheme.colorScheme.surfaceContainer
    }
    val digitColor = when {
        isError -> MaterialTheme.colorScheme.error
        given -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onPrimaryContainer
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .glassThin(shape = RoundedCornerShape(6.dp), color = containerColor)
            .clickable { onClick(index) },
        contentAlignment = Alignment.Center
    ) {
        if (entry != 0) {
            Text(
                text = entry.toString(),
                fontSize = fontSize.sp,
                fontWeight = if (given) FontWeight.Bold else FontWeight.Medium,
                color = digitColor
            )
        }
    }
}

/** 同行 / 同列 / 同宫。 */
private fun isRelated(index: Int, other: Int): Boolean {
    if (index == other) return false
    val row = index / 9
    val col = index % 9
    val otherRow = other / 9
    val otherCol = other % 9
    return row == otherRow || col == otherCol ||
        (row / 3 == otherRow / 3 && col / 3 == otherCol / 3)
}

// ─── 信息行 ────────────────────────────────────────────────────────────────

@Composable
private fun InfoRow(
    state: SudokuUiState,
    onTimeCardClick: () -> Unit
) {
    val filledCount = state.entries.count { it != 0 }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing)
    ) {
        InfoCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Outlined.LineBarChart,
            label = stringResource(R.string.sudoku_stat_difficulty),
            value = difficultyLabel(state.difficulty)
        )
        InfoCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Outlined.LineGrid4x4,
            label = stringResource(R.string.sudoku_stat_filled),
            value = stringResource(R.string.sudoku_stat_filled_value, filledCount)
        )
        // 计时模式 = 关闭时藏掉用时卡; 手动模式点卡片开始/暂停
        if (state.settings.timerMode != SudokuTimerMode.OFF) {
            val manual = state.settings.timerMode == SudokuTimerMode.MANUAL
            InfoCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.LineTimer,
                label = stringResource(R.string.sudoku_stat_time),
                value = formatSudokuTime(state.elapsedSeconds),
                onClick = if (manual) onTimeCardClick else null,
                trailing = if (manual) {
                    {
                        Icon(
                            imageVector = if (state.timerRunning) {
                                Icons.Outlined.LinePause
                            } else {
                                Icons.Outlined.LinePlay
                            },
                            contentDescription = stringResource(
                                if (state.timerRunning) {
                                    R.string.sudoku_timer_pause
                                } else {
                                    R.string.sudoku_timer_start
                                }
                            ),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else null
            )
        }
    }
}

@Composable
private fun InfoCard(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    val shape = RoundedCornerShape(OneBoxDesignSystem.mediumRadius)
    Row(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(20.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        trailing?.invoke()
    }
}

// ─── 数字键盘 ──────────────────────────────────────────────────────────────

@Composable
private fun NumberPad(
    state: SudokuUiState,
    component: SudokuComponent
) {
    // 某数字在盘上已满 9 个 → 置灰禁用
    val digitCounts = remember(state.entries) {
        IntArray(10) { digit -> state.entries.count { it == digit } }
    }
    val eraseEnabled = state.selectedIndex?.let { index ->
        state.puzzle.getOrNull(index) == 0 && state.entries.getOrNull(index)?.let { it != 0 } == true
    } == true

    Column(verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing)) {
        Row(horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing)) {
            for (digit in 1..5) {
                NumberKey(
                    modifier = Modifier.weight(1f),
                    digit = digit,
                    enabled = digitCounts[digit] < 9,
                    onClick = { component.onDigitInput(digit) }
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing)) {
            for (digit in 6..9) {
                NumberKey(
                    modifier = Modifier.weight(1f),
                    digit = digit,
                    enabled = digitCounts[digit] < 9,
                    onClick = { component.onDigitInput(digit) }
                )
            }
            KeyButton(
                modifier = Modifier.weight(1f),
                enabled = eraseEnabled,
                onClick = component::onErase
            ) {
                Icon(
                    imageVector = Icons.Outlined.LineBackspace,
                    contentDescription = stringResource(R.string.sudoku_erase),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun NumberKey(
    digit: Int,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    KeyButton(
        modifier = modifier,
        enabled = enabled,
        onClick = onClick
    ) {
        Text(
            text = digit.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun KeyButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .height(52.dp)
            .alpha(if (enabled) 1f else 0.35f)
            .clip(RoundedCornerShape(OneBoxDesignSystem.mediumRadius))
            .background(MaterialTheme.colorScheme.surface)
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

/** MM:SS 格式化, 超过 99 分钟也不截断(分钟位自然变长)。 */
internal fun formatSudokuTime(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}
