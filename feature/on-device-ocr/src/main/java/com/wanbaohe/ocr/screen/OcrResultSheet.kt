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

package com.wanbaohe.ocr.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.AddPhotoAlt
import com.t8rin.imagetoolbox.core.resources.icons.CopyAll
import com.t8rin.imagetoolbox.core.resources.icons.Share
import com.t8rin.imagetoolbox.core.resources.icons.Note
import com.t8rin.imagetoolbox.core.resources.icons.RestartAlt
import com.t8rin.imagetoolbox.core.resources.icons.SaveAs
import com.t8rin.imagetoolbox.core.resources.icons.TextCard
import com.wanbaohe.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.wanbaohe.core.ui.widget.system.OneBoxPillButton
import com.wanbaohe.ocr.R
import com.wanbaohe.ocr.component.OnDeviceOcrUiState

@Composable
private fun OcrActionButton(
    text: String,
    icon: ImageVector,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primary: Boolean = false,
) {
    val textStyle = MaterialTheme.typography.labelLarge
    val contentColor = if (primary) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSecondaryContainer
    }
    OneBoxPillButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(15.dp),
            )
        },
        containerColor = if (primary) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.secondaryContainer
        },
        contentColor = contentColor,
        textStyle = textStyle,
        textAutoSize = TextAutoSize.StepBased(
            minFontSize = 9.sp,
            maxFontSize = textStyle.fontSize,
            stepSize = 0.5.sp,
        ),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
    )
}

@Composable
internal fun OcrResultSheet(
    visible: Boolean,
    state: OnDeviceOcrUiState,
    text: String,
    onDismiss: (Boolean) -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onSaveMarkdown: () -> Unit,
    onSaveNote: () -> Unit,
    onCreateCard: () -> Unit,
    onReRecognize: () -> Unit,
    onNewImage: () -> Unit,
) {
    EnhancedModalBottomSheet(
        visible = visible,
        onDismiss = onDismiss,
        title = stringResource(
            when {
                state.isRecognizing -> R.string.ocr_recognizing
                state.lines.isEmpty() -> R.string.ocr_empty
                else -> R.string.ocr_text_result
            }
        ),
        nestedScrollEnabled = true,
        endContent = {
            if (state.lines.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.ocr_result_count, state.lines.size),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        endConfirmButtonPadding = 0.dp,
        confirmButton = {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OcrActionButton(
                        text = stringResource(R.string.ocr_copy),
                        icon = Icons.Outlined.CopyAll,
                        enabled = text.isNotBlank() && !state.isRecognizing,
                        onClick = onCopy,
                        modifier = Modifier.weight(1f),
                    )
                    OcrActionButton(
                        text = stringResource(R.string.ocr_share),
                        icon = Icons.Outlined.Share,
                        enabled = text.isNotBlank() && !state.isRecognizing,
                        onClick = onShare,
                        modifier = Modifier.weight(1f),
                    )
                    OcrActionButton(
                        text = stringResource(R.string.ocr_save_markdown),
                        icon = Icons.Outlined.SaveAs,
                        enabled = text.isNotBlank() && !state.isSavingMarkdown && !state.isRecognizing,
                        onClick = onSaveMarkdown,
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OcrActionButton(
                        text = stringResource(R.string.ocr_save_note),
                        icon = Icons.Outlined.Note,
                        enabled = text.isNotBlank() && !state.isSavingNote && !state.isRecognizing,
                        onClick = onSaveNote,
                        modifier = Modifier.weight(1f),
                    )
                    OcrActionButton(
                        text = stringResource(R.string.ocr_recognize_again),
                        icon = Icons.Outlined.RestartAlt,
                        enabled = !state.isRecognizing,
                        onClick = onReRecognize,
                        modifier = Modifier.weight(1f),
                    )
                    OcrActionButton(
                        text = stringResource(R.string.ocr_new_image),
                        icon = Icons.Outlined.AddPhotoAlt,
                        enabled = !state.isRecognizing,
                        onClick = onNewImage,
                        modifier = Modifier.weight(1f),
                        primary = true,
                    )
                }
                Spacer(Modifier.height(8.dp))
                OcrActionButton(
                    text = stringResource(R.string.ocr_make_card),
                    icon = Icons.Outlined.TextCard,
                    enabled = text.isNotBlank() && !state.isRecognizing,
                    onClick = onCreateCard,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 480.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
        ) {
            state.errorRes?.let { error ->
                Text(
                    text = stringResource(error),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(8.dp))
            }
            if (text.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.ocr_tap_hint),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                SelectionContainer {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
