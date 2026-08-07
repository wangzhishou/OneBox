package com.shifenmiao.feature.document.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.outlined.ContentCopy
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
import androidx.core.net.toUri
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.base.utils.FileUtils
import com.shifenmiao.common.handle.LocalUrlNavigator
import com.shifenmiao.common.ui.AddImagePickingDialogWithPicker
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.common.ui.rememberAddImagePickerState
import com.shifenmiao.core.constants.Constants.FILE_POINTS_CONSUME_NUM
import com.shifenmiao.database.ocr.entity.PaddleOcrTaskEntity
import com.shifenmiao.feature.document.component.OcrTaskListComponent
import com.shifenmiao.feature.document.component.OcrTaskListUiEvent
import com.shifenmiao.feature.document.domain.nextPollInSeconds
import com.shifenmiao.feature.document.ui.UploadValidationResult
import com.shifenmiao.feature.document.ui.bytesToDisplay
import com.shifenmiao.feature.document.ui.validateOcrUpload
import com.shifenmiao.feature.document.util.DocumentConstants
import com.shifenmiao.feature.ocr.document.R
import com.shifenmiao.model.ai.event.MainClickEvent
import com.shifenmiao.model.ai.event.MainClickEventFrom
import com.shifenmiao.model.ai.event.MainShowType
import com.shifenmiao.model.ocr.OcrTaskStatus
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.domain.model.MimeType
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.Picker
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.rememberFilePicker
import com.t8rin.imagetoolbox.core.ui.utils.helper.isPortraitOrientationAsState
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
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
import com.t8rin.imagetoolbox.core.resources.icons.ContentCopy
import com.t8rin.imagetoolbox.core.resources.icons.Delete
import com.t8rin.imagetoolbox.core.resources.icons.Refresh
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFeatures
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDescription
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDownload
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSave
import com.t8rin.imagetoolbox.core.resources.icons.line.LineWarning
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDateRange
import com.t8rin.imagetoolbox.core.resources.icons.line.LineVisibility

@Composable
fun OcrTaskListScreen(
    component: OcrTaskListComponent, onBack: () -> Unit
) {
    val tasks by component.tasks.collectAsState()
    val isSubmitting by component.isSubmitting.collectAsState()
    val downloadingTaskIds by component.downloadingTaskIds.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()


    var selectedTaskForSave by remember { mutableStateOf<PaddleOcrTaskEntity?>(null) }
    var taskToDelete by remember { mutableStateOf<PaddleOcrTaskEntity?>(null) }
    var nowMillis by remember { mutableStateOf(System.currentTimeMillis()) }

    /**
     * 任务开始前确认是否登录和积分足够
     */
    val ensureLoginAndPoints: (() -> Unit) -> Unit = { block ->
        ActionUtils.ensureLoginAndCheckPoints(
            source = "ocr_document",
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
                is OcrTaskListUiEvent.Toast -> AppToastHost.showToast(event.message)
            }
        }
    }

    val saveLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/markdown")) { uri ->
            uri?.let { destinationUri ->
                selectedTaskForSave?.localPath?.let { path ->
                    val sourceFile = File(path)
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
                    validateOcrUpload(context, uri, isPdf = true)
                }
                when (validation) {
                    is UploadValidationResult.Ok -> {
                        component.submitTask(
                            context = context,
                            fileUri = uri,
                            isPdf = true,
                            fileSizeBytes = validation.fileSizeBytes
                        )
                    }

                    is UploadValidationResult.TooLarge -> AppToastHost.showToast(
                        appContext.getString(
                            R.string.ocr_upload_file_too_large,
                            bytesToDisplay(validation.maxBytes),
                            bytesToDisplay(validation.actualBytes)
                        )
                    )

                    is UploadValidationResult.InvalidImageDimensions -> Unit
                    UploadValidationResult.UnableToReadFileSize -> AppToastHost.showToast(
                        appContext.getString(R.string.ocr_upload_unable_read_file_size)
                    )

                    UploadValidationResult.UnableToReadImageDimensions -> AppToastHost.showToast(
                        appContext.getString(R.string.ocr_upload_unable_read_image_dimensions)
                    )
                }
            }
        })

    val addImageState = rememberAddImagePickerState(
        picker = Picker.Single, onImagesPicked = { uris ->
            uris.firstOrNull()?.let { uri ->
                scope.launch {
                    val validation = withContext(Dispatchers.IO) {
                        validateOcrUpload(context, uri, isPdf = false)
                    }
                    when (validation) {
                        is UploadValidationResult.Ok -> {
                            component.submitTask(
                                context = context,
                                fileUri = uri,
                                isPdf = false,
                                fileSizeBytes = validation.fileSizeBytes
                            )
                        }

                        is UploadValidationResult.TooLarge -> AppToastHost.showToast(
                            appContext.getString(
                                R.string.ocr_upload_file_too_large,
                                bytesToDisplay(validation.maxBytes),
                                bytesToDisplay(validation.actualBytes)
                            )
                        )

                        is UploadValidationResult.InvalidImageDimensions -> AppToastHost.showToast(
                            appContext.getString(
                                R.string.ocr_upload_image_invalid_dimensions,
                                validation.width,
                                validation.height,
                                validation.minShortSidePx,
                                validation.maxLongSidePx
                            )
                        )

                        UploadValidationResult.UnableToReadFileSize -> AppToastHost.showToast(
                            appContext.getString(R.string.ocr_upload_unable_read_file_size)
                        )

                        UploadValidationResult.UnableToReadImageDimensions -> AppToastHost.showToast(
                            appContext.getString(R.string.ocr_upload_unable_read_image_dimensions)
                        )
                    }
                }
            }
        })

    // 添加图片对话框（复用 common 模块的组件）
    AddImagePickingDialogWithPicker(
        visible = addImageState.showDialog,
        onDismiss = addImageState.onDismissDialog,
        picker = Picker.Single,
        onImagesPicked = { uris ->
            uris.firstOrNull()?.let { uri ->
                scope.launch {
                    val validation = withContext(Dispatchers.IO) {
                        validateOcrUpload(context, uri, isPdf = false)
                    }
                    when (validation) {
                        is UploadValidationResult.Ok -> {
                            component.submitTask(
                                context = context,
                                fileUri = uri,
                                isPdf = false,
                                fileSizeBytes = validation.fileSizeBytes
                            )
                        }

                        is UploadValidationResult.TooLarge -> AppToastHost.showToast(
                            appContext.getString(
                                R.string.ocr_upload_file_too_large,
                                bytesToDisplay(validation.maxBytes),
                                bytesToDisplay(validation.actualBytes)
                            )
                        )

                        is UploadValidationResult.InvalidImageDimensions -> AppToastHost.showToast(
                            appContext.getString(
                                R.string.ocr_upload_image_invalid_dimensions,
                                validation.width,
                                validation.height,
                                validation.minShortSidePx,
                                validation.maxLongSidePx
                            )
                        )

                        UploadValidationResult.UnableToReadFileSize -> AppToastHost.showToast(
                            appContext.getString(R.string.ocr_upload_unable_read_file_size)
                        )

                        UploadValidationResult.UnableToReadImageDimensions -> AppToastHost.showToast(
                            appContext.getString(R.string.ocr_upload_unable_read_image_dimensions)
                        )
                    }
                }
            }
        })

    LoadingDialog(
        visible = isSubmitting,
        isForSaving = false,
        onCancelLoading = {
            component.cancelSubmitting(context)
        }
    ) {
        GlassSurface(
            modifier = Modifier
                .padding(
                    vertical = AppTheme.dimens.spaceNormal,
                    horizontal = AppTheme.dimens.paddingNormal
                )
                .wrapContentHeight(),
            style = GlassStyle.Regular,
            shape = MaterialTheme.shapes.medium
        ) {
            Text(
                stringResource(R.string.submit_loading),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    var isGridMode by remember { mutableStateOf(false) }

    BaseScreen(
        title = stringResource(R.string.ocr_document_task_list),
        onGoBack = onBack,
        actions = {
            IconButton(onClick = { isGridMode = !isGridMode }) {
                Icon(
                    imageVector = if (isGridMode) Icons.AutoMirrored.Filled.ViewList else com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFeatures,
                    contentDescription = stringResource(R.string.switch_layout)
                )
            }
        }) {
        val localUrlNavigator = LocalUrlNavigator.current
        OcrTaskListContent(
            tasks = tasks,
            isGridMode = isGridMode,
            nowMillis = nowMillis,
            downloadingTaskIds = downloadingTaskIds,
            onAddTask = {
                ensureLoginAndPoints { addImageState.pickImage() }
            },
            onAddImageOptions = {
                ensureLoginAndPoints { addImageState.onShowDialog() }
            },
            onAddPdf = {
                ensureLoginAndPoints { selectionPdfPicker.pickFile() }
            },
            onOpen = { task ->
                task.localPath?.let { path ->
                    localUrlNavigator.navigate(
                        Screen.MarkdownEditor(
                            initialUri = FileUtils.normalizeEditorUri(File(path).toUri())
                        )
                    )
                } ?: component.downloadMarkdown(context, task)
            },
            onSaveAs = { task ->
                selectedTaskForSave = task
                saveLauncher.launch("${task.fileName}.md")
            },
            onDeleteRequest = { task ->
                taskToDelete = task
            },
            onHttpDownload = { task -> component.onHttpDownload(context, task) },
            onCopy = { task -> component.copyRemoteMarkdownUrl(context, task) },
            onRetry = { task -> component.downloadMarkdown(context, task) })
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
private fun OcrTaskListContent(
    tasks: List<PaddleOcrTaskEntity>,
    isGridMode: Boolean,
    nowMillis: Long,
    downloadingTaskIds: Set<String>,
    onAddTask: () -> Unit,
    onAddImageOptions: () -> Unit,
    onAddPdf: () -> Unit,
    onOpen: (PaddleOcrTaskEntity) -> Unit,
    onSaveAs: (PaddleOcrTaskEntity) -> Unit,
    onDeleteRequest: (PaddleOcrTaskEntity) -> Unit,
    onHttpDownload: (PaddleOcrTaskEntity) -> Unit,
    onCopy: (PaddleOcrTaskEntity) -> Unit,
    onRetry: (PaddleOcrTaskEntity) -> Unit
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
        contentPadding = PaddingValues(horizontal = AppTheme.dimens.spaceLarge),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceLarge),
        verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceLarge)
    ) {
        item {
            AddTaskCard(
                onImageClick = onAddTask,
                onImageLongClick = onAddImageOptions,
                onPdfClick = onAddPdf,
                isGridMode = isGridMode,
                tipsText = stringResource(R.string.picker_tips),
                descriptionText = stringResource(R.string.empty_state_description2)
            ) {

                if (tasks.isEmpty()) {
                    Spacer(modifier = Modifier.height(AppTheme.dimens.spaceLarge))
                    EmptyStateView()
                }
            }
        }
        items(tasks) { task ->
            OcrTaskItem(
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
                onSaveAs = { onSaveAs(task) },
                onDelete = { onDeleteRequest(task) },
                onHttpDownload = { onHttpDownload(task) },
                onCopy = { onCopy(task) },
                onRetry = { onRetry(task) },
                onPreviewMarkdown = { onOpen(task) },
            )

        }
    }
}

@Composable
private fun OcrTaskItem(
    isGridMode: Boolean = false,
    task: PaddleOcrTaskEntity,
    nowMillis: Long,
    isDownloading: Boolean,
    onOpen: () -> Unit,
    onSaveAs: () -> Unit,
    onDelete: () -> Unit,
    onHttpDownload: () -> Unit,
    onCopy: () -> Unit,
    onRetry: () -> Unit,
    onPreviewMarkdown: () -> Unit,
    modifier: Modifier
) {
    val containerColor = when (task.status) {
        OcrTaskStatus.SUCCESS.value -> {
            MaterialTheme.colorScheme.primaryContainer
        }

        OcrTaskStatus.FAILED.value -> {
            MaterialTheme.colorScheme.errorContainer
        }

        else -> {
            MaterialTheme.colorScheme.surfaceContainer
        }
    }
    val contentColor = when (task.status) {
        OcrTaskStatus.SUCCESS.value -> {
            MaterialTheme.colorScheme.onPrimaryContainer
        }

        OcrTaskStatus.FAILED.value -> {
            MaterialTheme.colorScheme.onErrorContainer
        }

        else -> {
            MaterialTheme.colorScheme.onSurfaceVariant
        }
    }
    val shape = MaterialTheme.shapes.medium
    GlassCard(
        onClick = onOpen,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        shape = shape,
        containerAlpha = GlassStyle.Regular.backgroundAlpha
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(AppTheme.dimens.spaceLarge),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            OcrTaskHeader(task, isGridMode)
            Spacer(modifier = Modifier.weight(1f))
            Column(
                modifier = Modifier.padding(vertical = AppTheme.dimens.spaceSmall),
                verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSmall)
            ) {
                Text(
                    text = task.fileName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.task_id_prefix, task.taskId),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = AppTheme.dimens.spaceSmall),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            Spacer(modifier = Modifier.height(AppTheme.dimens.spaceSmall))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val pollInSeconds = if (task.status == OcrTaskStatus.PENDING.value) {
                    nextPollInSeconds(task.updatedAt, nowMillis)
                } else {
                    null
                }
                OcrTaskInfo(task = task, pollInSeconds = pollInSeconds)
                val showCopyRemoteMarkdownUrl = task.status == OcrTaskStatus.SUCCESS.value &&
                        !task.markdownUrl.isNullOrBlank() &&
                        nowMillis - task.createdAt <= DocumentConstants.REMOTE_MARKDOWN_URL_TTL_MS
                OcrTaskActions(
                    isGridMode = isGridMode,
                    status = task.status,
                    isDownloaded = !task.localPath.isNullOrBlank(),
                    isDownloading = isDownloading,
                    showCopyRemoteMarkdownUrl = showCopyRemoteMarkdownUrl,
                    onHttpDownload = onHttpDownload,
                    onCopy = onCopy,
                    onSaveAs = onSaveAs,
                    onRetry = onRetry,
                    onDownload = onOpen,
                    onDelete = onDelete,
                    onPreviewMarkdown = onPreviewMarkdown
                )
            }
        }
    }
}

@Composable
private fun OcrTaskHeader(
    task: PaddleOcrTaskEntity,
    isGridMode: Boolean
) {
    val size = if (isGridMode) {
        20.dp
    } else {
        30.dp
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDescription,
                contentDescription = null,
                modifier = Modifier.size(size)
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
}

@Composable
private fun OcrTaskInfo(task: PaddleOcrTaskEntity, pollInSeconds: Int?) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDateRange,
                contentDescription = null,
                modifier = Modifier.size(AppTheme.dimens.iconSmallSize),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(AppTheme.dimens.spaceExtraSmall))
            Text(
                text = formatDate(task.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (pollInSeconds != null) {
            Spacer(modifier = Modifier.height(AppTheme.dimens.spaceSmall))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineHistory,
                    contentDescription = null,
                    modifier = Modifier.size(AppTheme.dimens.iconSmallSize),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(AppTheme.dimens.spaceExtraSmall))
                Text(
                    text = stringResource(R.string.next_poll_in_seconds, pollInSeconds),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (!task.errorMsg.isNullOrEmpty() && task.status != OcrTaskStatus.SUCCESS.value) {
            Spacer(modifier = Modifier.height(AppTheme.dimens.spaceSmall))
            GlassSurface(
                modifier = Modifier.padding(vertical = AppTheme.dimens.spaceSmall),
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
                        modifier = Modifier.size(AppTheme.dimens.iconSmallSize)
                    )
                    Spacer(modifier = Modifier.width(AppTheme.dimens.spaceSmall))
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
}

@Composable
private fun OcrTaskActions(
    isGridMode: Boolean = false,
    status: String,
    isDownloaded: Boolean,
    isDownloading: Boolean,
    showCopyRemoteMarkdownUrl: Boolean,
    onHttpDownload: () -> Unit,
    onCopy: () -> Unit,
    onSaveAs: () -> Unit,
    onRetry: () -> Unit,
    onDownload: () -> Unit,
    onDelete: () -> Unit,
    onPreviewMarkdown: () -> Unit
) {
    val size = if (isGridMode) {
        18.dp
    } else {
        36.dp
    }
    Row(
        modifier = Modifier
            .padding(top = AppTheme.dimens.spaceLarge)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSmall, Alignment.End),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (status == OcrTaskStatus.SUCCESS.value) {
            if (isDownloaded) {
                IconButton(
                    modifier = Modifier.size(size),
                    onClick = onHttpDownload
                ) {
                    Icon(
                        com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDownload,
                        contentDescription = stringResource(com.t8rin.imagetoolbox.core.resources.R.string.download)
                    )
                }
                IconButton(
                    modifier = Modifier.size(size),
                    onClick = onSaveAs
                ) {
                    Icon(
                        com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSave,
                        contentDescription = stringResource(R.string.save_as)
                    )
                }

                IconButton(
                    modifier = Modifier.size(size),
                    onClick = onPreviewMarkdown
                ) {
                    Icon(
                        com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineVisibility,
                        contentDescription = stringResource(R.string.preview_markdown)
                    )
                }
            } else {
                AssistChip(
                    border = null,
                    onClick = onDownload,
                    enabled = !isDownloading,
                    shape = MaterialTheme.shapes.medium,
                    colors = AppTheme.colors.assistChipColors(),
                    label = {
                        Text(
                            if (isDownloading) {
                                stringResource(R.string.ocr_downloading)
                            } else {
                                stringResource(R.string.ocr_download_to_local)
                            }
                        )
                    }
                )
                if (showCopyRemoteMarkdownUrl) {
                    IconButton(
                        modifier = Modifier.size(size),
                        onClick = onCopy
                    ) {
                        Icon(
                            com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ContentCopy,
                            contentDescription = stringResource(R.string.copy)
                        )
                    }
                }
            }
        } else if (status == OcrTaskStatus.FAILED.value) {
            IconButton(
                modifier = Modifier.size(size),
                onClick = onRetry
            ) {
                Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Refresh, contentDescription = stringResource(R.string.retry))
            }
        }

        IconButton(
            modifier = Modifier.size(size),
            onClick = onDelete
        ) {
            Icon(
                com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Delete,
                contentDescription = stringResource(R.string.delete)
            )
        }
    }
}

 
