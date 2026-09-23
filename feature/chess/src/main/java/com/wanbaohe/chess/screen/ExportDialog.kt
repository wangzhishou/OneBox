package com.wanbaohe.chess.screen

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.ui.utils.helper.Clipboard
import com.t8rin.imagetoolbox.core.ui.utils.helper.ContextUtils.shareText
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalButton
import com.wanbaohe.chess.R

/**
 * 棋谱导出面板（Game 页与回放页共用）。
 *
 * 三个格式按钮只负责**生成内容**，内容统一显示在下方预览区；
 * 生成后才能【复制】/【分享】——棋谱的核心使用场景是"发给别人 / 贴到群里"，
 * 只有裸文本让用户长按手选（JSON 几百行时基本不可用）。
 */
@Composable
fun ExportDialog(
    exportContent: String,
    onExportFen: () -> Unit,
    onExportJson: () -> Unit,
    onExportText: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onShareImage: (() -> Unit)? = null,
    isSharingImage: Boolean = false,
) {
    val context = LocalContext.current
    val hasContent = exportContent.isNotBlank()

    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.chess_export_dialog_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GlassTonalButton(onClick = onExportFen, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.chess_export_fen))
                    }
                    GlassTonalButton(onClick = onExportJson, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.chess_export_json))
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GlassTonalButton(onClick = onExportText, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.chess_export_text))
                    }
                    if (onShareImage != null) {
                        GlassTonalButton(
                            onClick = onShareImage,
                            enabled = !isSharingImage,
                            modifier = Modifier.weight(1f),
                        ) {
                            if (isSharingImage) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                            } else {
                                Text(stringResource(R.string.chess_share_image))
                            }
                        }
                    }
                }

                Text(
                    text = exportContent.ifBlank { stringResource(R.string.chess_empty_export) },
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 220.dp)
                        .verticalScroll(rememberScrollState())
                        .horizontalScroll(rememberScrollState()),
                )

                if (hasContent) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GlassTonalButton(
                            onClick = {
                                Clipboard.copy(
                                    text = exportContent,
                                    message = R.string.chess_copied,
                                )
                            },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(stringResource(R.string.chess_copy_notation))
                        }
                        GlassTonalButton(
                            onClick = { context.shareText(exportContent) },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(stringResource(R.string.chess_share_notation))
                        }
                    }
                }
            }
        },
        confirmButton = {
            GlassTonalButton(onClick = onDismiss) {
                Text(stringResource(R.string.chess_close))
            }
        },
    )
}
