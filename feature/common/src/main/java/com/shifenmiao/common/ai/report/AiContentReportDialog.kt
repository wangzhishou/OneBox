/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.shifenmiao.common.ai.report

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.shifenmiao.core.R
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFlag
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.core.ui.widget.enhanced.EnhancedAlertDialog
import com.wanbaohe.core.ui.widget.glass.GlassOutlinedTextField
import com.wanbaohe.core.ui.widget.system.SelectorField
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun AiContentReportButton(
    contentPreview: String,
    modifier: Modifier = Modifier,
    onSubmit: suspend (AiContentReportReason, String) -> Result<Unit>,
) {
    var showDialog by remember { mutableStateOf(false) }

    TextButton(
        onClick = { showDialog = true },
        modifier = modifier,
    ) {
        Icon(
            imageVector = Icons.Outlined.LineFlag,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = stringResource(R.string.ai_content_report_action),
            modifier = Modifier.padding(start = 6.dp),
        )
    }

    if (showDialog) {
        AiContentReportDialog(
            contentPreview = contentPreview,
            onDismiss = { showDialog = false },
            onSubmit = onSubmit,
        )
    }
}

@Composable
fun AiContentReportDialog(
    contentPreview: String,
    onDismiss: () -> Unit,
    onSubmit: suspend (AiContentReportReason, String) -> Result<Unit>,
) {
    var selectedReason by remember { mutableStateOf(AiContentReportReason.OTHER) }
    var details by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var submitFailed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val preview = remember(contentPreview) {
        val plain = contentPreview.toReportPreviewPlainText()
        if (plain.length > PREVIEW_CHARS) {
            plain.take(PREVIEW_CHARS) + "…"
        } else {
            plain
        }
    }

    EnhancedAlertDialog(
        visible = true,
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = !isSubmitting,
            dismissOnClickOutside = !isSubmitting,
        ),
        title = { Text(stringResource(R.string.ai_content_report_title)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = stringResource(R.string.ai_content_report_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (preview.isNotBlank()) {
                    Text(
                        text = preview,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 5,
                    )
                }
                SelectorField(
                    label = stringResource(R.string.ai_content_report_reason_label),
                    selected = selectedReason,
                    options = AiContentReportReason.entries,
                    optionLabel = { stringResource(it.labelRes) },
                    onSelect = {
                        selectedReason = it
                        submitFailed = false
                    },
                    enabled = !isSubmitting,
                )
                GlassOutlinedTextField(
                    value = details,
                    onValueChange = {
                        details = it.take(MAX_DETAILS_CHARS)
                        submitFailed = false
                    },
                    label = { Text(stringResource(R.string.ai_content_report_details_hint)) },
                    minLines = 3,
                    maxLines = 5,
                    enabled = !isSubmitting,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (submitFailed) {
                    Text(
                        text = stringResource(R.string.ai_content_report_failed),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    scope.launch {
                        isSubmitting = true
                        submitFailed = false
                        val result = try {
                            onSubmit(selectedReason, details.trim())
                        } catch (cancellation: CancellationException) {
                            throw cancellation
                        } catch (error: Exception) {
                            Result.failure<Unit>(error)
                        }
                        isSubmitting = false
                        if (result.isSuccess) {
                            AppToastHost.showToast(R.string.ai_content_report_success)
                            onDismiss()
                        } else {
                            submitFailed = true
                        }
                    }
                },
                enabled = !isSubmitting,
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp))
                } else {
                    Text(stringResource(R.string.ai_content_report_submit))
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSubmitting,
            ) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

private const val PREVIEW_CHARS = 600
private const val MAX_DETAILS_CHARS = 2_000

/** 弹窗预览只给用户看正文，去掉 Markdown 标记。提交内容仍用原文。 */
private fun String.toReportPreviewPlainText(): String {
    return replace(Regex("```[\\w-]*\\n?"), "")
        .replace("```", "")
        .replace(Regex("`([^`]*)`"), "$1")
        .replace(Regex("!\\[([^\\]]*)]\\([^)]*\\)"), "$1")
        .replace(Regex("\\[([^\\]]*)]\\([^)]*\\)"), "$1")
        .replace(Regex("(?m)^#{1,6}\\s*"), "")
        .replace(Regex("(?m)^\\s*[-*+]\\s+"), "")
        .replace(Regex("(?m)^\\s*\\d+\\.\\s+"), "")
        .replace(Regex("\\*\\*(.+?)\\*\\*"), "$1")
        .replace(Regex("__(.+?)__"), "$1")
        .replace("*", "")
        .replace(Regex("[ \\t]{2,}"), " ")
        .trim()
}
