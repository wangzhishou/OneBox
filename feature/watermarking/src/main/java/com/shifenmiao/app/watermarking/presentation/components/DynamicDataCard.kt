/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package com.shifenmiao.app.watermarking.presentation.components

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDownload
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSync
import com.wanbaohe.core.ui.widget.icon_shape.IconShapeContainer
import com.wanbaohe.core.ui.widget.modifier.ShapeDefaults
import com.wanbaohe.core.ui.widget.modifier.flatGlassContainer
import com.wanbaohe.core.ui.widget.system.OneBoxPillButton

private enum class FetchAction(
    @StringRes val labelRes: Int,
    val loading: Boolean = false,
) {
    FETCH(R.string.watermark_fetch_action),
    LOADING(R.string.watermark_fetching_action, loading = true),
    RETRY(R.string.watermark_fetch_retry_action),
    REFRESH(R.string.watermark_fetch_refresh_action),
}

/**
 * 动态数据（位置 / 海拔 / 天气）的获取卡片 —— 与图层列表同一套 listCard 结构：
 * 左侧主标题 + 说明（或已经取到的值），右侧一颗随状态变化的获取按钮。
 *
 * 状态全部收敛在按钮上：
 * - 未取到：[获取]
 * - 获取中：[获取中] + 转圈（按钮禁用，整张卡片也不可点）
 * - 失败：[重试]，说明文字变成错误提示
 * - 取到了：[刷新]，说明位置直接显示取到的值
 */
@Composable
fun DynamicDataCard(
    title: String,
    hint: String,
    value: String?,
    icon: ImageVector,
    isLoading: Boolean,
    failed: Boolean,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val action = when {
        isLoading -> FetchAction.LOADING
        failed -> FetchAction.RETRY
        value != null -> FetchAction.REFRESH
        else -> FetchAction.FETCH
    }
    val containerColor = when (action) {
        FetchAction.LOADING, FetchAction.FETCH -> MaterialTheme.colorScheme.primaryContainer
        FetchAction.RETRY -> MaterialTheme.colorScheme.errorContainer
        FetchAction.REFRESH -> MaterialTheme.colorScheme.secondaryContainer
    }
    val contentColor = when (action) {
        FetchAction.LOADING, FetchAction.FETCH -> MaterialTheme.colorScheme.onPrimaryContainer
        FetchAction.RETRY -> MaterialTheme.colorScheme.onErrorContainer
        FetchAction.REFRESH -> MaterialTheme.colorScheme.onSecondaryContainer
    }
    val statusColor = when {
        value != null -> MaterialTheme.colorScheme.primary
        failed || action == FetchAction.RETRY -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(ShapeDefaults.large)
            .flatGlassContainer(
                shape = ShapeDefaults.large,
                resultPadding = 0.dp,
            )
            .clickable(enabled = !isLoading, onClick = onAction)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconShapeContainer(enabled = true) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = value ?: hint,
                style = MaterialTheme.typography.bodySmall,
                color = statusColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(10.dp))
        FetchPillButton(
            action = action,
            containerColor = containerColor,
            contentColor = contentColor,
            onClick = onAction,
        )
    }
}

@Composable
private fun FetchPillButton(
    action: FetchAction,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
) {
    OneBoxPillButton(
        text = stringResource(action.labelRes),
        onClick = onClick,
        enabled = !action.loading,
        containerColor = containerColor,
        contentColor = contentColor,
        textStyle = MaterialTheme.typography.labelMedium,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        leadingIcon = {
            when (action) {
                FetchAction.LOADING -> CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    strokeWidth = 1.8.dp,
                    color = contentColor,
                )
                FetchAction.RETRY, FetchAction.REFRESH -> Icon(
                    imageVector = Icons.Outlined.LineSync,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(14.dp),
                )
                FetchAction.FETCH -> Icon(
                    imageVector = Icons.Outlined.LineDownload,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(14.dp),
                )
            }
        },
        modifier = Modifier.widthIn(min = 84.dp),
    )
}
