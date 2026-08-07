package com.wanbaohe.camera.watermark.presentation.components

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.ui.ZoomableImagePreview
import com.t8rin.imagetoolbox.core.ui.widget.image.Picture
import com.wanbaohe.camera.watermark.R

/**
 * 水印预览组件
 * 展示带水印效果的图片，支持手势操作（缩放、拖动、双击）
 * @param imageBitmap 预览图片 (使用 ImageBitmap 避免 Bitmap recycle 问题)
 * @param isLoading 是否加载中
 * @param isImmersive 是否处于沉浸式模式（沉浸式模式下图片适应屏幕宽度，无内边距）
 * @param contentPadding 内边距（避免被顶部/底部栏遮挡，沉浸式模式下忽略）
 * @param onClick 点击回调，用于切换沉浸式模式
 */
@Composable
fun WatermarkPreview(
    imageBitmap: ImageBitmap?,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    isImmersive: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        when {
            imageBitmap != null || isLoading -> {
                // 使用可缩放的图片预览组件
                ZoomableImagePreview(
                    imageBitmap = imageBitmap,
                    isLoading = isLoading,
                    isImmersive = isImmersive,
                    contentPadding = contentPadding,
                    onTap = onClick,
                    modifier = Modifier.fillMaxSize(),
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
            else -> {
                Text(
                    text = stringResource(R.string.camera_watermark_select_image_hint),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 紧凑型水印预览（用于批量列表）
 */
@Composable
fun CompactWatermarkPreview(
    bitmap: Bitmap?,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(8.dp)
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        Color.Transparent
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(2.dp, borderColor, shape)
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            // 使用 Picture 组件显示图片
            Picture(
                model = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                shimmerEnabled = false,
                showTransparencyChecker = false
            )
        }
    }
}

