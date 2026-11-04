package com.wanbaohe.translator.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.CameraAlt
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.t8rin.imagetoolbox.core.resources.icons.Close
import com.t8rin.imagetoolbox.core.resources.icons.ContentCopy
import com.t8rin.imagetoolbox.core.resources.icons.Language
import com.t8rin.imagetoolbox.core.resources.icons.Mic
import com.t8rin.imagetoolbox.core.resources.icons.Star
import com.t8rin.imagetoolbox.core.resources.icons.VolumeUp
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSwapHoriz
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.core.ui.widget.enhanced.EnhancedButton
import com.wanbaohe.core.ui.widget.enhanced.EnhancedDropdownMenu
import com.wanbaohe.core.ui.widget.glass.GlassCard
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.translator.R
import com.wanbaohe.translator.component.TranslatorComponent
import com.wanbaohe.translator.component.TranslatorUiState
import com.wanbaohe.translator.domain.TranslationLanguage
import com.wanbaohe.translator.domain.TranslationModelMode

@Composable
internal fun TranslationPane(
    state: TranslatorUiState,
    onInputChange: (String) -> Unit,
    onSourceLanguage: (TranslationLanguage) -> Unit,
    onTargetLanguage: (TranslationLanguage) -> Unit,
    onSwap: () -> Unit,
    onTranslate: () -> Unit,
    onVoiceInput: () -> Unit,
    onCameraInput: () -> Unit,
    onClear: () -> Unit,
    onSpeak: () -> Unit,
    onFavorite: () -> Unit,
    onOpenLocalModels: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val currentRecord = state.history.firstOrNull { it.id == state.currentRecordId }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LanguageSelector(
                selected = state.sourceLanguage,
                includeAuto = true,
                onSelect = onSourceLanguage,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onSwap, enabled = !state.isTranslating) {
                Icon(
                    Icons.Outlined.LineSwapHoriz,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            LanguageSelector(
                selected = state.targetLanguage,
                includeAuto = false,
                onSelect = onTargetLanguage,
                modifier = Modifier.weight(1f),
            )
        }

        GlassCard(
            style = GlassStyle.Thick,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            BasicTextField(
                value = state.inputText,
                onValueChange = onInputChange,
                enabled = !state.isTranslating && !state.isRecognizingImage,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.onPrimaryContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                decorationBox = { innerTextField ->
                    Box {
                        if (state.inputText.isEmpty()) {
                            Text(
                                text = stringResource(R.string.translation_input_hint),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        innerTextField()
                    }
                },
            )
            Text(
                text = "${state.inputText.length}/${TranslatorComponent.MAX_INPUT_LENGTH}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(horizontal = 16.dp),
            )
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                TranslationAction(
                    icon = Icons.Outlined.Mic,
                    label = stringResource(R.string.translation_voice_input),
                    onClick = onVoiceInput,
                )
                TranslationAction(
                    icon = Icons.Outlined.CameraAlt,
                    label = stringResource(R.string.translation_camera_input),
                    onClick = onCameraInput,
                )
                TranslationAction(
                    icon = Icons.Rounded.Close,
                    label = stringResource(R.string.translation_clear),
                    onClick = onClear,
                )
            }
            if (state.isRecognizingImage) {
                val progress = state.ocrDownloadProgress
                if (progress == null) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                } else {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        EnhancedButton(
            onClick = onTranslate,
            enabled = !state.isTranslating && !state.isRecognizingImage,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            glassStyle = GlassStyle.None,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (state.isTranslating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.Language,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
            }
            Spacer(Modifier.size(8.dp))
            Text(
                stringResource(
                    if (state.isTranslating) R.string.translation_in_progress else R.string.translation_action
                )
            )
        }

        val showLocalModelAction = state.errorRes == R.string.translation_local_model_missing &&
            state.modelMode == TranslationModelMode.LOCAL && !state.isLocalModelInstalled
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 32.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(state.errorRes ?: state.modelMode.labelRes()),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.65f),
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = if (showLocalModelAction) Modifier.weight(1f) else Modifier,
            )
            if (showLocalModelAction) {
                TextButton(
                    onClick = onOpenLocalModels,
                    contentPadding = PaddingValues(horizontal = 8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.translation_download_local_model),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }

        GlassCard(
            style = GlassStyle.Regular,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            ) {
                when {
                    state.isTranslating && state.outputText.isBlank() -> CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    state.outputText.isNotBlank() -> Text(
                        text = state.outputText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    else -> Text(
                        text = stringResource(R.string.translation_output_hint),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                TranslationAction(
                    icon = Icons.Outlined.ContentCopy,
                    label = stringResource(R.string.translation_copy),
                    enabled = state.outputText.isNotBlank(),
                    onClick = {
                        copyText(context, state.outputText)
                        AppToastHost.showToast(com.t8rin.imagetoolbox.core.resources.R.string.copied)
                    },
                )
                TranslationAction(
                    icon = Icons.Outlined.VolumeUp,
                    label = stringResource(R.string.translation_read_aloud),
                    enabled = state.outputText.isNotBlank() && !state.isSpeaking,
                    onClick = onSpeak,
                )
                TranslationAction(
                    icon = if (currentRecord?.isFavorite == true) Icons.Rounded.Star else Icons.Outlined.Star,
                    label = stringResource(
                        if (currentRecord?.isFavorite == true) R.string.translation_unfavorite
                        else R.string.translation_favorite
                    ),
                    enabled = currentRecord != null,
                    onClick = onFavorite,
                )
            }
        }

    }
}

@Composable
private fun LanguageSelector(
    selected: TranslationLanguage,
    includeAuto: Boolean,
    onSelect: (TranslationLanguage) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        GlassCard(
            onClick = { expanded = true },
            style = GlassStyle.Thin,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(selected.labelRes()),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = 8.dp, vertical = 12.dp),
            )
        }
        EnhancedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.matchParentSize(),
            maxWidth = 240.dp,
            maxHeight = 420.dp,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
        ) {
            TranslationLanguage.entries
                .filter { includeAuto || it != TranslationLanguage.AUTO }
                .forEach { language ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(language.labelRes()),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (language == selected) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                        trailingIcon = {
                            if (language == selected) {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                            } else {
                                Spacer(Modifier.size(24.dp))
                            }
                        },
                        onClick = {
                            onSelect(language)
                            expanded = false
                        },
                    )
                }
        }
    }
}

@Composable
private fun TranslationAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    Column(
        modifier = Modifier
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (enabled) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
    }
}

private fun copyText(context: Context, text: String) {
    context.getSystemService<ClipboardManager>()?.setPrimaryClip(
        ClipData.newPlainText("translation", text)
    )
}
