package com.shifenmiao.feature.document.screen

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.common.ui.AddImagePickingDialogWithPicker
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.common.ui.rememberAddImagePickerState
import com.shifenmiao.core.constants.Constants.FILE_POINTS_CONSUME_NUM
import com.shifenmiao.database.docconvert.entity.DocConvertTaskEntity
import com.shifenmiao.feature.document.component.DocConvertTaskListComponent
import com.shifenmiao.feature.document.component.DocConvertTaskListUiEvent
import com.shifenmiao.feature.document.domain.nextPollInSeconds
import com.shifenmiao.feature.document.ui.UploadValidationResult
import com.shifenmiao.feature.document.ui.bytesToDisplay
import com.shifenmiao.feature.document.ui.validateDocConvertUpload
import com.shifenmiao.model.ai.event.MainClickEvent
import com.shifenmiao.model.ai.event.MainClickEventFrom
import com.shifenmiao.model.ai.event.MainShowType
import com.shifenmiao.model.ocr.OcrTaskStatus
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.domain.model.MimeType
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.Picker
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.rememberFilePicker
import com.t8rin.imagetoolbox.core.ui.utils.helper.isPortraitOrientationAsState
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.widget.dialogs.LoadingDialog
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSurface
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.utils.appContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.shifenmiao.model.event.AppEventBus
import java.io.File
import com.t8rin.imagetoolbox.core.resources.icons.line.LineHistory
import com.t8rin.imagetoolbox.core.resources.icons.Delete
import com.t8rin.imagetoolbox.core.resources.icons.Refresh
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFeatures
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDownload
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLink
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSave
import com.t8rin.imagetoolbox.core.resources.icons.line.LineWarning
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDateRange
import com.t8rin.imagetoolbox.core.resources.icons.line.LineVisibility

@Composable
fun DocConvertTaskListScreen(
    component: DocConvertTaskListComponent,
    onBack: () -> Unit
) {
    val tasks by component.tasks.collectAsState()
    val isSubmitting by component.isSubmitting.collectAsState()
    val downloadingTaskIds by component.downloadingTaskIds.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()


    var selectedForSave by remember { mutableStateOf<Pair<DocConvertTaskEntity, Boolean>?>(null) }
    var taskToDelete by remember { mutableStateOf<DocConvertTaskEntity?>(null) }
    var nowMillis by remember { mutableStateOf(System.currentTimeMillis()) }

    val ensureLoginAndPoints: (() -> Unit) -> Unit = { block ->
        ActionUtils.ensureLoginAndCheckPoints(
            source = "doc_convert",
            point = FILE_POINTS_CONSUME_NUM,
            onPointsFailure = {
                AppEventBus.emit(
                    MainClickEvent(
                        from = MainClickEventFrom.OCR_TASK_LIST,
                        type = MainShowType.BUY_COFFEE
                    )
                )
            },
            onSuccess = block
        )
    }

    LaunchedEffect(component) {
        component.startPendingPolling(context)
    }
    DisposableEffect(component) {
        onDispose {
            component.stopPendingPolling()
        }
    }
    LaunchedEffect(Unit) {
        while (true) {
            nowMillis = System.currentTimeMillis()
            delay(1000)
        }
    }
    LaunchedEffect(component) {
        component.uiEvents.collect { event ->
            when (event) {
                is DocConvertTaskListUiEvent.Toast -> AppToastHost.showToast(event.message)
            }
        }
    }

    val saveLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("*/*")) { uri ->
            uri?.let { destinationUri ->
                val (task, preferWord) = selectedForSave ?: return@rememberLauncherForActivityResult
                val path = if (preferWord) task.localWordPath else task.localExcelPath
                path?.let {
                    val sourceFile = File(it)
                    if (sourceFile.exists()) {
                        context.contentResolver.openOutputStream(destinationUri)?.use { output ->
                            sourceFile.inputStream().use { input ->
                                input.copyTo(output)
                            }
                        }
                    }
                }
            }
        }

    val selectionPdfPicker = rememberFilePicker(
        mimeType = MimeType.Pdf,
        onSuccess = { uri: Uri ->
            scope.launch {
                val validation = withContext(Dispatchers.IO) {
                    validateDocConvertUpload(context, uri, isPdf = true)
                }
                when (validation) {
                    is UploadValidationResult.Ok -> component.submitTask(
                        context = context,
                        fileUri = uri,
                        isPdf = true,
                        fileSizeBytes = validation.fileSizeBytes
                    )

                    is UploadValidationResult.TooLarge -> AppToastHost.showToast(
                        appContext.getString(
                            com.shifenmiao.feature.ocr.document.R.string.ocr_upload_file_too_large,
                            bytesToDisplay(validation.maxBytes),
                            bytesToDisplay(validation.actualBytes)
                        )
                    )

                    is UploadValidationResult.InvalidImageDimensions -> Unit
                    UploadValidationResult.UnableToReadFileSize -> AppToastHost.showToast(
                        appContext.getString(com.shifenmiao.feature.ocr.document.R.string.ocr_upload_unable_read_file_size)
                    )

                    UploadValidationResult.UnableToReadImageDimensions -> AppToastHost.showToast(
                        appContext.getString(com.shifenmiao.feature.ocr.document.R.string.ocr_upload_unable_read_image_dimensions)
                    )
                }
            }
        }
    )

    val onImagesPicked: (List<Uri>) -> Unit = { uris ->
        uris.firstOrNull()?.let { uri ->
            scope.launch {
                val validation = withContext(Dispatchers.IO) {
                    validateDocConvertUpload(context, uri, isPdf = false)
                }
                when (validation) {
                    is UploadValidationResult.Ok -> component.submitTask(
                        context = context,
                        fileUri = uri,
                        isPdf = false,
                        fileSizeBytes = validation.fileSizeBytes
                    )

                    is UploadValidationResult.TooLarge -> AppToastHost.showToast(
                        appContext.getString(
                            com.shifenmiao.feature.ocr.document.R.string.ocr_upload_file_too_large,
                            bytesToDisplay(validation.maxBytes),
                            bytesToDisplay(validation.actualBytes)
                        )
                    )

                    is UploadValidationResult.InvalidImageDimensions -> AppToastHost.showToast(
                        appContext.getString(
                            com.shifenmiao.feature.ocr.document.R.string.ocr_upload_image_invalid_dimensions,
                            validation.width,
                            validation.height,
                            validation.minShortSidePx,
                            validation.maxLongSidePx
                        )
                    )

                    UploadValidationResult.UnableToReadFileSize -> AppToastHost.showToast(
                        appContext.getString(com.shifenmiao.feature.ocr.document.R.string.ocr_upload_unable_read_file_size)
                    )

                    UploadValidationResult.UnableToReadImageDimensions -> AppToastHost.showToast(
                        appContext.getString(com.shifenmiao.feature.ocr.document.R.string.ocr_upload_unable_read_image_dimensions)
                    )
                }
            }
        }
    }

    val addImageState = rememberAddImagePickerState(
        picker = Picker.Single,
        onImagesPicked = onImagesPicked
    )

    AddImagePickingDialogWithPicker(
        visible = addImageState.showDialog,
        onDismiss = addImageState.onDismissDialog,
        picker = Picker.Single,
        onImagesPicked = onImagesPicked
    )

    LoadingDialog(
        visible = isSubmitting,
        isForSaving = false,
        onCancelLoading = { component.cancelSubmitting(context) }
    ) {
        GlassSurface(
            modifier = Modifier
                .padding(
                    vertical = AppTheme.dimens.spaceNormal,
                    horizontal = AppTheme.dimens.paddingNormal
                ),
            style = GlassStyle.Regular,
            shape = MaterialTheme.shapes.medium
        ) {
            Text(
                stringResource(com.shifenmiao.feature.ocr.document.R.string.submit_loading),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    var isGridMode by remember { mutableStateOf(false) }

    BaseScreen(
        title = stringResource(com.shifenmiao.feature.ocr.document.R.string.doc_convert_task_list),
        onGoBack = onBack,
        actions = {
            IconButton(onClick = { isGridMode = !isGridMode }) {
                Icon(
                    imageVector = if (isGridMode) Icons.AutoMirrored.Filled.ViewList else com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFeatures,
                    contentDescription = stringResource(com.shifenmiao.feature.ocr.document.R.string.switch_layout)
                )
            }
        }
    ) {
        DocConvertTaskListContent(
            tasks = tasks,
            isGridMode = isGridMode,
            nowMillis = nowMillis,
            downloadingTaskIds = downloadingTaskIds,
            onAddTask = { ensureLoginAndPoints { addImageState.pickImage() } },
            onAddImageOptions = { ensureLoginAndPoints { addImageState.onShowDialog() } },
            onAddPdf = { ensureLoginAndPoints { selectionPdfPicker.pickFile() } },
            onOpen = { task ->
                val localPath = task.localWordPath ?: task.localExcelPath
                if (!localPath.isNullOrBlank()) {
                    openLocalFile(context, localPath)
                } else {
                    component.downloadDefaultResult(context, task)
                }
            },
            onSaveAs = { task, preferWord ->
                selectedForSave = task to preferWord
                val ext = if (preferWord) "docx" else "xlsx"
                saveLauncher.launch("${task.fileName}.$ext")
            },
            onDeleteRequest = { taskToDelete = it },
            onHttpDownload = { task, preferWord -> component.onHttpDownload(context, task, preferWord) },
            onCopy = { task, preferWord -> component.copyRemoteUrl(context, task, preferWord) },
            onRetry = { task -> component.downloadDefaultResult(context, task) },
            onDownloadWord = { task -> component.downloadWord(context, task) },
            onDownloadExcel = { task -> component.downloadExcel(context, task) }
        )
    }

    DeleteConfirmationDialog(
        visible = taskToDelete != null,
        fileName = taskToDelete?.fileName.orEmpty(),
        onDismiss = { taskToDelete = null },
        onConfirm = {
            taskToDelete?.let { component.deleteTask(it) }
            taskToDelete = null
        }
    )
}

@Composable
private fun DocConvertTaskListContent(
    tasks: List<DocConvertTaskEntity>,
    isGridMode: Boolean,
    nowMillis: Long,
    downloadingTaskIds: Set<String>,
    onAddTask: () -> Unit,
    onAddImageOptions: () -> Unit,
    onAddPdf: () -> Unit,
    onOpen: (DocConvertTaskEntity) -> Unit,
    onSaveAs: (DocConvertTaskEntity, Boolean) -> Unit,
    onDeleteRequest: (DocConvertTaskEntity) -> Unit,
    onHttpDownload: (DocConvertTaskEntity, Boolean) -> Unit,
    onCopy: (DocConvertTaskEntity, Boolean) -> Unit,
    onRetry: (DocConvertTaskEntity) -> Unit,
    onDownloadWord: (DocConvertTaskEntity) -> Unit,
    onDownloadExcel: (DocConvertTaskEntity) -> Unit
) {
    val isPortrait by isPortraitOrientationAsState()
    val columns = if (isGridMode && isPortrait) {
        GridCells.Fixed(2)
    } else {
        GridCells.Adaptive(280.dp)
    }
    LazyVerticalGrid(
        columns = columns,
        modifier = Modifier
            .fillMaxSize()
            .animateContentSize(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AddTaskCard(
                onImageClick = onAddTask,
                onImageLongClick = onAddImageOptions,
                onPdfClick = onAddPdf,
                isGridMode = isGridMode,
                tipsText = stringResource(com.shifenmiao.feature.ocr.document.R.string.picker_tips),
                descriptionText = stringResource(com.shifenmiao.feature.ocr.document.R.string.doc_convert_empty_description)
            ) {
                if (tasks.isEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    EmptyStateView()
                }
            }
        }
        items(tasks) { task ->
            DocConvertTaskItem(
                modifier = Modifier
                    .heightIn(min = 220.dp, max = 280.dp)
                    .animateItem(
                        fadeInSpec = spring(dampingRatio = 0.8f, stiffness = 380f),
                        placementSpec = spring(dampingRatio = 0.8f, stiffness = 380f),
                        fadeOutSpec = spring(dampingRatio = 0.8f, stiffness = 380f)
                    ),
                isGridMode = isGridMode,
                task = task,
                nowMillis = nowMillis,
                isDownloading = downloadingTaskIds.contains(task.taskId),
                onOpen = { onOpen(task) },
                onSaveAs = { preferWord -> onSaveAs(task, preferWord) },
                onDelete = { onDeleteRequest(task) },
                onHttpDownload = { preferWord -> onHttpDownload(task, preferWord) },
                onCopy = { preferWord -> onCopy(task, preferWord) },
                onRetry = { onRetry(task) },
                onDownloadWord = { onDownloadWord(task) },
                onDownloadExcel = { onDownloadExcel(task) }
            )
        }
    }
}

@Composable
private fun DocConvertTaskItem(
    isGridMode: Boolean,
    task: DocConvertTaskEntity,
    nowMillis: Long,
    isDownloading: Boolean,
    onOpen: () -> Unit,
    onSaveAs: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onHttpDownload: (Boolean) -> Unit,
    onCopy: (Boolean) -> Unit,
    onRetry: () -> Unit,
    onDownloadWord: () -> Unit,
    onDownloadExcel: () -> Unit,
    modifier: Modifier
) {
    val containerColor = when (task.status) {
        OcrTaskStatus.SUCCESS.value -> MaterialTheme.colorScheme.primaryContainer
        OcrTaskStatus.FAILED.value -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surfaceContainer
    }
    val contentColor = when (task.status) {
        OcrTaskStatus.SUCCESS.value -> MaterialTheme.colorScheme.onPrimaryContainer
        OcrTaskStatus.FAILED.value -> MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    GlassCard(
        onClick = onOpen,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        shape = MaterialTheme.shapes.medium,
        containerAlpha = GlassStyle.Regular.backgroundAlpha
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDownload,
                        contentDescription = null,
                        modifier = Modifier.size(if (isGridMode) 20.dp else 30.dp)
                    )
                    if (task.fileSizeBytes > 0) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = bytesToDisplay(task.fileSizeBytes),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                StatusChip(task.status, isGridMode)
            }
            Spacer(modifier = Modifier.weight(1f))
            Column(
                modifier = Modifier.padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = task.fileName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(com.shifenmiao.feature.ocr.document.R.string.task_id_prefix, task.taskId),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (task.status == OcrTaskStatus.SUCCESS.value) {
                    val types = buildList {
                        if (!task.wordUrl.isNullOrBlank()) add(stringResource(com.shifenmiao.feature.ocr.document.R.string.doc_convert_word))
                        if (!task.excelUrl.isNullOrBlank()) add(stringResource(com.shifenmiao.feature.ocr.document.R.string.doc_convert_excel))
                        if (isEmpty()) {
                            if (!task.localWordPath.isNullOrBlank()) add(stringResource(com.shifenmiao.feature.ocr.document.R.string.doc_convert_word))
                            if (!task.localExcelPath.isNullOrBlank()) add(stringResource(com.shifenmiao.feature.ocr.document.R.string.doc_convert_excel))
                        }
                    }.joinToString(" / ")
                    if (types.isNotBlank()) {
                        Text(
                            text = stringResource(com.shifenmiao.feature.ocr.document.R.string.doc_convert_file_type, types),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            val pollInSeconds = if (task.status == OcrTaskStatus.PENDING.value || task.status == OcrTaskStatus.PROCESSING.value) {
                nextPollInSeconds(task.updatedAt, nowMillis)
            } else null

            androidx.compose.foundation.layout.FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDateRange,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formatDate(task.createdAt),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (pollInSeconds != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineHistory,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(
                                    com.shifenmiao.feature.ocr.document.R.string.next_poll_in_seconds,
                                    pollInSeconds
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (!task.errorMsg.isNullOrEmpty() && task.status != OcrTaskStatus.SUCCESS.value) {
                        Spacer(modifier = Modifier.height(8.dp))
                        GlassSurface(
                            modifier = Modifier.padding(vertical = 8.dp),
                            style = GlassStyle.Thin,
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineWarning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = task.errorMsg!!,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val hasWord = !task.wordUrl.isNullOrBlank()
                    val hasExcel = !task.excelUrl.isNullOrBlank()
                    val wordDownloaded = !task.localWordPath.isNullOrBlank()
                    val excelDownloaded = !task.localExcelPath.isNullOrBlank()
                    val anyDownloaded = wordDownloaded || excelDownloaded
                    val primaryIsWord = when {
                        wordDownloaded -> true
                        excelDownloaded -> false
                        hasWord -> true
                        else -> false
                    }

                    if (task.status == OcrTaskStatus.SUCCESS.value) {
                        if (anyDownloaded) {
                            IconButton(
                                modifier = Modifier.size(if (isGridMode) 18.dp else 36.dp),
                                onClick = { onHttpDownload(primaryIsWord) }
                            ) {
                                Icon(
                                    com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDownload,
                                    contentDescription = stringResource(com.t8rin.imagetoolbox.core.resources.R.string.download)
                                )
                            }
                            IconButton(
                                modifier = Modifier.size(if (isGridMode) 18.dp else 36.dp),
                                onClick = { onSaveAs(primaryIsWord) }
                            ) {
                                Icon(
                                    com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSave,
                                    contentDescription = stringResource(com.shifenmiao.feature.ocr.document.R.string.save_as)
                                )
                            }
                            IconButton(
                                modifier = Modifier.size(if (isGridMode) 18.dp else 36.dp),
                                onClick = onOpen
                            ) {
                                Icon(
                                    com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineVisibility,
                                    contentDescription = stringResource(com.shifenmiao.feature.ocr.document.R.string.preview_markdown)
                                )
                            }

                            if (hasWord && hasExcel) {
                                val otherIsWord = !primaryIsWord
                                val otherDownloaded = if (otherIsWord) wordDownloaded else excelDownloaded
                                if (!otherDownloaded) {
                                    AssistChip(
                                        onClick = if (otherIsWord) onDownloadWord else onDownloadExcel,
                                        enabled = !isDownloading,
                                        label = {
                                            Text(
                                                if (otherIsWord) {
                                                    stringResource(com.shifenmiao.feature.ocr.document.R.string.doc_convert_word)
                                                } else {
                                                    stringResource(com.shifenmiao.feature.ocr.document.R.string.doc_convert_excel)
                                                }
                                            )
                                        }
                                    )
                                }
                            }
                        } else {
                            AssistChip(
                                border = null,
                                onClick = onOpen,
                                shape = MaterialTheme.shapes.medium,
                                enabled = !isDownloading,
                                colors = AppTheme.colors.assistChipColors(),
                                label = {
                                    Text(
                                        if (isDownloading) {
                                            stringResource(com.shifenmiao.feature.ocr.document.R.string.ocr_downloading)
                                        } else {
                                            stringResource(com.shifenmiao.feature.ocr.document.R.string.ocr_download_to_local)
                                        }
                                    )
                                }
                            )
                            if (hasWord && hasExcel) {
                                AssistChip(
                                    onClick = if (primaryIsWord) onDownloadExcel else onDownloadWord,
                                    enabled = !isDownloading,
                                    label = {
                                        Text(
                                            if (primaryIsWord) {
                                                stringResource(com.shifenmiao.feature.ocr.document.R.string.doc_convert_excel)
                                            } else {
                                                stringResource(com.shifenmiao.feature.ocr.document.R.string.doc_convert_word)
                                            }
                                        )
                                    }
                                )
                            }
                            IconButton(
                                modifier = Modifier.size(if (isGridMode) 18.dp else 36.dp),
                                onClick = { onCopy(primaryIsWord) }
                            ) {
                                Icon(
                                    com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineLink,
                                    contentDescription = stringResource(com.shifenmiao.feature.ocr.document.R.string.copy)
                                )
                            }
                        }
                    } else if (task.status == OcrTaskStatus.FAILED.value) {
                        IconButton(
                            modifier = Modifier.size(if (isGridMode) 18.dp else 36.dp),
                            onClick = onRetry
                        ) {
                            Icon(
                                com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Refresh,
                                contentDescription = stringResource(com.shifenmiao.feature.ocr.document.R.string.retry)
                            )
                        }
                    }

                    IconButton(
                        modifier = Modifier.size(if (isGridMode) 18.dp else 36.dp),
                        onClick = onDelete
                    ) {
                        Icon(
                            com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Delete,
                            contentDescription = stringResource(com.shifenmiao.feature.ocr.document.R.string.delete)
                        )
                    }
                }
            }
        }
    }
}

private fun openLocalFile(context: android.content.Context, filePath: String) {
    val file = File(filePath)
    if (!file.exists()) return
    val uri = FileProvider.getUriForFile(
        context,
        context.getString(com.t8rin.imagetoolbox.core.resources.R.string.file_provider),
        file
    )
    val type = when {
        filePath.endsWith(".docx", ignoreCase = true) -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        filePath.endsWith(".xlsx", ignoreCase = true) -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        else -> "*/*"
    }
    val pm = context.packageManager

    fun buildIntent(mimeType: String): Intent {
        return Intent(Intent.ACTION_VIEW)
            .setDataAndType(uri, mimeType)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    val typedIntent = buildIntent(type)
    val fallbackIntent = buildIntent("*/*")

    val targetIntent = when {
        typedIntent.resolveActivity(pm) != null -> typedIntent
        fallbackIntent.resolveActivity(pm) != null -> fallbackIntent
        else -> null
    }

    if (targetIntent == null) {
        ActionUtils.showError(context.getString(com.shifenmiao.feature.ocr.document.R.string.doc_convert_no_viewer))
        return
    }

    try {
        context.startActivity(Intent.createChooser(targetIntent, null))
    } catch (_: ActivityNotFoundException) {
        ActionUtils.showError(context.getString(com.shifenmiao.feature.ocr.document.R.string.doc_convert_no_viewer))
    }
}
