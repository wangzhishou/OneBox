package com.wanbaohe.ocr.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import com.shifenmiao.common.ui.BaseScreen
import com.t8rin.imagetoolbox.core.resources.icons.ContentCopy
import com.t8rin.imagetoolbox.core.resources.icons.ImageToText
import com.t8rin.imagetoolbox.core.resources.icons.Share
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCameraAlt
import com.t8rin.imagetoolbox.core.resources.icons.line.LineImagePreview
import com.wanbaohe.core.ui.utils.content_pickers.ImagePickerMode
import com.wanbaohe.core.ui.utils.content_pickers.rememberImagePicker
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.core.ui.widget.glass.GlassCard
import com.wanbaohe.core.ui.widget.glass.GlassLinearProgressIndicator
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.ocr.R
import com.wanbaohe.ocr.component.OnDeviceOcrComponent
import com.wanbaohe.ocr.component.OnDeviceOcrUiState
import com.t8rin.imagetoolbox.core.resources.Icons
import com.shifenmiao.core.R as CoreR

@Composable
fun OnDeviceOcrScreen(component: OnDeviceOcrComponent) {
    val state by component.uiState.collectAsState()
    val context = LocalContext.current
    val gallery = rememberImagePicker(onSuccess = component::onImagePicked)
    val camera = rememberImagePicker(mode = ImagePickerMode.CameraCapture) { uris ->
        uris.firstOrNull()?.let(component::onImagePicked)
    }
    val text = state.lines.joinToString(separator = "\n") { it.text }
    BaseScreen(
        title = stringResource(CoreR.string.on_device_ocr),
        onGoBack = component.onGoBack,
        actions = {
            if (text.isNotEmpty()) {
                IconButton(onClick = { copyText(context, text) }) {
                    Icon(
                        Icons.Rounded.ContentCopy,
                        contentDescription = stringResource(R.string.ocr_copy),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                IconButton(onClick = { shareText(context, text) }) {
                    Icon(
                        Icons.Outlined.Share,
                        contentDescription = stringResource(R.string.ocr_share),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
        ) {
            when {
                !state.modelReady -> DownloadPane(
                    state = state,
                    onDownload = component::downloadModel,
                    onOpenCloud = component.onOpenCloudOcr,
                    modifier = Modifier.weight(1f),
                )
                state.preview != null -> ResultPane(
                    state = state,
                    text = text,
                    onSelect = { index ->
                        component.select(index)
                        copyText(context, state.lines[index].text)
                    },
                    onCamera = camera::pickImage,
                    onGallery = gallery::pickImage,
                    modifier = Modifier.weight(1f),
                )
                else -> PickPane(
                    onCamera = camera::pickImage,
                    onGallery = gallery::pickImage,
                    onOpenCloud = component.onOpenCloudOcr,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun DownloadPane(
    state: OnDeviceOcrUiState,
    onDownload: () -> Unit,
    onOpenCloud: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        GlassCard(style = GlassStyle.Thick, modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = Icons.Outlined.ImageToText,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(48.dp),
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.ocr_intro_title),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.ocr_intro_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                state.errorRes?.let { error ->
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = stringResource(error),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                    )
                }
                Spacer(Modifier.height(20.dp))
                if (state.isDownloading) {
                    val progress = state.downloadProgress
                    if (progress == null) {
                        GlassLinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    } else {
                        GlassLinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = progress?.let { value ->
                            stringResource(
                                R.string.ocr_download_percent,
                                (value * 100).toInt().coerceIn(0, 100),
                            )
                        } ?: stringResource(R.string.ocr_downloading),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                } else {
                    Button(
                        onClick = onDownload,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ),
                    ) {
                        Text(stringResource(R.string.ocr_download))
                    }
                }
            }
        }
        TextButton(onClick = onOpenCloud) {
            Text(stringResource(R.string.ocr_open_cloud))
        }
    }
}

@Composable
private fun PickPane(
    onCamera: () -> Unit,
    onGallery: () -> Unit,
    onOpenCloud: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PickCard(
                icon = Icons.Outlined.LineCameraAlt,
                label = stringResource(R.string.ocr_pick_camera),
                onClick = onCamera,
                modifier = Modifier.weight(1f),
            )
            PickCard(
                icon = Icons.Outlined.LineImagePreview,
                label = stringResource(R.string.ocr_pick_gallery),
                onClick = onGallery,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.ocr_pick_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        TextButton(onClick = onOpenCloud, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text(stringResource(R.string.ocr_open_cloud))
        }
    }
}

@Composable
private fun PickCard(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    GlassCard(onClick = onClick, style = GlassStyle.Thick, modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 28.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(36.dp),
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun ResultPane(
    state: OnDeviceOcrUiState,
    text: String,
    onSelect: (Int) -> Unit,
    onCamera: () -> Unit,
    onGallery: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val preview = state.preview ?: return
    Column(modifier = modifier.fillMaxWidth()) {
        BoxRecognizing(
            recognizing = state.isRecognizing,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(top = 8.dp),
        ) {
            RecognizedImage(
                bitmap = preview,
                lines = state.lines,
                selectedIndex = state.selectedIndex,
                onSelect = onSelect,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Spacer(Modifier.height(12.dp))
        GlassCard(style = GlassStyle.Medium, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = when {
                        state.isRecognizing -> stringResource(R.string.ocr_recognizing)
                        state.lines.isEmpty() -> stringResource(R.string.ocr_empty)
                        else -> stringResource(R.string.ocr_result_count, state.lines.size)
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onCamera, enabled = !state.isRecognizing) {
                        Text(stringResource(R.string.ocr_retake))
                    }
                    TextButton(onClick = onGallery, enabled = !state.isRecognizing) {
                        Text(stringResource(R.string.ocr_replace))
                    }
                }
                state.errorRes?.let { error ->
                    Text(
                        text = stringResource(error),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                if (text.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.ocr_tap_hint),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    Column(
                        modifier = Modifier
                            .heightIn(max = 220.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        state.lines.forEachIndexed { index, line ->
                            val selected = index == state.selectedIndex
                            Text(
                                text = line.text,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (selected) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (selected) {
                                            MaterialTheme.colorScheme.primaryContainer
                                        } else {
                                            Color.Transparent
                                        },
                                    )
                                    .clickable { onSelect(index) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun BoxRecognizing(
    recognizing: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier) {
        content()
        if (recognizing) {
            GlassCard(
                style = GlassStyle.Thick,
                modifier = Modifier.align(Alignment.Center),
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(18.dp)
                        .size(28.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    strokeWidth = 3.dp,
                )
            }
        }
    }
}

private fun copyText(context: Context, text: String) {
    val clipboard = context.getSystemService<ClipboardManager>() ?: return
    clipboard.setPrimaryClip(ClipData.newPlainText("ocr", text))
    AppToastHost.showToast(R.string.ocr_copied)
}

private fun shareText(context: Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, null))
}
