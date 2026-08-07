package com.wanbaohe.camera.watermark.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalIconButton
import com.wanbaohe.camera.watermark.R
import com.wanbaohe.camera.watermark.domain.LogoType
import com.wanbaohe.camera.watermark.domain.WatermarkStyle
import com.wanbaohe.camera.watermark.util.localizedTemplateName
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.Edit

/**
 * 水印样式选择器
 * 展示预设模板供用户选择
 */
@Composable
fun StyleSelector(
    presets: List<WatermarkStyle>,
    selectedStyle: WatermarkStyle,
    onStyleSelected: (WatermarkStyle) -> Unit,
    onStyleCustomize: () -> Unit,
    onEditStyle: (WatermarkStyle) -> Unit = {},
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState()
) {
    // 样式列表（可滚动）
    LazyRow(
        state = listState,
        modifier = modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        stickyHeader {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(68.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                    ),
                contentAlignment = Alignment.CenterStart
            ) {
                SettingsItem(onClick = onStyleCustomize)
            }
        }
        items(presets, key = { it.id }) { style ->
            StylePresetItem(
                style = style,
                isSelected = style.id == selectedStyle.id,
                onClick = { onStyleSelected(style) },
                onEdit = { onEditStyle(style) }
            )
            if(style != presets.last()) {
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}

/**
 * 设置按钮卡片（固定在左侧）- 使用添加图标
 */
@Composable
private fun SettingsItem(
    onClick: () -> Unit
) {
    val shape = MaterialTheme.shapes.medium

    Box(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                shape = shape
            )
            .clip(shape)
            .clickable(onClick = onClick)
            .size(60.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Add,
            contentDescription = stringResource(R.string.camera_watermark_manage_template),
            modifier = Modifier.size(28.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 单个样式预设项（选中时显示编辑按钮）
 */
@Composable
private fun StylePresetItem(
    style: WatermarkStyle,
    isSelected: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit
) {
    val shape = MaterialTheme.shapes.medium
    // 预置模板名称本地化，用户自建模板显示库中的名称
    val displayName = localizedTemplateName(style)
    Box(
        modifier = Modifier
            .size(60.dp)
            .clip(shape)
            .background(
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHighest
                },
                shape = shape
            )
    ) {
        // 主卡片
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 显示 Logo 或文字
                LogoPreviewSmall(
                    logoType = style.logoType,
                    customLogoPath = style.customLogoPath,
                    fallbackText = displayName.first().toString(),
                    primaryTextColor = style.primaryTextColor
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = displayName,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }
        }

        // 右侧中间编辑按钮（仅选中时显示）
        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        shape = shape
                    )
                    .align(Alignment.Center),
                contentAlignment = Alignment.Center
            ) {
                GlassTonalIconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(20.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors().copy(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Edit,
                        contentDescription = stringResource(R.string.camera_watermark_edit),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

/**
 * 小尺寸 Logo 预览
 */
@Composable
private fun LogoPreviewSmall(
    logoType: LogoType,
    customLogoPath: String?,
    fallbackText: String,
    primaryTextColor: Long,
    modifier: Modifier = Modifier.size(28.dp)
) {
    when (logoType) {
        LogoType.NONE -> {
            // 无 Logo，显示文字
            Text(
                text = fallbackText,
                color = Color(primaryTextColor.toInt()),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        LogoType.LEICA -> {
            Image(
                painter = painterResource(id = R.drawable.leica_logo),
                contentDescription = stringResource(R.string.camera_watermark_style_leica),
                modifier = modifier,
                contentScale = ContentScale.Fit
            )
        }

        LogoType.WANBAOHE -> {
            Image(
                painter = painterResource(id = com.shifenmiao.core.R.drawable.logo),
                contentDescription = stringResource(R.string.camera_watermark_style_wanbaohe),
                modifier = modifier,
                contentScale = ContentScale.Fit
            )
        }

        LogoType.APPLE -> {
            Image(
                painter = painterResource(id = R.drawable.apple_logo),
                contentDescription = stringResource(R.string.camera_watermark_style_apple),
                modifier = modifier,
                contentScale = ContentScale.Fit
            )
        }

        LogoType.GOOGLE -> {
            Image(
                painter = painterResource(id = R.drawable.google_logo),
                contentDescription = stringResource(R.string.camera_watermark_style_google),
                modifier = modifier,
                contentScale = ContentScale.Fit
            )
        }

        LogoType.HUAWEI -> {
            Image(
                painter = painterResource(id = R.drawable.huawei_logo),
                contentDescription = stringResource(R.string.camera_watermark_style_huawei),
                modifier = modifier,
                contentScale = ContentScale.Fit
            )
        }

        LogoType.OPPO -> {
            Image(
                painter = painterResource(id = R.drawable.oppo_logo),
                contentDescription = stringResource(R.string.camera_watermark_style_oppo),
                modifier = modifier,
                contentScale = ContentScale.Fit
            )
        }

        LogoType.VIVO -> {
            Image(
                painter = painterResource(id = R.drawable.vivo_logo),
                contentDescription = stringResource(R.string.camera_watermark_style_vivo),
                modifier = modifier,
                contentScale = ContentScale.Fit
            )
        }

        LogoType.XIAOMI -> {
            Image(
                painter = painterResource(id = R.drawable.xiaomi_logo),
                contentDescription = stringResource(R.string.camera_watermark_style_xiaomi),
                modifier = modifier,
                contentScale = ContentScale.Fit
            )
        }

        LogoType.ONEPLUS -> {
            Image(
                painter = painterResource(id = R.drawable.oneplus_logo),
                contentDescription = stringResource(R.string.camera_watermark_style_oneplus),
                modifier = modifier,
                contentScale = ContentScale.Fit
            )
        }

        LogoType.CUSTOM -> {
            if (customLogoPath != null) {
                AsyncImage(
                    model = customLogoPath.toUri(),
                    contentDescription = stringResource(R.string.camera_watermark_custom),
                    modifier = modifier,
                    contentScale = ContentScale.Fit
                )
            } else {
                Text(
                    text = fallbackText,
                    color = Color(primaryTextColor.toInt()),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}
