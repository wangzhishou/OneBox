package com.wanbaohe.minesweeper.screen

import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.common.ui.rememberImmersiveModeState
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.Fullscreen
import com.t8rin.imagetoolbox.core.resources.icons.Refresh
import com.t8rin.imagetoolbox.core.resources.icons.Share
import com.t8rin.imagetoolbox.core.resources.icons.VolumeOff
import com.t8rin.imagetoolbox.core.resources.icons.VolumeUp
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFlag
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMinesweeper
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTimer
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTouchApp
import com.wanbaohe.core.ui.utils.capturable.capturable
import com.wanbaohe.core.ui.utils.capturable.rememberCaptureController
import com.wanbaohe.core.ui.widget.glass.GlassTonalButton
import com.wanbaohe.core.ui.widget.glass.GlassTonalIconButton
import com.wanbaohe.core.ui.widget.glass.glassThick
import com.wanbaohe.core.ui.widget.glass.glassThin
import com.wanbaohe.minesweeper.R
import com.wanbaohe.minesweeper.component.MinesweeperComponent
import com.wanbaohe.minesweeper.logic.BOARD_COLS
import com.wanbaohe.minesweeper.logic.BOARD_ROWS_DEFAULT
import com.wanbaohe.minesweeper.logic.BOARD_ROWS_MAX
import com.wanbaohe.minesweeper.logic.BOARD_ROWS_MIN
import com.wanbaohe.minesweeper.logic.Cell
import com.wanbaohe.minesweeper.logic.GameState
import kotlinx.coroutines.launch

/**
 * 扫雷主页面。
 *
 * 交互说明(这里跟经典"点=挖 / 长按=插旗"不一样, 见 [MinesweeperComponent.onCellClicked]):
 * - 底部 icon bar 第一个按钮切换"挖雷 / 插旗"模式, 单击永远按当前模式走, 长按恒等于插旗。
 *   纯长按方案在触屏上很容易误判: 按到 300ms 松手系统算点击, 直接把雷挖了。
 *   给一个显式开关之后, 长按只作为老玩家的快捷方式保留。
 * - 已翻开的数字, 周围旗数够了一次点开一圈(和弦)。
 */
@Composable
fun MinesweeperScreen(
    component: MinesweeperComponent
) {
    val state by component.uiState.collectAsState()
    // 全屏(沉浸)状态: 收起标题栏与信息条, 只留棋盘
    val immersiveState = rememberImmersiveModeState()
    val captureController = rememberCaptureController()
    val scope = rememberCoroutineScope()

    ImmersiveSystemBars(enabled = immersiveState.isImmersive)

    BaseScreen(
        title = stringResource(R.string.minesweeper_title),
        onGoBack = component.onGoBack,
        immersiveModeState = immersiveState,
        // 不传 actions: 用 BaseScreen 的默认 action(主题快捷设置)。
        // 游戏本身的控制全在底部 icon bar, 全屏时标题栏收起也摸得到。
        //
        // 结算层挂 BaseScreen 的 foreground: 它落在根 Box 上、在 TopAppBar 之外,
        // 所以能盖住整屏(含标题栏); 塞在棋盘那个 Box 里就只能盖住棋盘。
        foreground = {
            val finished = state.gameState == GameState.WON || state.gameState == GameState.LOST
            AnimatedVisibility(
                visible = finished,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                ResultOverlay(
                    isWon = state.gameState == GameState.WON,
                    seconds = state.timer,
                    onRestart = component::resetGame
                )
            }
        }
    ) {
        // 外层只留纵向 padding: 棋盘要顶到屏幕两边, 横向间距交给信息条和底部 bar 自己加
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp)
        ) {
            AnimatedVisibility(
                visible = immersiveState.isUiVisible,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                StatRow(
                    timer = state.timer,
                    minesLeft = state.minesLeft,
                    modifier = Modifier.padding(
                        start = BAR_SIDE_PADDING,
                        end = BAR_SIDE_PADDING,
                        bottom = BOARD_SPACING
                    )
                )
            }

            val rows = state.board.size
            val cols = state.board.firstOrNull()?.size ?: 0

            // 先用剩余区域算出格子边长, 玻璃外框再贴合棋盘本身,
            // 最后整体垂直居中 —— 不这么做的话盘会在框里空出两大块玻璃
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = BAR_SIDE_PADDING, vertical = BOARD_SPACING)
            ) {
                // 行数反过来由"能塞几行"决定, 这样宽度永远吃得满, 棋盘两边不会空出一条
                val areaWidth = maxWidth
                val areaHeight = maxHeight
                val fitRows = remember(areaWidth, areaHeight, cols) {
                    fitBoardRows(areaWidth, areaHeight, cols)
                }
                LaunchedEffect(fitRows) {
                    component.applyBoardRows(fitRows)
                }

                if (rows == 0) return@BoxWithConstraints

                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    // 先把玻璃外框的内边距扣掉再算, 否则算出来的盘会比外框还宽
                    val usableWidth = areaWidth - FRAME_PADDING * 2
                    val usableHeight = areaHeight - FRAME_PADDING * 2
                    val cellSize = maxOf(
                        minOf(
                            (usableWidth - CELL_GAP * (cols - 1)) / cols,
                            (usableHeight - CELL_GAP * (rows - 1)) / rows
                        ),
                        MIN_CELL_SIZE
                    )
                    // 极窄屏才会走到这里: 格子顶到最小尺寸后仍然放不下
                    val overflow =
                        cellSize * cols + CELL_GAP * (cols - 1) + FRAME_PADDING * 2 > areaWidth ||
                            cellSize * rows + CELL_GAP * (rows - 1) + FRAME_PADDING * 2 > areaHeight

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .then(if (overflow) Modifier.verticalScroll(rememberScrollState()) else Modifier),
                        contentAlignment = if (overflow) Alignment.TopCenter else Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .capturable(captureController)
                                .glassThick(shape = RoundedCornerShape(16.dp))
                                .padding(FRAME_PADDING)
                        ) {
                            BoardGrid(
                                board = state.board,
                                cellSize = cellSize,
                                onCellClick = { row, col -> component.onCellClicked(row, col) },
                                onCellLongClick = { row, col -> component.onCellLongClicked(row, col) }
                            )
                        }
                    }

                    // 全屏时顶上的信息条被收起来了, 剩几颗雷就没人知道了 ——
                    // 用一块浮在棋盘顶上的小 chip 补回来, 不占布局高度
                    // 包一层 Column: 外面那个 Column 的 receiver 还在隐式作用域里,
                    // 直接在这里调 AnimatedVisibility 会被解析成 ColumnScope 那个重载
                    Column(modifier = Modifier.align(Alignment.TopCenter)) {
                        AnimatedVisibility(
                            visible = immersiveState.isImmersive,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            ImmersiveStats(
                                timer = state.timer,
                                minesLeft = state.minesLeft
                            )
                        }
                    }
                }
            }

            BottomBar(
                modifier = Modifier.padding(horizontal = BAR_SIDE_PADDING),
                flagMode = state.flagMode,
                soundEnabled = state.soundEnabled,
                immersive = immersiveState.isImmersive,
                onToggleFlagMode = component::toggleFlagMode,
                onToggleSound = component::toggleSound,
                onToggleImmersive = immersiveState::toggle,
                onRestart = component::resetGame,
                onShare = {
                    scope.launch {
                        runCatching {
                            component.shareBitmap(captureController.bitmap())
                        }
                    }
                }
            )
        }
    }
}

/**
 * 隐藏 / 显示系统栏。只切系统栏本身, 不去碰 WindowCompat.setDecorFitsSystemWindows,
 * 免得整个 Activity 的布局算法被改掉。退出页面时强制恢复, 不会把别的页面留成 immersion。
 */
@Composable
private fun ImmersiveSystemBars(enabled: Boolean) {
    val window = LocalActivity.current?.window
    DisposableEffect(enabled, window) {
        val controller = window?.let { WindowInsetsControllerCompat(it, it.decorView) }
        if (controller != null) {
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            if (enabled) {
                controller.hide(WindowInsetsCompat.Type.systemBars())
            } else {
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
        }
        onDispose {
            window?.let {
                WindowInsetsControllerCompat(it, it.decorView)
                    .show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }
}

@Composable
private fun StatRow(
    timer: Int,
    minesLeft: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatChip(
            modifier = Modifier.weight(1f),
            icon = {
                Icon(
                    imageVector = Icons.Outlined.LineTimer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            },
            label = stringResource(R.string.minesweeper_time),
            value = timer.toString()
        )
        StatChip(
            modifier = Modifier.weight(1f),
            icon = {
                Icon(
                    imageVector = Icons.Outlined.LineMinesweeper,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(20.dp)
                )
            },
            label = stringResource(R.string.minesweeper_mines_left),
            value = minesLeft.toString()
        )
    }
}

@Composable
private fun StatChip(
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    label: String,
    value: String
) {
    Row(
        modifier = modifier
            .glassThin(shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        icon()
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * 底部 icon bar: 左边一行操作提示, 右边是模式 / 重开 / 声音 / 全屏 / 分享。
 * 放在底部是因为这些控制要在全屏模式下也能摸到, 标题栏全屏时是被收起来的。
 */
@Composable
private fun BottomBar(
    flagMode: Boolean,
    soundEnabled: Boolean,
    immersive: Boolean,
    onToggleFlagMode: () -> Unit,
    onToggleSound: () -> Unit,
    onToggleImmersive: () -> Unit,
    onRestart: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .glassThin(shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(
                if (flagMode) R.string.minesweeper_hint_flag else R.string.minesweeper_hint_dig
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        )

        // 模式开关: 插旗模式下填充高亮, 让人一眼看出当前是哪种
        GlassTonalIconButton(
            onClick = onToggleFlagMode,
            modifier = Modifier.size(48.dp),
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = if (flagMode) {
                    MaterialTheme.colorScheme.tertiaryContainer
                } else {
                    MaterialTheme.colorScheme.secondaryContainer
                },
                contentColor = if (flagMode) {
                    MaterialTheme.colorScheme.onTertiaryContainer
                } else {
                    MaterialTheme.colorScheme.onSecondaryContainer
                }
            )
        ) {
            Icon(
                imageVector = if (flagMode) Icons.Outlined.LineFlag else Icons.Outlined.LineTouchApp,
                contentDescription = stringResource(
                    if (flagMode) R.string.minesweeper_mode_flag else R.string.minesweeper_mode_dig
                ),
                modifier = Modifier.size(24.dp)
            )
        }

        BarIconButton(
            onClick = onRestart,
            imageVector = Icons.Outlined.Refresh,
            contentDescription = stringResource(R.string.minesweeper_restart)
        )
        BarIconButton(
            onClick = onToggleSound,
            imageVector = if (soundEnabled) Icons.Outlined.VolumeUp else Icons.Outlined.VolumeOff,
            contentDescription = stringResource(
                if (soundEnabled) R.string.minesweeper_sound_on else R.string.minesweeper_sound_off
            )
        )
        BarIconButton(
            onClick = onToggleImmersive,
            imageVector = Icons.Outlined.Fullscreen,
            contentDescription = stringResource(
                if (immersive) R.string.minesweeper_exit_fullscreen else R.string.minesweeper_fullscreen
            ),
            tint = if (immersive) MaterialTheme.colorScheme.onPrimaryContainer else Color.Unspecified
        )
        BarIconButton(
            onClick = onShare,
            imageVector = Icons.Outlined.Share,
            contentDescription = stringResource(R.string.minesweeper_share)
        )
    }
}

@Composable
private fun BarIconButton(
    onClick: () -> Unit,
    imageVector: ImageVector,
    contentDescription: String,
    tint: Color = Color.Unspecified
) {
    GlassTonalIconButton(
        onClick = onClick,
        modifier = Modifier.size(48.dp)
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            // Unspecified = 跟随 IconButton 的 contentColor, 只有全屏激活态才特意高亮
            tint = if (tint == Color.Unspecified) LocalContentColor.current else tint,
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * 棋盘。格子边长由外层按可用区域算好传进来, 这里只管摆格子;
 * 行数多于列数, 所以整块是竖向的, 外层的玻璃框正好贴合它。
 */
@Composable
private fun BoardGrid(
    board: List<List<Cell>>,
    cellSize: Dp,
    onCellClick: (row: Int, col: Int) -> Unit,
    onCellLongClick: (row: Int, col: Int) -> Unit
) {
    if (board.isEmpty()) return
    // 格子太多时逐格画玻璃开销偏大, 退化成纯色
    val useCellGlass = board.size * board[0].size <= CELL_GLASS_LIMIT

    Column(verticalArrangement = Arrangement.spacedBy(CELL_GAP)) {
        board.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(CELL_GAP)) {
                row.forEach { cell ->
                    CellView(
                        cell = cell,
                        size = cellSize,
                        useGlass = useCellGlass,
                        onClick = { onCellClick(cell.row, cell.col) },
                        onLongClick = { onCellLongClick(cell.row, cell.col) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CellView(
    cell: Cell,
    size: Dp,
    useGlass: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.86f else 1f,
        label = "cellPress"
    )
    val shape = RoundedCornerShape(8.dp)
    val containerColor = when {
        !cell.isRevealed -> MaterialTheme.colorScheme.primaryContainer
        cell.isMine -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surfaceContainer
    }
    val fontSize = (size.value * 0.5f).sp
    val iconSize = size * 0.6f

    Box(
        modifier = Modifier
            .size(size)
            .pointerInput(cell.row, cell.col) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        try {
                            awaitRelease()
                        } finally {
                            pressed = false
                        }
                    },
                    onTap = { onClick() },
                    onLongPress = {
                        // 长按成功给一次震动, 不然手指盖着格子, 根本不知道插上旗没有
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLongClick()
                    }
                )
            }
            .scale(scale)
            .then(
                if (!cell.isRevealed && useGlass) {
                    Modifier.glassThin(shape = shape, color = containerColor)
                } else {
                    Modifier
                        .clip(shape)
                        .background(
                            if (cell.isRevealed) containerColor.copy(alpha = 0.35f) else containerColor
                        )
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        when {
            cell.isRevealed && cell.isMine -> {
                Icon(
                    imageVector = Icons.Outlined.LineMinesweeper,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(iconSize)
                )
            }

            cell.isRevealed && cell.neighborMines > 0 -> {
                Text(
                    text = cell.neighborMines.toString(),
                    color = numberColor(cell.neighborMines),
                    fontWeight = FontWeight.Bold,
                    fontSize = fontSize
                )
            }

            !cell.isRevealed && cell.isFlagged -> {
                Icon(
                    imageVector = Icons.Outlined.LineFlag,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(iconSize)
                )
            }
        }
    }
}

/**
 * 按可用区域反推行数: 先按宽度定死格子边长(列数固定, 所以边长只跟宽度有关),
 * 再看这个高度里塞得下几行。
 *
 * 这样宽度永远是吃满的 —— 反过来写死行数的话, 高度会先撑满、格子被迫缩小,
 * 棋盘跟着变窄, 两边空出一条跟操作栏也对不齐。
 */
private fun fitBoardRows(maxWidth: Dp, maxHeight: Dp, cols: Int): Int {
    if (cols <= 0) return BOARD_ROWS_DEFAULT
    val innerWidth = maxWidth - FRAME_PADDING * 2
    val innerHeight = maxHeight - FRAME_PADDING * 2
    val cellWidth = (innerWidth - CELL_GAP * (cols - 1)) / cols
    if (cellWidth <= 0.dp) return BOARD_ROWS_DEFAULT
    val fit = ((innerHeight + CELL_GAP) / (cellWidth + CELL_GAP)).toInt()
    return fit.coerceIn(BOARD_ROWS_MIN, BOARD_ROWS_MAX)
}

/**
 * 全屏模式下浮在棋盘顶上的精简信息。
 * 只显示两个数字 + 图标: 这时候人要看的是"还剩几颗雷", 不是"剩余地雷"这四个字。
 */
@Composable
private fun ImmersiveStats(
    timer: Int,
    minesLeft: Int
) {
    Row(
        modifier = Modifier
            .padding(top = 6.dp)
            .glassThin(shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ImmersiveStatValue(
            icon = Icons.Outlined.LineTimer,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            value = timer.toString()
        )
        ImmersiveStatValue(
            icon = Icons.Outlined.LineMinesweeper,
            tint = MaterialTheme.colorScheme.onErrorContainer,
            value = minesLeft.toString()
        )
    }
}

@Composable
private fun ImmersiveStatValue(
    icon: ImageVector,
    tint: Color,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/** 经典扫雷配色, 深浅色主题下都能看清。 */
private fun numberColor(count: Int): Color = when (count) {
    1 -> Color(0xFF4C8DFF)
    2 -> Color(0xFF2BB673)
    3 -> Color(0xFFFF5C5C)
    4 -> Color(0xFF9C6BFF)
    5 -> Color(0xFFFF9F43)
    6 -> Color(0xFF26C6DA)
    7 -> Color(0xFFFFCA28)
    else -> Color(0xFFB0BEC5)
}

@Composable
private fun ResultOverlay(
    isWon: Boolean,
    seconds: Int,
    onRestart: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.55f))
            // 吃掉所有点击: 盖住整屏之后, 底下那层棋盘还在, 不拦一下会继续翻格子
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            ),
        contentAlignment = Alignment.Center
    ) {
        val accentColor = if (isWon) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onErrorContainer
        }

        // 结算卡片是弹层容器, 按仓库约定不走玻璃管线: 玻璃底覆盖率很低,
        // 压在棋盘上会透出格子, 结果文字糊成一团。这里给 M3 实底
        // surfaceContainerLowest, 任何棋盘底色下都看得清。
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            shadowElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = if (isWon) Icons.Outlined.LineFlag else Icons.Outlined.LineMinesweeper,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(40.dp)
                )
                Text(
                    text = stringResource(if (isWon) R.string.minesweeper_won else R.string.minesweeper_lost),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.minesweeper_result_detail, seconds),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                GlassTonalButton(
                    onClick = onRestart,
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(R.string.minesweeper_play_again))
                }
            }
        }
    }
}

/** 格子间距 */
private val CELL_GAP = 4.dp
/** 棋盘玻璃外框的内边距 */
private val FRAME_PADDING = 6.dp
/** 棋盘与上方信息条 / 下方 icon bar 之间的呼吸空间 */
private val BOARD_SPACING = 16.dp
/** 信息条与底部 bar 的左右留白; 棋盘不吃这个值, 它顶到屏幕两边 */
private val BAR_SIDE_PADDING = 8.dp
/** 格子最小边长: 再小手指就点不准了, 低于这个值就允许滚动 */
private val MIN_CELL_SIZE = 26.dp
private const val CELL_GLASS_LIMIT = 400
