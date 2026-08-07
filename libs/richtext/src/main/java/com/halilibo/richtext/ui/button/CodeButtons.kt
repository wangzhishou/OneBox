package com.halilibo.richtext.ui.button

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.halilibo.richtext.R
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassBackground
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.PlayCircle
import com.t8rin.imagetoolbox.core.resources.icons.ContentCopy
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSave

@Composable
fun CodeBlockActionButton(
    onClick: () -> Unit,
    leadingIcon: @Composable (() -> Unit) = {
        Icon(
            com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ContentCopy,
            contentDescription = stringResource(id = R.string.localized_description),
            Modifier.size(12.dp)
        )
    },
    title: String
) {
    val shape = RoundedCornerShape(50)
    Row(
        modifier = Modifier
            .clip(shape)
            .combinedClickable {
                onClick()
            }
            .glassBackground(
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                shape = shape
            )
            .padding(
                horizontal = 8.dp,
                vertical = 3.dp
            ),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        leadingIcon()
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun CopyCodeActionButton(
    title: String = stringResource(id = R.string.copy),
    onClick: () -> Unit
) {
    CodeBlockActionButton(
        onClick = onClick,
        leadingIcon = {
            Icon(
                com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ContentCopy,
                contentDescription = stringResource(id = R.string.localized_description),
                Modifier.size(10.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        title = title
    )
}

@Composable
fun CreateWidgetActionButton(
    title: String = stringResource(id = R.string.create_widget),
    onClick: () -> Unit
) {
    FilledTonalButton(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        // 1. 覆盖默认的内边距
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
        // 2. 使用 Modifier 覆盖最小高度限制
        modifier = Modifier.height(24.dp),
        content = {
            Icon(
                com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Add,
                contentDescription = stringResource(id = R.string.localized_description),
                modifier = Modifier.size(12.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp),
                // 建议也使用更小的字体
                style = MaterialTheme.typography.labelSmall
            )
        }
    )
}

@Composable
fun RunCodeActionButton(
    title: String = stringResource(id = R.string.run),
    onClick: () -> Unit
) {
    CodeBlockActionButton(
        onClick = onClick,
        leadingIcon = {
            Icon(
                com.t8rin.imagetoolbox.core.resources.Icons.Outlined.PlayCircle,
                contentDescription = stringResource(id = R.string.localized_description),
                Modifier.size(10.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        title = title
    )
}

@Composable
fun SaveCodeActionButton(
    title: String = stringResource(id = R.string.save),
    onClick: () -> Unit
) {
    CodeBlockActionButton(
        onClick = onClick,
        leadingIcon = {
            Icon(
                com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSave,
                contentDescription = stringResource(id = R.string.localized_description),
                Modifier.size(10.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        title = title
    )
}