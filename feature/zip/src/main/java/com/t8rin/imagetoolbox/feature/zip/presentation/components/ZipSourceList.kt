package com.t8rin.imagetoolbox.feature.zip.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.Delete
import com.wanbaohe.core.ui.utils.file.FileTypeIconProvider
import com.wanbaohe.core.ui.widget.file.FileTypeBadge
import com.wanbaohe.core.ui.widget.glass.GlassButton
import com.wanbaohe.core.ui.widget.glass.GlassOutlinedButton
import com.wanbaohe.core.ui.widget.glass.glassRegular
import com.wanbaohe.core.ui.widget.text.AutoSizeText
import com.t8rin.imagetoolbox.feature.zip.R
import com.t8rin.imagetoolbox.feature.zip.domain.ZipSourceFile
import com.t8rin.imagetoolbox.feature.zip.presentation.rememberFormattedSize

/**
 * 制作压缩包页的空状态：虚线添加图标 + 说明 + 添加文件按钮。
 */
@Composable
internal fun ZipEmptyState(
    onAddFiles: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ZipEmptyIllustration()

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = stringResource(R.string.zip_empty_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.zip_empty_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        GlassOutlinedButton(
            onClick = onAddFiles,
            modifier = Modifier.height(48.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            AutoSizeText(
                text = stringResource(R.string.zip_add_files),
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ZipEmptyIllustration(modifier: Modifier = Modifier) {
    val containerColor = MaterialTheme.colorScheme.primaryContainer
    val accentColor = MaterialTheme.colorScheme.onPrimaryContainer

    Box(
        modifier = modifier
            .size(152.dp)
            .clip(CircleShape)
            .background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(76.dp)) {
            val strokeWidth = 2.4.dp.toPx()
            val inset = strokeWidth / 2
            val cornerRadius = 14.dp.toPx()
            val dashedBorder = Path().apply {
                addRoundRect(
                    RoundRect(
                        left = inset,
                        top = inset,
                        right = size.width - inset,
                        bottom = size.height - inset,
                        cornerRadius = CornerRadius(cornerRadius, cornerRadius)
                    )
                )
            }
            drawPath(
                path = dashedBorder,
                color = accentColor,
                style = Stroke(
                    width = strokeWidth,
                    pathEffect = PathEffect.dashPathEffect(
                        intervals = floatArrayOf(6.dp.toPx(), 5.dp.toPx())
                    )
                )
            )

            val centerX = size.width / 2
            val centerY = size.height / 2
            val arm = 13.dp.toPx()
            val plusStroke = 3.dp.toPx()
            drawLine(
                color = accentColor,
                start = Offset(centerX - arm, centerY),
                end = Offset(centerX + arm, centerY),
                strokeWidth = plusStroke,
                cap = StrokeCap.Round
            )
            drawLine(
                color = accentColor,
                start = Offset(centerX, centerY - arm),
                end = Offset(centerX, centerY + arm),
                strokeWidth = plusStroke,
                cap = StrokeCap.Round
            )
        }
    }
}

/**
 * 已添加文件：标题 + 数量/体积汇总 + 文件卡片列表。
 */
@Composable
internal fun ZipSourceFileList(
    files: List<ZipSourceFile>,
    totalSize: Long,
    onRemove: (ZipSourceFile) -> Unit,
    modifier: Modifier = Modifier
) {
    val formattedTotalSize = rememberFormattedSize(totalSize)
    val navigationBarInset = WindowInsets.navigationBars
        .asPaddingValues()
        .calculateBottomPadding()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 8.dp,
            // 给底部通栏按钮留位，滚动到底时最后一行不被挡住
            bottom = 88.dp + navigationBarInset
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item(key = "zip_source_header") {
            Column(modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)) {
                Text(
                    text = stringResource(R.string.zip_added_files),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = pluralStringResource(
                        R.plurals.zip_added_summary,
                        files.size,
                        files.size,
                        formattedTotalSize
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(
            items = files,
            key = { it.uri }
        ) { file ->
            ZipSourceFileRow(
                file = file,
                onRemove = { onRemove(file) }
            )
        }
    }
}

@Composable
private fun ZipSourceFileRow(
    file: ZipSourceFile,
    onRemove: () -> Unit
) {
    val formattedSize = rememberFormattedSize(file.size)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .glassRegular(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainer
            )
            .padding(start = 14.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FileTypeBadge(
            category = FileTypeIconProvider.categoryOf(
                isDirectory = false,
                extension = file.extension,
                mimeType = file.mimeType
            )
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = file.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = formattedSize,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        IconButton(onClick = onRemove) {
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = stringResource(R.string.zip_remove_file),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 底部通栏主按钮。空状态时保持可见但不可点，明确"下一步"。
 */
@Composable
internal fun ZipBottomActionBar(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        GlassButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            AutoSizeText(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}
