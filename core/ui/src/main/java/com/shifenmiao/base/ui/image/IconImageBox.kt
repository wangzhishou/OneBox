package com.shifenmiao.base.ui.image

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Size
import com.shifenmiao.base.ui.icon.BuildCustomIcon
import com.shifenmiao.theme.AppTheme

@Composable
fun IconImageBox(
    modifier: Modifier,
    textColor: Color? = MaterialTheme.colorScheme.onSurfaceVariant,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(0.3f),
    shape: Shape = RoundedCornerShape(AppTheme.dimens.cornerRadiusNormal),
    iconPath: String? = null,
    firstCharacter: String = "null",
    size: Dp = AppTheme.dimens.cardIconSize,
    iconName: String? = null,
) {
    val effectiveTextColor = textColor ?: MaterialTheme.colorScheme.onSurfaceVariant
    val boxModifier = remember(size, shape) {
        Modifier
            .clip(shape)
            .background(
                color = backgroundColor,
                shape = shape
            )
            .size(size)
    }
    
    when {
        iconName?.isNotEmpty() == true -> {
            Box(
                modifier = boxModifier,
                contentAlignment = Alignment.Center
            ) {
                BuildCustomIcon(
                    modifier = modifier.size(16.dp),
                    iconName = iconName,
                    tint = effectiveTextColor
                )
            }
        }
        iconPath?.isNotEmpty() == true -> {
            val context = LocalContext.current
            // 优化图像请求，使用稳定的关键字避免重组
            val imageRequest = remember(iconPath) {
                ImageRequest.Builder(context)
                    .data(iconPath)
                    .size(Size.ORIGINAL)
                    .crossfade(true)
                    .diskCacheKey(iconPath)
                    .memoryCacheKey(iconPath)
                    .build()
            }
            
            // 使用 SubcomposeAsyncImage 处理加载状态
            SubcomposeAsyncImage(
                model = imageRequest,
                contentDescription = firstCharacter,
                contentScale = ContentScale.FillWidth,
                modifier = modifier
                    .size(size)
                    .clip(shape),
                loading = {
                    AvatarImage(
                        modifier = Modifier,
                        backgroundColor = backgroundColor,
                        textColor = effectiveTextColor,
                        text = firstCharacter,
                        size = size,
                        shape = RoundedCornerShape(AppTheme.dimens.cornerRadiusNormal)
                    )
                },
                error = {
                    AvatarImage(
                        modifier = Modifier,
                        backgroundColor = backgroundColor,
                        textColor = effectiveTextColor,
                        text = firstCharacter,
                        size = size,
                        shape = RoundedCornerShape(AppTheme.dimens.cornerRadiusNormal)
                    )
                }
            )
        }
        else -> {
            AvatarImage(
                modifier = modifier,
                backgroundColor = backgroundColor,
                textColor = effectiveTextColor,
                text = firstCharacter,
                size = size,
                shape = RoundedCornerShape(AppTheme.dimens.cornerRadiusNormal)
            )
        }
    }
}

@Composable
fun IconBox(
    modifier: Modifier = Modifier,
    contentColor: Color? = MaterialTheme.colorScheme.onSurfaceVariant,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    shape: Shape = RoundedCornerShape(50),
    firstCharacter: String = "null",
    size: Dp = 24.dp,
    iconName: String? = null
) {
    val effectiveContentColor = contentColor ?: MaterialTheme.colorScheme.onSurfaceVariant
    // 缓存修饰符以避免重组
    val boxModifier = remember(size, shape, containerColor) {
        Modifier
            .clip(shape)
            .background(
                color = containerColor,
                shape = shape
            )
            .size(size)
    }
    
    when {
        iconName?.isNotEmpty() == true -> {
            Box(
                modifier = boxModifier,
                contentAlignment = Alignment.Center
            ) {
                BuildCustomIcon(
                    modifier = modifier.size(12.dp),
                    iconName = iconName,
                    tint = effectiveContentColor
                )
            }
        }
        else -> {
            AvatarImage(
                modifier = modifier,
                backgroundColor = containerColor,
                textColor = effectiveContentColor,
                text = firstCharacter,
                size = size,
                shape = RoundedCornerShape(AppTheme.dimens.cornerRadiusNormal)
            )
        }
    }
}

@Composable
fun IconImage(
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    shape: Shape = RoundedCornerShape(AppTheme.dimens.cornerRadiusNormal),
    iconPath: String? = null,
    firstCharacter: String = "null",
    alpha: Float = DefaultAlpha,
    iconName: String? = null,
    size: Dp = 100.dp,
) {
    when {
        iconName?.isNotEmpty() == true -> {
            BuildCustomIcon(
                modifier = modifier.size(size),
                iconName = iconName,
                tint = textColor
            )
        }
        iconPath?.isNotEmpty() == true -> {
            val context = LocalContext.current
            val imageRequest = remember(iconPath) {
                ImageRequest.Builder(context)
                    .data(iconPath)
                    .size(Size.ORIGINAL)
                    .crossfade(true)
                    .diskCacheKey(iconPath)
                    .memoryCacheKey(iconPath)
                    .build()
            }
            
            // 使用 SubcomposeAsyncImage 处理加载状态
            SubcomposeAsyncImage(
                model = imageRequest,
                contentDescription = firstCharacter,
                contentScale = ContentScale.Fit,
                alpha = alpha,
                modifier = modifier.clip(shape),
                loading = {
                    AvatarImage(
                        modifier = modifier.size(size),
                        textColor = textColor,
                        text = firstCharacter,
                        textSize = size * 2,
                        alpha = alpha,
                        shape = RoundedCornerShape(AppTheme.dimens.cornerRadiusNormal)
                    )
                },
                error = {
                    AvatarImage(
                        modifier = modifier.size(size),
                        textColor = textColor,
                        text = firstCharacter,
                        textSize = size * 2,
                        alpha = alpha,
                        shape = RoundedCornerShape(AppTheme.dimens.cornerRadiusNormal)
                    )
                }
            )
        }
        else -> {
            AvatarImage(
                modifier = modifier.size(size),
                textColor = textColor,
                text = firstCharacter,
                textSize = size * 2,
                alpha = alpha,
                shape = RoundedCornerShape(AppTheme.dimens.cornerRadiusNormal)
            )
        }
    }
}
