package com.wanbaohe.markuplayers.presentation.home

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.base.ui.button.CancelButton
import com.shifenmiao.base.ui.button.ConfirmButton
import com.shifenmiao.database.recent_access.entity.RecentAccessEntity
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.ArrowBack
import com.t8rin.imagetoolbox.core.resources.icons.AutoFixHigh
import com.t8rin.imagetoolbox.core.resources.icons.CameraAlt
import com.t8rin.imagetoolbox.core.resources.icons.ContentPaste
import com.t8rin.imagetoolbox.core.resources.icons.Delete
import com.t8rin.imagetoolbox.core.resources.icons.Image
import com.t8rin.imagetoolbox.core.resources.icons.ImageOverlay
import com.t8rin.imagetoolbox.core.resources.icons.NoteAdd
import com.t8rin.imagetoolbox.core.resources.icons.Transparency
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.ImagePickerMode
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.Picker
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.rememberImagePicker
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.utils.helper.rememberClipboardData
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedIconButton
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedAlertDialog
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.image.Picture
import com.t8rin.imagetoolbox.core.ui.widget.modifier.ShapeDefaults
import com.wanbaohe.markuplayers.R
import com.wanbaohe.markuplayers.domain.model.AiImageOp
import com.wanbaohe.markuplayers.presentation.screenLogic.MarkupLayersComponent
import com.wanbaohe.markuplayers.domain.render.ImageMemoryBudget
import java.text.DateFormat
import java.util.Date

/** New images, durable editable projects, and recent image files are separate entry points. */
@Composable
fun MarkupHomeContent(
    component: MarkupLayersComponent
) {
    val setImage: (Uri) -> Unit = { uri ->
        component.setUri(
            uri = uri,
            onFailure = AppToastHost::showFailureToast
        )
    }
    val galleryPicker = rememberImagePicker(onSuccess = setImage)
    val cameraPicker = rememberImagePicker(
        mode = ImagePickerMode.CameraCapture,
        onSuccess = { uris -> uris.firstOrNull()?.let(setImage) }
    )
    val clipboardUris by rememberClipboardData()
    var showBlankCanvasSheet by rememberSaveable { mutableStateOf(false) }
    val canvasBudget = remember { ImageMemoryBudget() }

    BaseScreen(
        title = {
            Column {
                Text(stringResource(R.string.markup_home_title))
                Text(
                    text = stringResource(R.string.markup_home_tagline),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        onGoBack = component.onGoBack,
        navigationIcon = {
            EnhancedIconButton(onClick = component.onGoBack) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.markup_back)
                )
            }
        },
        content = {
            // 导入卡片 + 最近项目作为整体在剩余空间内垂直居中;内容超出高度时再滚动
            // (scroll 容器内嵌一层 minHeight=视口高度的 Column,由 Arrangement.Center 居中)
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                val viewportHeight = maxHeight
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = viewportHeight)
                            .padding(24.dp)
                    ) {
                        ImportCard(
                            onImportClick = galleryPicker::pickImage,
                            entries = {
                                ImportEntry(
                                    icon = Icons.Outlined.Image,
                                    label = stringResource(R.string.markup_entry_gallery),
                                    onClick = galleryPicker::pickImage
                                )
                                ImportEntry(
                                    icon = Icons.Outlined.CameraAlt,
                                    label = stringResource(R.string.markup_entry_camera),
                                    onClick = cameraPicker::pickImage
                                )
                                if (clipboardUris.isNotEmpty()) {
                                    ImportEntry(
                                        icon = Icons.Rounded.ContentPaste,
                                        label = stringResource(R.string.markup_entry_paste),
                                        onClick = { clipboardUris.firstOrNull()?.let(setImage) }
                                    )
                                }
                                ImportEntry(
                                    icon = Icons.Outlined.NoteAdd,
                                    label = stringResource(R.string.markup_entry_blank_canvas),
                                    onClick = { showBlankCanvasSheet = true }
                                )
                            }
                        )
                        Spacer(Modifier.height(16.dp))
                        QuickTasksSection(
                            component = component,
                            setImage = setImage,
                            onMakeCover = { showBlankCanvasSheet = true }
                        )
                        Spacer(Modifier.height(24.dp))
                        SavedProjectsSection(component = component)
                        Spacer(Modifier.height(24.dp))
                        RecentImagesSection(
                            projects = component.recentProjects,
                            onProjectClick = setImage
                        )
                    }
                }
            }
        }
    )

    BlankCanvasSheet(
        visible = showBlankCanvasSheet,
        onDismiss = { showBlankCanvasSheet = false },
        isCreating = component.isImageLoading,
        maxCanvasPixels = canvasBudget.maxWorkingPixels,
        onCreate = { width, height, backgroundColor ->
            component.startWithBlankCanvas(
                width = width,
                height = height,
                backgroundColor = backgroundColor
            )
        }
    )
}

@Composable
private fun ImportCard(
    onImportClick: () -> Unit,
    entries: @Composable () -> Unit,
) {
    val dashColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
    val cornerRadius = 24.dp
    // 玻璃卡片打底;虚线描边由内层 drawBehind 叠加(绘制在玻璃背景之后,不被盖住),
    // 保留设计稿「虚线导入框」语义
    GlassCard(
        onClick = onImportClick,
        shape = RoundedCornerShape(cornerRadius),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    val strokeWidth = 1.5.dp.toPx()
                    val radius = cornerRadius.toPx()
                    drawRoundRect(
                        color = dashColor,
                        style = Stroke(
                            width = strokeWidth,
                            pathEffect = PathEffect.dashPathEffect(
                                intervals = floatArrayOf(16f, 12f)
                            )
                        ),
                        cornerRadius = CornerRadius(radius)
                    )
                }
                .padding(horizontal = 24.dp, vertical = 40.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Image,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(56.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.markup_import_hint),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.markup_import_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                entries()
            }
        }
    }
}

@Composable
private fun ImportEntry(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(ShapeDefaults.default)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun QuickTasksSection(
    component: MarkupLayersComponent,
    setImage: (Uri) -> Unit,
    onMakeCover: () -> Unit,
) {
    // 任务化入口:选图并加载成功后直接执行对应操作;取消或加载失败时清除
    val taskPicker = rememberImagePicker(
        picker = Picker.Single,
        onFailure = { component.requestAiOpOnOpen(null) },
        onSuccess = { uris: List<Uri> -> uris.firstOrNull()?.let(setImage) }
    )
    val pickForOp: (AiImageOp) -> Unit = { op ->
        component.requestAiOpOnOpen(op)
        taskPicker.pickImage()
    }
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.studio_quick_tasks),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            QuickTaskEntry(
                icon = Icons.Filled.Transparency,
                label = stringResource(R.string.studio_task_remove_bg),
                onClick = { pickForOp(AiImageOp.Segment) },
                modifier = Modifier.weight(1f)
            )
            QuickTaskEntry(
                icon = Icons.Outlined.AutoFixHigh,
                label = stringResource(R.string.studio_task_repair_photo),
                onClick = { pickForOp(AiImageOp.Inpainting) },
                modifier = Modifier.weight(1f)
            )
            QuickTaskEntry(
                icon = Icons.Outlined.ImageOverlay,
                label = stringResource(R.string.studio_task_make_cover),
                onClick = onMakeCover,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuickTaskEntry(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(ShapeDefaults.default)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            autoSize = TextAutoSize.StepBased(
                minFontSize = 8.sp,
                maxFontSize = MaterialTheme.typography.labelMedium.fontSize,
                stepSize = 0.25.sp,
            )
        )
    }
}

@Composable
private fun SavedProjectsSection(component: MarkupLayersComponent) {
    var deleteProjectId by rememberSaveable { mutableStateOf<String?>(null) }
    var deleteProjectName by rememberSaveable { mutableStateOf("") }
    val dateFormat = remember {
        DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
    }

    Text(
        text = stringResource(R.string.studio_projects),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(Modifier.height(12.dp))
    if (component.projects.isEmpty()) {
        Text(
            text = stringResource(R.string.studio_projects_empty),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    } else {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(component.projects, key = { it.id }) { project ->
                val openDescription = stringResource(R.string.studio_project_open, project.name)
                Column(
                    modifier = Modifier
                        .size(width = 176.dp, height = 220.dp)
                        .clip(ShapeDefaults.default)
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Picture(
                        model = project.thumbnailPath,
                        contentDescription = openDescription,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(112.dp)
                            .clickable { component.openProject(project.id) }
                    )
                    Text(
                        text = project.name,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                    Text(
                        text = stringResource(
                            R.string.studio_project_updated,
                            dateFormat.format(Date(project.updatedAt))
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp, end = 4.dp)
                    ) {
                        TextButton(
                            onClick = { component.openProject(project.id) },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .semantics { contentDescription = openDescription }
                        ) {
                            Text(
                                text = pluralStringResource(
                                    R.plurals.studio_project_layers,
                                    project.layerCount,
                                    project.layerCount
                                ),
                                maxLines = 1
                            )
                        }
                        EnhancedIconButton(
                            onClick = {
                                deleteProjectId = project.id
                                deleteProjectName = project.name
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = stringResource(
                                    R.string.studio_project_delete,
                                    project.name
                                ),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
    EnhancedAlertDialog(
        visible = deleteProjectId != null,
        onDismissRequest = { deleteProjectId = null },
        title = { Text(stringResource(R.string.studio_project_delete_title)) },
        text = { Text(stringResource(R.string.studio_project_delete_message, deleteProjectName)) },
        dismissButton = {
            CancelButton(
                text = stringResource(R.string.markup_cancel),
                onClick = { deleteProjectId = null }
            )
        },
        confirmButton = {
            ConfirmButton(
                text = stringResource(R.string.markup_delete),
                onClick = {
                    deleteProjectId?.let(component::deleteProject)
                    deleteProjectId = null
                }
            )
        }
    )
}

/** Recent image files start a new project, unlike the editable projects above. */
@Composable
private fun RecentImagesSection(
    projects: List<RecentAccessEntity>,
    onProjectClick: (Uri) -> Unit,
) {
    var failedUris by remember { mutableStateOf(setOf<String>()) }
    val visibleProjects = projects.filter {
        it.uri.isNotBlank() && it.uri !in failedUris
    }

    Text(
        text = stringResource(R.string.studio_recent_files),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(Modifier.height(12.dp))
    if (visibleProjects.isEmpty()) {
        Text(
            text = stringResource(R.string.studio_recent_files_empty),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(visibleProjects, key = { it.uri }) { entry ->
            Picture(
                model = entry.uri,
                contentDescription = entry.displayName,
                contentScale = ContentScale.Crop,
                onError = { failedUris = failedUris + entry.uri },
                modifier = Modifier
                    .size(88.dp)
                    .clip(ShapeDefaults.default)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .clickable { onProjectClick(entry.uri.toUri()) }
            )
        }
    }
}
