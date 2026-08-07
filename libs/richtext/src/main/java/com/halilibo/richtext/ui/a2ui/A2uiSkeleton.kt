package com.halilibo.richtext.ui.a2ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.halilibo.richtext.R
import com.shifenmiao.core.ui.skeleton.shimmerLoading

/**
 * a2ui 代码块流式输出时的骨架占位组件。
 *
 * 模拟一张简单卡片布局：标题行 + 内容行 + 按钮行，
 * 带 shimmer 动画，提示用户内容正在生成。
 */
@Composable
fun A2uiSkeletonPlaceholder(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = MaterialTheme.shapes.medium
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 标题行占位
        Box(
            modifier = Modifier
                .width(120.dp)
                .height(16.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                .shimmerLoading()
        )

        // 内容区域占位
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 长文本行
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    .shimmerLoading()
            )
            // 中等文本行
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    .shimmerLoading()
            )
            // 短文本行
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    .shimmerLoading()
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 按钮占位
        Box(
            modifier = Modifier
                .align(Alignment.End)
                .width(80.dp)
                .height(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                .shimmerLoading()
        )
    }
}

/**
 * a2ui 输出完成后解析失败的占位。不要继续显示骨架，否则用户会误以为还在加载。
 */
@Composable
fun A2uiErrorPlaceholder(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.28f),
                shape = MaterialTheme.shapes.medium
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.a2ui_parse_failed),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onErrorContainer
        )
        Text(
            text = message.ifBlank { stringResource(R.string.a2ui_parse_failed_hint) },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.82f)
        )
    }
}

