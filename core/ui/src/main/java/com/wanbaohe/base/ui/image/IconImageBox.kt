/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.wanbaohe.base.ui.image

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
import com.wanbaohe.base.ui.icon.BuildCustomIcon
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
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
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
