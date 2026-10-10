package com.wanbaohe.xiangqi.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDownloadForOffline
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMemory
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePsychology
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedAlertDialog
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalButton
import com.wanbaohe.xiangqi.R
import com.wanbaohe.xiangqi.application.port.outbound.XiangqiAiSource
import com.wanbaohe.xiangqi.data.local.XiangqiEngineWeights

/**
 * 象棋专用走棋 AI 选择面板。
 *
 * 只列决策/引擎类来源 +「快速工作模型」，与聊天模型选择器分离；
 * 新增开源象棋引擎时扩展 [XiangqiAiSource.RemoteEngine.presets] 即可出现在此列表。
 *
 * 「本地引擎」是一等公民：未下载时点选会弹下载确认（不偷跑 10.7MB），
 * 下载中展示进度，完成后自动选中。
 */
@Composable
fun XiangqiAiPickerBottomSheet(
    visible: Boolean,
    selected: XiangqiAiSource,
    workingModelTitle: String,
    title: String,
    localEnginePackaged: Boolean,
    localEngineState: XiangqiEngineWeights.InstallState,
    onSelected: (XiangqiAiSource) -> Unit,
    onDismiss: () -> Unit,
    onDownloadLocalEngine: () -> Unit,
    onCancelLocalEngineDownload: () -> Unit,
    onPickWorkingModel: (() -> Unit)? = null,
) {
    var confirmingLocalDownload by remember { mutableStateOf(false) }
    var pendingAutoSelectLocal by remember { mutableStateOf(false) }

    // 用户点「下载并启用」后，权重落盘即自动选中本地引擎并关掉面板
    androidx.compose.runtime.LaunchedEffect(localEngineState) {
        if (pendingAutoSelectLocal && localEngineState is XiangqiEngineWeights.InstallState.Installed) {
            pendingAutoSelectLocal = false
            onSelected(XiangqiAiSource.LocalEngine)
            onDismiss()
        }
    }

    EnhancedModalBottomSheet(
        visible = visible,
        onDismiss = { onDismiss() },
        dragHandle = {},
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = AppTheme.dimens.spaceLarge, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.xiangqi_ai_picker_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(12.dp))

            XiangqiAiSource.presets.forEach { source ->
                SourceRow(
                    source = source,
                    selected = source == selected,
                    workingModelTitle = workingModelTitle,
                    localEnginePackaged = localEnginePackaged,
                    localEngineState = localEngineState,
                    onCancelLocalEngineDownload = onCancelLocalEngineDownload,
                    onClick = {
                        when {
                            source != XiangqiAiSource.LocalEngine -> {
                                onSelected(source)
                                onDismiss()
                            }
                            !localEnginePackaged -> Unit
                            localEngineState is XiangqiEngineWeights.InstallState.Installed -> {
                                onSelected(source)
                                onDismiss()
                            }
                            localEngineState is XiangqiEngineWeights.InstallState.Downloading -> Unit
                            else -> confirmingLocalDownload = true
                        }
                    },
                )
                if (source == XiangqiAiSource.WorkingModel && onPickWorkingModel != null) {
                    TextButton(onClick = onPickWorkingModel) {
                        Text(stringResource(R.string.xiangqi_settings_pick_ai_model))
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (confirmingLocalDownload) {
        EnhancedAlertDialog(
            visible = true,
            onDismissRequest = { confirmingLocalDownload = false },
            title = { Text(stringResource(R.string.xiangqi_local_engine_download_confirm_title)) },
            text = {
                Text(stringResource(R.string.xiangqi_local_engine_download_confirm_message))
            },
            confirmButton = {
                GlassTonalButton(
                    onClick = {
                        confirmingLocalDownload = false
                        pendingAutoSelectLocal = true
                        onDownloadLocalEngine()
                    },
                ) {
                    Text(stringResource(R.string.xiangqi_local_engine_download_and_enable))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmingLocalDownload = false }) {
                    Text(stringResource(R.string.xiangqi_close))
                }
            },
        )
    }
}

@Composable
private fun SourceRow(
    source: XiangqiAiSource,
    selected: Boolean,
    workingModelTitle: String,
    localEnginePackaged: Boolean,
    localEngineState: XiangqiEngineWeights.InstallState,
    onCancelLocalEngineDownload: () -> Unit,
    onClick: () -> Unit,
) {
    val title = when (source) {
        XiangqiAiSource.WorkingModel -> stringResource(R.string.xiangqi_ai_source_working_model)
        XiangqiAiSource.Jev -> stringResource(R.string.xiangqi_ai_source_jev)
        XiangqiAiSource.LocalEngine -> stringResource(R.string.xiangqi_ai_source_local_engine)
        is XiangqiAiSource.RemoteEngine -> when (source.engineId) {
            XiangqiAiSource.RemoteEngine.PIKAFISH -> stringResource(R.string.xiangqi_ai_source_pikafish)
            else -> source.engineId
        }
    }
    val subtitle = when (source) {
        XiangqiAiSource.WorkingModel -> workingModelTitle
        XiangqiAiSource.Jev -> stringResource(R.string.xiangqi_ai_source_jev_desc)
        XiangqiAiSource.LocalEngine -> when {
            !localEnginePackaged -> stringResource(R.string.xiangqi_local_engine_unavailable)
            localEngineState is XiangqiEngineWeights.InstallState.Installed ->
                stringResource(R.string.xiangqi_ai_source_local_engine_ready)
            localEngineState is XiangqiEngineWeights.InstallState.Downloading ->
                stringResource(R.string.xiangqi_ai_source_local_engine_downloading)
            else -> stringResource(R.string.xiangqi_ai_source_local_engine_need_download)
        }
        is XiangqiAiSource.RemoteEngine -> stringResource(R.string.xiangqi_ai_source_engine_desc)
    }

    val enabled = source != XiangqiAiSource.LocalEngine ||
        (localEnginePackaged && localEngineState is XiangqiEngineWeights.InstallState.Installed) ||
        (localEnginePackaged && localEngineState is XiangqiEngineWeights.InstallState.NotInstalled)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, enabled = enabled, onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RadioButton(selected = selected, onClick = onClick, enabled = enabled)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                },
            )
            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (source == XiangqiAiSource.LocalEngine &&
                localEngineState is XiangqiEngineWeights.InstallState.Downloading
            ) {
                val fraction =
                    if (localEngineState.total > 0) {
                        (localEngineState.downloaded.toFloat() / localEngineState.total).coerceIn(0f, 1f)
                    } else {
                        0f
                    }
                LinearProgressIndicator(
                    progress = { fraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(
                            R.string.xiangqi_local_engine_downloading,
                            (fraction * 100).toInt(),
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    TextButton(onClick = onCancelLocalEngineDownload) {
                        Text(stringResource(R.string.xiangqi_cancel), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            SourceTags(source = source)
        }
        when {
            source == XiangqiAiSource.LocalEngine -> Icon(
                imageVector = Icons.Outlined.LineDownloadForOffline,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            source is XiangqiAiSource.RemoteEngine -> Icon(
                imageVector = Icons.Outlined.LineMemory,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            source == XiangqiAiSource.Jev -> Icon(
                imageVector = Icons.Outlined.LinePsychology,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
    }
}

@Composable
private fun SourceTags(source: XiangqiAiSource) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 4.dp),
    ) {
        if (source.requiresLogin) {
            SourceTag(stringResource(R.string.xiangqi_ai_tag_login), Color(0xFFF08A5D))
        }
        if (source.requiresPoints) {
            SourceTag(stringResource(R.string.xiangqi_ai_tag_points), Color(0xFF4F46E5))
        }
        if (!source.requiresLogin && !source.requiresPoints) {
            SourceTag(stringResource(R.string.xiangqi_ai_tag_free), Color(0xFF3D8B7A))
        }
    }
}

@Composable
private fun SourceTag(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = color.copy(alpha = 0.14f),
        shadowElevation = 0.dp,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp,
            ),
            color = color.copy(alpha = 0.92f),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
        )
    }
}
