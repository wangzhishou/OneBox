package com.wanbaohe.xiangqi.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.VolumeOff
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.common.ui.BaseScreen
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSurface
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalButton
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalIconButton
import com.wanbaohe.xiangqi.R
import com.wanbaohe.xiangqi.application.dto.NotationRow
import com.wanbaohe.xiangqi.application.dto.NotationRows
import com.wanbaohe.xiangqi.application.dto.PlyRecord
import com.wanbaohe.xiangqi.component.XiangqiAnalysisComponent
import com.wanbaohe.xiangqi.data.TextExportLabels
import com.wanbaohe.xiangqi.presentation.localizedGameResultText
import com.wanbaohe.xiangqi.ui.board.XiangqiBoard
import com.t8rin.imagetoolbox.core.resources.icons.line.LineChevronLeft
import com.t8rin.imagetoolbox.core.resources.icons.line.LineChevronRight
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDownload
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMemory
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePauseBars
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePlay
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSkipNext
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSkipPrevious

@Composable
fun XiangqiAnalysisScreen(
    component: XiangqiAnalysisComponent,
    modifier: Modifier = Modifier,
    showChrome: Boolean = true,
) {
    val state = component.uiState
    var exportDialog by remember { mutableStateOf(false) }
    val exportLabels = TextExportLabels(
        header = stringResource(R.string.xiangqi_export_header),
        titleLabel = stringResource(R.string.xiangqi_export_title_label),
        initialFenLabel = stringResource(R.string.xiangqi_export_initial_fen_label),
        resultLabel = stringResource(R.string.xiangqi_export_result_label),
    )
    // 结果文案在 UI 层本地化后传给导出，导出层不依赖 Android 资源。
    // 用落库的 resultText（整局恒定），不是回放进度推出来的盘面状态。
    val resultText = localizedGameResultText(state.resultText)

    val content: @Composable (Modifier) -> Unit = { contentModifier ->
        XiangqiAnalysisContent(
            component = component,
            modifier = contentModifier,
            onExport = { exportDialog = true },
            resultText = resultText,
        )
    }

    if (showChrome) {
        BaseScreen(
            title = state.title.ifBlank { stringResource(R.string.xiangqi_analysis_title) },
            onGoBack = component.onGoBack,
        ) {
            content(Modifier.fillMaxSize())
        }
    } else {
        content(modifier)
    }

    if (exportDialog) {
        ExportDialog(
            exportContent = state.exportContent,
            onExportFen = component::exportFen,
            onExportJson = component::exportJson,
            onExportText = { component.exportText(exportLabels, resultText) },
            onDismiss = {
                exportDialog = false
                component.dismissExport()
            },
        )
    }
}

@Composable
private fun XiangqiAnalysisContent(
    component: XiangqiAnalysisComponent,
    modifier: Modifier,
    onExport: () -> Unit,
    resultText: String,
) {
    val state = component.uiState
    val rows = remember(state.plies) { NotationRows.of(state.plies) }
    val listState = rememberLazyListState()

    // 自动播放/跳转时把当前手滚进可视区
    LaunchedEffect(state.currentPly, rows.size) {
        val index = rows.indexOfFirst { row ->
            row.red?.ply == state.currentPly || row.black?.ply == state.currentPly
        }
        if (index >= 0) listState.animateScrollToItem(index)
    }

    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        XiangqiBoard(
            boardState = state.boardState,
            selectedPoint = null,
            candidateTargets = emptySet(),
            onCellTap = { _, _ -> },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
        )

        ReplayControls(
            isAutoPlaying = state.isAutoPlaying,
            onStart = component::goToStart,
            onPrev = component::goPrev,
            onToggleAutoPlay = component::toggleAutoPlay,
            onNext = component::goNext,
            onEnd = component::goToEnd,
        )

        if (resultText.isNotBlank()) {
            Text(
                text = resultText,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        // 未终局的局从卡片「复盘」进来后，必须有路回对局页；已结束的局没有可继续的下法
        if (resultText.isBlank()) {
            GlassTonalButton(
                onClick = component::openCurrentGame,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMemory,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(stringResource(R.string.xiangqi_analysis_back_to_game))
                }
            }
        }

        GlassSurface(
            // weight 让面板占据剩余高度：棋盘与导出入口固定，只有着法表内部滚动。
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            style = GlassStyle.Medium,
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                AnalysisPanelHeader(
                    moveCount = state.plies.size,
                    isSoundOn = state.isSoundOn,
                    onToggleSound = component::toggleSound,
                    onExport = onExport,
                )

                if (rows.isEmpty()) {
                    Text(
                        text = stringResource(R.string.xiangqi_no_history),
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = 8.dp,
                            vertical = 8.dp,
                        ),
                    ) {
                        items(rows, key = { it.turn }) { row ->
                            NotationRowItem(
                                row = row,
                                currentPly = state.currentPly,
                                onPlyClick = component::goToPly,
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 面板标题栏。
 *
 * 导出入口必须固定在**滚动容器之外**：放在着法表内部时，长棋谱会把它推出屏幕，
 * 用户得先滚到底再滚回来才能导出。
 */
@Composable
private fun AnalysisPanelHeader(
    moveCount: Int,
    isSoundOn: Boolean,
    onToggleSound: () -> Unit,
    onExport: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.xiangqi_analysis_panel_title),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
            ),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.xiangqi_ply_count, moveCount),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            // 声音总开关：关掉后这一步的音效和背景音乐一起停
            GlassTonalIconButton(onClick = onToggleSound) {
                Icon(
                    imageVector = if (isSoundOn) {
                        Icons.AutoMirrored.Outlined.VolumeUp
                    } else {
                        Icons.AutoMirrored.Outlined.VolumeOff
                    },
                    contentDescription = stringResource(
                        if (isSoundOn) R.string.xiangqi_analysis_sound_on else R.string.xiangqi_analysis_sound_off,
                    ),
                    modifier = Modifier.size(18.dp),
                )
            }
            GlassTonalIconButton(onClick = onExport) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDownload,
                    contentDescription = stringResource(R.string.xiangqi_export),
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

/**
 * 一行着法。红/黑两列各自可点，点击跳到该手（打谱动线）。
 *
 * 黑先局面的首行 [NotationRow.red] 为 null，渲染成省略占位。
 */
@Composable
private fun NotationRowItem(
    row: NotationRow,
    currentPly: Int,
    onPlyClick: (Int) -> Unit,
) {
    val isRedActive = row.red?.ply == currentPly
    val isBlackActive = row.black?.ply == currentPly

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isRedActive || isBlackActive) {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                } else {
                    Color.Transparent
                },
            )
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "${row.turn}.",
            modifier = Modifier.weight(0.15f),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
        MoveCell(
            ply = row.red,
            isActive = isRedActive,
            activeColor = MaterialTheme.colorScheme.onPrimaryContainer,
            idleColor = MaterialTheme.colorScheme.error,
            modifier = Modifier.weight(0.425f),
            onPlyClick = onPlyClick,
        )
        MoveCell(
            ply = row.black,
            isActive = isBlackActive,
            activeColor = MaterialTheme.colorScheme.onPrimaryContainer,
            idleColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            modifier = Modifier.weight(0.425f),
            onPlyClick = onPlyClick,
        )
    }
}

@Composable
private fun MoveCell(
    ply: PlyRecord?,
    isActive: Boolean,
    activeColor: Color,
    idleColor: Color,
    modifier: Modifier,
    onPlyClick: (Int) -> Unit,
) {
    Text(
        text = ply?.displayText() ?: "...",
        modifier = modifier.then(
            if (ply != null) Modifier.clickable { onPlyClick(ply.ply) } else Modifier,
        ),
        color = if (isActive) activeColor else idleColor,
        style = MaterialTheme.typography.bodyMedium.copy(
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
        ),
    )
}

private fun PlyRecord.displayText(): String = moveCn.ifBlank { moveUcci }

@Composable
private fun ReplayControls(
    isAutoPlaying: Boolean,
    onStart: () -> Unit,
    onPrev: () -> Unit,
    onToggleAutoPlay: () -> Unit,
    onNext: () -> Unit,
    onEnd: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 首/尾用「竖线+三角」的 skip 图标：语义明确（跳到两端，不是逐手）
        GlassTonalIconButton(onClick = onStart) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSkipPrevious,
                contentDescription = stringResource(R.string.xiangqi_replay_to_start),
                // 该图标路径几乎铺满 24 视口（y 1.3-22.7），与 chevron/播放三角并排时
                // 显得大一圈。**在调用处**缩放而不是改图标本身：LineSkipPrevious/Next
                // 是全局共享图标，还被 core/ui 的图标选择器（ImageVectorMap）列出，
                // 改全局会影响全 App 其它用到它的地方。
                modifier = Modifier.scale(SKIP_ICON_SCALE),
            )
        }
        // 单步用 chevron：比"无竖线的 skip"更轻，且与首/尾不混淆
        GlassTonalIconButton(onClick = onPrev) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineChevronLeft,
                contentDescription = stringResource(R.string.xiangqi_replay_prev),
            )
        }
        GlassTonalIconButton(
            onClick = onToggleAutoPlay,
            modifier = Modifier.size(52.dp),
        ) {
            Icon(
                // 纯三角播放键，无外圈：外层按钮本身就是圆形玻璃底，
                // 再套一个圆形图标会变成"圆套圆"
                imageVector = if (isAutoPlaying) com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePauseBars else com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePlay,
                contentDescription = stringResource(
                    if (isAutoPlaying) R.string.xiangqi_replay_pause else R.string.xiangqi_replay_auto_play,
                ),
                modifier = Modifier.size(28.dp),
            )
        }
        GlassTonalIconButton(onClick = onNext) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineChevronRight,
                contentDescription = stringResource(R.string.xiangqi_replay_next),
            )
        }
        GlassTonalIconButton(onClick = onEnd) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSkipNext,
                contentDescription = stringResource(R.string.xiangqi_replay_to_end),
                modifier = Modifier.scale(SKIP_ICON_SCALE),
            )
        }
    }
}

/**
 * 首/尾 skip 图标的局部缩放。
 *
 * 这两个图标在 24 视口里纵向铺满（1.3~22.7），而相邻的 chevron 与播放三角
 * 只占约 12~14 单位，并排时 skip 会显得大一圈。缩到 ~78% 让留白与其它键一致。
 *
 * 只在调用处缩放、不改图标本体：它们被 core/ui 的图标选择器全局引用。
 */
private const val SKIP_ICON_SCALE = 0.78f
