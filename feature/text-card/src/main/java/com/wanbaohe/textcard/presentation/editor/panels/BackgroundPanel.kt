package com.wanbaohe.textcard.presentation.editor.panels

import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.rememberImagePicker
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedSliderItem
import com.t8rin.imagetoolbox.core.ui.widget.image.Picture
import com.t8rin.imagetoolbox.core.ui.widget.modifier.meshGradient
import com.wanbaohe.textcard.R
import com.wanbaohe.textcard.domain.model.BackgroundSpec
import com.wanbaohe.textcard.domain.model.GradientPresets
import com.wanbaohe.textcard.domain.model.PaperKind
import com.wanbaohe.textcard.domain.render.MESH_RESOLUTION
import com.wanbaohe.textcard.domain.render.toPointPairs
import com.wanbaohe.textcard.presentation.editor.MeshGradientEditorSheet
import com.wanbaohe.textcard.presentation.editor.drawPaperTexture
import com.wanbaohe.textcard.presentation.screenLogic.TextCardComponent
import kotlin.math.roundToInt
import androidx.compose.material.icons.Icons as MaterialIcons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Refresh

/**
 * 纸张背景面板(设计稿 02):纸张网格 + 渐变色卡 + 自定义(相册)+ 背景透明度。
 */
@Composable
fun BackgroundPanel(component: TextCardComponent) {
    val background = component.background
    // mesh 渐变编辑弹层的初始网格:点预设色卡 = 以该预设为初值进编辑器;
    // 自定义入口 = 以当前背景渐变(或默认预设)起步
    var meshEditorSeed by remember { mutableStateOf<BackgroundSpec.Gradient?>(null) }
    var showMeshEditor by remember { mutableStateOf(false) }

    val imagePicker = rememberImagePicker { uri: Uri ->
        component.updateBackground(BackgroundSpec.Image(uri.toString()))
    }

    // 纸张标题行:右侧刷新按钮(后台新发布纸张后客户端列表不更新,手动重拉)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        PanelTitle(
            titleRes = R.string.textcard_paper_background,
            modifier = Modifier.weight(1f)
        )
        val refreshing = component.remotePapersRefreshing
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .clickable(enabled = !refreshing) { component.refreshRemotePapers() }
        ) {
            if (refreshing) {
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Icon(
                    imageVector = MaterialIcons.Outlined.Refresh,
                    contentDescription = stringResource(R.string.textcard_refresh_papers),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
    // 纸张横向滑动列表:内置纸张在前,远程(Strapi)已就绪纸张追加在尾部
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        item(key = "none") {
            PaperCell(
                label = stringResource(R.string.textcard_paper_none),
                selected = background is BackgroundSpec.None,
                onClick = { component.updateBackground(BackgroundSpec.None) }
            ) {
                Icon(
                    imageVector = MaterialIcons.Outlined.Block,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        items(
            items = PaperKind.entries,
            key = { "builtin_$it" }
        ) { kind ->
            PaperCell(
                label = stringResource(kind.labelRes),
                selected = background == BackgroundSpec.Paper(kind),
                onClick = { component.updateBackground(BackgroundSpec.Paper(kind)) }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) { drawPaperTexture(kind) }
            }
        }
        items(
            items = component.remotePapers,
            key = { "remote_${it.localPath}" }
        ) { paper ->
            PaperCell(
                label = paper.title,
                selected = (background as? BackgroundSpec.Image)?.uri == paper.localPath,
                onClick = {
                    component.updateBackground(BackgroundSpec.Image(paper.localPath))
                }
            ) {
                Picture(
                    model = paper.localPath,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    showTransparencyChecker = false,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    PanelTitle(
        titleRes = R.string.textcard_gradient,
        modifier = Modifier.padding(top = 16.dp)
    )
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        GradientPresets.all.forEach { preset ->
            SelectableCell(
                selected = background == preset,
                onClick = {
                    meshEditorSeed = preset
                    showMeshEditor = true
                },
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .meshGradient(
                            points = preset.toPointPairs(),
                            resolutionX = MESH_RESOLUTION,
                            resolutionY = MESH_RESOLUTION
                        )
                )
            }
        }
        // 自定义渐变:打开 mesh 编辑弹层(以当前背景渐变为初值)
        SelectableCell(
            selected = background is BackgroundSpec.Gradient && background !in GradientPresets.all,
            onClick = {
                meshEditorSeed = null
                showMeshEditor = true
            },
            modifier = Modifier.weight(1f)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = MaterialIcons.Outlined.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.textcard_custom),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    PanelTitle(
        titleRes = R.string.textcard_custom_background,
        modifier = Modifier.padding(top = 16.dp)
    )
    // 自定义背景:相册选择 + 图片创作引导,与纸张项同款小卡(96dp 框 + 下方名称)
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        PaperCell(
            label = stringResource(R.string.textcard_pick_from_gallery),
            selected = background is BackgroundSpec.Image,
            onClick = { imagePicker.pickImage() }
        ) {
            if (background is BackgroundSpec.Image) {
                Picture(
                    model = background.uri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    showTransparencyChecker = false,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = MaterialIcons.Outlined.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        // 引导去图片创作做底图(同名小卡,文案两行打全)
        PaperCell(
            label = stringResource(R.string.textcard_go_image_creation),
            selected = false,
            onClick = { component.onNavigate(Screen.MarkupLayers()) },
            labelMaxLines = 2
        ) {
            Icon(
                imageVector = MaterialIcons.Outlined.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    EnhancedSliderItem(
        value = component.backgroundOpacity,
        title = stringResource(R.string.textcard_background_opacity),
        valueRange = 0f..1f,
        onValueChange = component::updateBackgroundOpacity,
        internalStateTransformation = { (it * 100).roundToInt() },
        valueSuffix = "%",
        modifier = Modifier.padding(top = 8.dp)
    )

    MeshGradientEditorSheet(
        visible = showMeshEditor,
        component = component,
        seed = meshEditorSeed,
        onDismiss = { showMeshEditor = false }
    )
}

@Composable
internal fun PanelTitle(
    titleRes: Int,
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(titleRes),
        style = MaterialTheme.typography.titleSmall,
        modifier = modifier.padding(vertical = 8.dp)
    )
}

/** 纸张单元格:96dp 宽 4:3 预览 + 底部名称(横向列表项) */
@Composable
private fun PaperCell(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    labelMaxLines: Int = 1,
    content: @Composable () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(96.dp)
    ) {
        SelectableCell(
            selected = selected,
            onClick = onClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                content()
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = labelMaxLines,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

/** 4:3 可选中单元格:选中态主色描边 + 右上角对勾 */
@Composable
private fun SelectableCell(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier.aspectRatio(4f / 3f)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
                .border(
                    width = if (selected) 2.dp else 1.dp,
                    color = if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable(onClick = onClick)
        ) {
            content()
        }
        if (selected) {
            Icon(
                imageVector = MaterialIcons.Outlined.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(18.dp)
            )
        }
    }
}
