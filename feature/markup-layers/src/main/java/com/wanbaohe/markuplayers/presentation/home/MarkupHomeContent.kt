package com.wanbaohe.markuplayers.presentation.home

import android.net.Uri
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.ArrowBack
import com.t8rin.imagetoolbox.core.resources.icons.CameraAlt
import com.t8rin.imagetoolbox.core.resources.icons.ContentPaste
import com.t8rin.imagetoolbox.core.resources.icons.Image
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.ImagePickerMode
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.rememberImagePicker
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.utils.helper.rememberClipboardData
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedIconButton
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedTopAppBar
import com.t8rin.imagetoolbox.core.ui.widget.modifier.ShapeDefaults
import com.wanbaohe.markuplayers.R
import com.wanbaohe.markuplayers.presentation.screenLogic.MarkupLayersComponent

/**
 * 空状态首页(设计稿「图片处理控制台」):
 * 居中大卡片 + 相册/相机/粘贴三个导入入口。
 * 设计稿的「示例图」入口因项目无现成示例图资源,本期省略。
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

    Column(modifier = Modifier.fillMaxSize()) {
        EnhancedTopAppBar(
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
            navigationIcon = {
                EnhancedIconButton(onClick = component.onGoBack) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.markup_back)
                    )
                }
            }
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
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
                }
            )
        }
    }
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
            modifier = Modifier.fillMaxWidth()
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
