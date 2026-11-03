package com.wanbaohe.ocr.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import com.shifenmiao.common.ui.BaseScreen
import com.t8rin.imagetoolbox.core.data.utils.getPath
import com.t8rin.imagetoolbox.core.domain.model.MimeType
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.ImageToText
import com.t8rin.imagetoolbox.core.resources.icons.CameraAlt
import com.t8rin.imagetoolbox.core.resources.icons.DeleteForever
import com.t8rin.imagetoolbox.core.resources.icons.History
import com.t8rin.imagetoolbox.core.resources.icons.ImagePreview
import com.wanbaohe.base.ui.button.CancelButton
import com.wanbaohe.base.ui.button.ConfirmButton
import com.wanbaohe.base.ui.button.PrimaryButton
import com.wanbaohe.core.ui.utils.content_pickers.ImagePickerMode
import com.wanbaohe.core.ui.utils.content_pickers.rememberFileCreator
import com.wanbaohe.core.ui.utils.content_pickers.rememberImagePicker
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.core.ui.utils.helper.SaveSuccessNotifier
import com.wanbaohe.core.ui.widget.enhanced.EnhancedAlertDialog
import com.wanbaohe.core.ui.widget.glass.GlassCard
import com.wanbaohe.core.ui.widget.glass.GlassLinearProgressIndicator
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.core.ui.widget.navigation.BottomNavItem
import com.wanbaohe.core.ui.widget.navigation.BottomNavigationBar
import com.wanbaohe.ocr.R
import com.wanbaohe.ocr.component.OcrTab
import com.wanbaohe.ocr.component.OnDeviceOcrComponent
import com.wanbaohe.ocr.component.OnDeviceOcrUiState
import com.wanbaohe.ocr.domain.markdownFileName
import com.wanbaohe.ocr.domain.toPlainText
import com.shifenmiao.core.R as CoreR

@Composable
fun OnDeviceOcrScreen(component: OnDeviceOcrComponent) {
    val state by component.uiState.collectAsState()
    val context = LocalContext.current
    val gallery = rememberImagePicker(onSuccess = component::onImagePicked)
    val camera = rememberImagePicker(mode = ImagePickerMode.CameraCapture) { uris ->
        uris.firstOrNull()?.let(component::onImagePicked)
    }
    val text = state.lines.toPlainText()
    val markdownFileName = state.lines.markdownFileName()
    var showClearHistory by remember { mutableStateOf(false) }
    var showResultSheet by remember { mutableStateOf(false) }
    val fileSaver = rememberFileCreator(
        mimeType = MimeType.Markdown,
        onSuccess = { uri ->
            component.saveMarkdown(uri) { saved ->
                if (saved) {
                    val fullPath = uri.toString().getPath(context)
                    SaveSuccessNotifier.notifyFileSaved(
                        fileUri = uri.toString(),
                        fileName = fullPath.substringAfterLast('/').ifBlank { markdownFileName },
                        savingPath = fullPath.substringBeforeLast('/', ""),
                    )
                } else {
                    AppToastHost.showToast(R.string.ocr_save_failed)
                }
            }
        },
    )
    LaunchedEffect(state.activeHistoryId, state.isRecognizing) {
        if (!state.isRecognizing &&
            state.activeHistoryId != null &&
            state.preview != null &&
            state.errorRes == null
        ) {
            showResultSheet = true
        }
    }
    BaseScreen(
        title = stringResource(
            if (state.currentTab == OcrTab.HISTORY) R.string.ocr_history_title
            else CoreR.string.on_device_ocr
        ),
        onGoBack = component.onGoBack,
        showNavigationBarsPadding = false,
        supportGlassEffect = true,
        actions = {
            if (state.currentTab == OcrTab.HISTORY) {
                if (state.history.isNotEmpty()) {
                    IconButton(onClick = { showClearHistory = true }) {
                        Icon(
                            Icons.Outlined.DeleteForever,
                            contentDescription = stringResource(R.string.ocr_clear_history),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
            } else if (state.preview != null) {
                IconButton(onClick = { showResultSheet = !showResultSheet }) {
                    Icon(
                        imageVector = if (showResultSheet) {
                            Icons.Outlined.ImagePreview
                        } else {
                            Icons.Outlined.ImageToText
                        },
                        contentDescription = stringResource(
                            if (showResultSheet) R.string.ocr_hide_result_panel
                            else R.string.ocr_show_result_panel
                        ),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        },
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            when (state.currentTab) {
                OcrTab.RECOGNIZE -> Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
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
                            onSelect = component::select,
                            onCopyLine = { copyText(context, it) },
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
                OcrTab.HISTORY -> OcrHistoryPane(
                    records = state.history,
                    onOpen = { record ->
                        component.openHistory(record) {
                            AppToastHost.showToast(R.string.ocr_history_open_failed)
                        }
                    },
                    onDelete = component::deleteHistory,
                    onStartRecognizing = { component.selectTab(OcrTab.RECOGNIZE) },
                    modifier = Modifier.weight(1f),
                )
            }
            OcrBottomBar(
                currentTab = state.currentTab,
                onSelect = component::selectTab,
            )
        }
    }

    OcrResultSheet(
        visible = showResultSheet && state.preview != null && state.currentTab == OcrTab.RECOGNIZE,
        state = state,
        text = text,
        onDismiss = { showResultSheet = false },
        onCopy = { copyText(context, text) },
        onShare = { shareText(context, text) },
        onCreateCard = { component.onOpenTextCard(text) },
        onSaveMarkdown = {
            showResultSheet = false
            fileSaver.make(markdownFileName)
        },
        onSaveNote = {
            component.saveAsNote { saved ->
                AppToastHost.showToast(
                    if (saved) R.string.ocr_note_saved else R.string.ocr_note_save_failed
                )
            }
        },
        onReRecognize = {
            showResultSheet = false
            component.recognizeAgain()
        },
        onNewImage = {
            showResultSheet = false
            gallery.pickImage()
        },
    )

    if (showClearHistory) {
        EnhancedAlertDialog(
            visible = true,
            onDismissRequest = { showClearHistory = false },
            title = { Text(stringResource(R.string.ocr_clear_history)) },
            text = { Text(stringResource(R.string.ocr_clear_history_confirm)) },
            confirmButton = {
                ConfirmButton(
                    text = stringResource(R.string.ocr_clear_history),
                    onClick = {
                        component.clearHistory()
                        showClearHistory = false
                    },
                )
            },
            dismissButton = {
                CancelButton(onClick = { showClearHistory = false })
            },
        )
    }
}

@Composable
private fun OcrBottomBar(
    currentTab: OcrTab,
    onSelect: (OcrTab) -> Unit,
) {
    val items = listOf(
        BottomNavItem(
            id = OcrTab.RECOGNIZE.name,
            label = stringResource(R.string.ocr_recognize_tab),
            icon = Icons.Outlined.ImageToText,
        ),
        BottomNavItem(
            id = OcrTab.HISTORY.name,
            label = stringResource(R.string.ocr_history_tab),
            icon = Icons.Outlined.History,
        ),
    )
    BottomNavigationBar(
        items = items,
        selectedItemId = currentTab.name,
        onItemClick = { item ->
            runCatching { OcrTab.valueOf(item.id) }.getOrNull()?.let(onSelect)
        },
        modifier = Modifier.fillMaxWidth(),
    )
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
                    PrimaryButton(
                        text = stringResource(R.string.ocr_download),
                        onClick = onDownload,
                    )
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
                icon = Icons.Outlined.CameraAlt,
                label = stringResource(R.string.ocr_pick_camera),
                onClick = onCamera,
                modifier = Modifier.weight(1f),
            )
            PickCard(
                icon = Icons.Outlined.ImagePreview,
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
    onSelect: (Int?) -> Unit,
    onCopyLine: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val preview = state.preview ?: return
    val scrollState = rememberScrollState()
    LaunchedEffect(state.isRecognizing) {
        if (state.isRecognizing) scrollState.scrollTo(0)
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState),
    ) {
        BoxRecognizing(
            recognizing = state.isRecognizing,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
        ) {
            RecognizedImage(
                bitmap = preview,
                lines = state.lines,
                selectedIndex = state.selectedIndex,
                onSelect = onSelect,
                onCopyLine = onCopyLine,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun BoxRecognizing(
    recognizing: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier) {
        content()
        if (recognizing) {
            GlassCard(
                style = GlassStyle.Thick,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp),
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
