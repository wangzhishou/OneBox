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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.database.recent_access.entity.RecentAccessEntity
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.ArrowBack
import com.t8rin.imagetoolbox.core.resources.icons.CameraAlt
import com.t8rin.imagetoolbox.core.resources.icons.ContentPaste
import com.t8rin.imagetoolbox.core.resources.icons.Image
import com.t8rin.imagetoolbox.core.resources.icons.NoteAdd
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.ImagePickerMode
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.rememberImagePicker
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.utils.helper.rememberClipboardData
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedIconButton
import com.t8rin.imagetoolbox.core.ui.widget.image.Picture
import com.t8rin.imagetoolbox.core.ui.widget.modifier.ShapeDefaults
import com.wanbaohe.markuplayers.R
import com.wanbaohe.markuplayers.presentation.screenLogic.MarkupLayersComponent

/**
 * 空状态首页(设计稿「图片创作」):
 * 居中大卡片 + 相册/相机/粘贴/空白画布导入入口,下方「最近项目」横排缩略图
 * (公共最近访问表:打开图片/保存成功时记录,uri 去重,组件内 Flow 持续观察)。
 * 设计稿的「示例图」入口因项目无现成示例图资源,本期省略。
 * 顶栏走 [BaseScreen]:不传 actions,右上角显示默认 actionIcon(TopAppBarEmoji)。
 */
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
                        Spacer(Modifier.height(24.dp))
                        RecentProjectsSection(
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
        onCreate = { width, height, backgroundColor ->
            showBlankCanvasSheet = false
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
    val dashColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
    val cornerRadius = 24.dp
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
            .clip(RoundedCornerShape(cornerRadius))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .clickable(onClick = onImportClick)
            .padding(horizontal = 24.dp, vertical = 40.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.Image,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
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
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
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

/** 最近项目:横排缩略图圆角卡片,点按直接以该图进入编辑;加载失败的项剔除 */
@Composable
private fun RecentProjectsSection(
    projects: List<RecentAccessEntity>,
    onProjectClick: (Uri) -> Unit,
) {
    var failedUris by remember { mutableStateOf(setOf<String>()) }
    val visibleProjects = projects.filter {
        it.uri.isNotBlank() && it.uri !in failedUris
    }

    Text(
        text = stringResource(R.string.markup_recent_projects),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(Modifier.height(12.dp))
    if (visibleProjects.isEmpty()) {
        Text(
            text = stringResource(R.string.markup_recent_projects_empty),
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
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .clickable { onProjectClick(entry.uri.toUri()) }
            )
        }
    }
}
