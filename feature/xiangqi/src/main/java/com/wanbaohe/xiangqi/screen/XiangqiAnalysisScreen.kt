package com.wanbaohe.xiangqi.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.wanbaohe.xiangqi.component.XiangqiAnalysisComponent
import com.wanbaohe.xiangqi.data.TextExportLabels
import com.wanbaohe.xiangqi.ui.board.XiangqiBoard
import com.t8rin.imagetoolbox.core.resources.icons.PlayCircle
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDownload
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMemory
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

    val content: @Composable (Modifier) -> Unit = { contentModifier ->
        XiangqiAnalysisContent(
            component = component,
            modifier = contentModifier,
            onExport = { exportDialog = true },
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
        AlertDialog(
            onDismissRequest = {
                exportDialog = false
                component.dismissExport()
            },
            title = { Text(stringResource(R.string.xiangqi_export_dialog_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GlassTonalButton(onClick = component::exportFen, modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.xiangqi_export_fen))
                        }
                        GlassTonalButton(onClick = component::exportJson, modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.xiangqi_export_json))
                        }
                    }
                    GlassTonalButton(onClick = { component.exportText(exportLabels) }, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.xiangqi_export_text))
                    }
                    Text(state.exportContent.ifBlank { stringResource(R.string.xiangqi_empty_export) })
                }
            },
            confirmButton = {
                GlassTonalButton(onClick = {
                    exportDialog = false
                    component.dismissExport()
                }) {
                    Text(stringResource(R.string.xiangqi_close))
                }
            },
        )
    }
}

@Composable
private fun XiangqiAnalysisContent(
    component: XiangqiAnalysisComponent,
    modifier: Modifier,
    onExport: () -> Unit,
) {
    val state = component.uiState

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        XiangqiBoard(
            boardState = state.boardState,
            selectedPoint = null,
            candidateTargets = emptySet(),
            onCellTap = { _, _ -> },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        )

        ReplayControls(
            isAutoPlaying = state.isAutoPlaying,
            onStart = component::goToStart,
            onPrev = component::goPrev,
            onToggleAutoPlay = component::toggleAutoPlay,
            onNext = component::goNext,
            onEnd = component::goToEnd,
        )

        GlassSurface(
            modifier = Modifier.fillMaxWidth(),
            style = GlassStyle.Medium,
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                        .padding(16.dp),
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
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDownload,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.clickable { onExport() },
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                ) {
                    if (state.plies.isEmpty()) {
                        Text(
                            text = stringResource(R.string.xiangqi_no_history),
                            modifier = Modifier.padding(8.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        state.plies.chunked(2).forEachIndexed { index, pair ->
                            val redMove = pair.getOrNull(0)
                            val blackMove = pair.getOrNull(1)
                            val isRedActive = redMove?.ply == state.currentPly
                            val isBlackActive = blackMove?.ply == state.currentPly

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (isRedActive || isBlackActive) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f) else Color.Transparent)
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "${index + 1}.",
                                    modifier = Modifier.weight(0.15f),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                Text(
                                    text = redMove?.moveCn?.ifBlank { redMove.moveUcci } ?: "...",
                                    modifier = Modifier.weight(0.425f),
                                    color = if (isRedActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (isRedActive) FontWeight.Bold else FontWeight.Normal),
                                )
                                Text(
                                    text = blackMove?.moveCn?.ifBlank { blackMove.moveUcci } ?: "...",
                                    modifier = Modifier.weight(0.425f),
                                    color = if (isBlackActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (isBlackActive) FontWeight.Bold else FontWeight.Normal),
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    GlassTonalButton(
                        onClick = onExport,
                        modifier = Modifier.weight(1f),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text(stringResource(R.string.xiangqi_export_short))
                        }
                    }
                    GlassTonalButton(
                        onClick = component::openCurrentGame,
                        modifier = Modifier.weight(1f),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMemory, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text(stringResource(R.string.xiangqi_analyze))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}


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
        GlassTonalIconButton(onClick = onStart) {
            Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSkipPrevious, contentDescription = stringResource(R.string.xiangqi_replay_to_start))
        }
        GlassTonalIconButton(onClick = onPrev) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.PlayCircle,
                contentDescription = stringResource(R.string.xiangqi_replay_prev),
                modifier = Modifier.graphicsLayer { scaleX = -1f },
            )
        }
        GlassTonalIconButton(
            onClick = onToggleAutoPlay,
            modifier = Modifier.size(52.dp),
        ) {
            Icon(
                imageVector = if (isAutoPlaying) Icons.Outlined.Pause else com.t8rin.imagetoolbox.core.resources.Icons.Outlined.PlayCircle,
                contentDescription = stringResource(if (isAutoPlaying) R.string.xiangqi_replay_pause else R.string.xiangqi_replay_auto_play),
                modifier = Modifier.size(28.dp),
            )
        }
        GlassTonalIconButton(onClick = onNext) {
            Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.PlayCircle, contentDescription = stringResource(R.string.xiangqi_replay_next))
        }
        GlassTonalIconButton(onClick = onEnd) {
            Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSkipNext, contentDescription = stringResource(R.string.xiangqi_replay_to_end))
        }
    }
}
