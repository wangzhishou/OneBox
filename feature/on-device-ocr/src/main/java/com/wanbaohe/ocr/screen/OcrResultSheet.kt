package com.wanbaohe.ocr.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.AddPhotoAlt
import com.t8rin.imagetoolbox.core.resources.icons.line.LineNote
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRestartAlt
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSaveAs
import com.wanbaohe.base.ui.button.PrimaryButton
import com.wanbaohe.base.ui.button.SecondaryButton
import com.wanbaohe.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.wanbaohe.ocr.R
import com.wanbaohe.ocr.component.OnDeviceOcrUiState

@Composable
internal fun OcrResultSheet(
    visible: Boolean,
    state: OnDeviceOcrUiState,
    text: String,
    onDismiss: (Boolean) -> Unit,
    onSaveMarkdown: () -> Unit,
    onSaveNote: () -> Unit,
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
                    SecondaryButton(
                        text = stringResource(R.string.ocr_save_markdown),
                        enable = text.isNotBlank() && !state.isSavingMarkdown && !state.isRecognizing,
                        style = MaterialTheme.typography.labelMedium,
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.LineSaveAs,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(16.dp),
                            )
                        },
                        onClick = onSaveMarkdown,
                        modifier = Modifier.weight(1f),
                    )
                    SecondaryButton(
                        text = stringResource(R.string.ocr_save_note),
                        enable = text.isNotBlank() && !state.isSavingNote && !state.isRecognizing,
                        style = MaterialTheme.typography.labelMedium,
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.LineNote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(16.dp),
                            )
                        },
                        onClick = onSaveNote,
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SecondaryButton(
                        text = stringResource(R.string.ocr_recognize_again),
                        enable = !state.isRecognizing,
                        style = MaterialTheme.typography.labelMedium,
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.LineRestartAlt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(16.dp),
                            )
                        },
                        onClick = onReRecognize,
                        modifier = Modifier.weight(1f),
                    )
                    PrimaryButton(
                        text = stringResource(R.string.ocr_new_image),
                        enable = !state.isRecognizing,
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.AddPhotoAlt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(16.dp),
                            )
                        },
                        onClick = onNewImage,
                        modifier = Modifier.weight(1f),
                    )
                }
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
