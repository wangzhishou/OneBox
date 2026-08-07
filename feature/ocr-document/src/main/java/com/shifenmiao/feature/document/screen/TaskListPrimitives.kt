package com.shifenmiao.feature.document.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shifenmiao.feature.ocr.document.R
import com.shifenmiao.model.ocr.OcrTaskStatus
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedAlertDialog
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedButton
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassBackground
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.t8rin.imagetoolbox.core.resources.icons.line.LineHistory
import com.t8rin.imagetoolbox.core.resources.icons.CheckCircle
import com.t8rin.imagetoolbox.core.resources.icons.Delete
import com.t8rin.imagetoolbox.core.resources.icons.Refresh
import com.t8rin.imagetoolbox.core.resources.icons.line.LineImage
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePdf
import com.t8rin.imagetoolbox.core.resources.icons.line.LineWarning

@Composable
internal fun AddTaskCard(
    isGridMode: Boolean,
    onImageClick: () -> Unit,
    onImageLongClick: () -> Unit,
    onPdfClick: () -> Unit,
    tipsText: String,
    descriptionText: String,
    content: @Composable ColumnScope.() -> Unit = { },
) {
    val buttonSize = if (isGridMode) 60.dp else 80.dp
    val iconSize = if (isGridMode) 16.dp else 32.dp
    val shape = MaterialTheme.shapes.medium
    GlassCard(
        modifier = Modifier
            .heightIn(min = 220.dp, max = 280.dp)
            .fillMaxWidth(),
        shape = shape,
        containerAlpha = GlassStyle.Regular.backgroundAlpha
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(
                    state = rememberScrollState()
                )
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            FlowRow(
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(buttonSize)
                            .glassBackground(
                                style = GlassStyle.Thin,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = shape
                            )
                            .clip(shape)
                            .combinedClickable(
                                onClick = onImageClick,
                                onLongClick = onImageLongClick
                            )
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineImage,
                                contentDescription = stringResource(R.string.image),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(iconSize)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.image),
                                style = if (isGridMode) {
                                    MaterialTheme.typography.titleSmall
                                } else {
                                    MaterialTheme.typography.titleMedium
                                },
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .size(buttonSize)
                            .glassBackground(
                                style = GlassStyle.Thin,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = shape
                            )
                            .clip(shape)
                            .clickable(onClick = onPdfClick)
                    ) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePdf,
                            contentDescription = stringResource(R.string.pdf),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(iconSize)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.pdf),
                            style = if (isGridMode) {
                                MaterialTheme.typography.titleSmall
                            } else {
                                MaterialTheme.typography.titleMedium
                            },
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = tipsText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = descriptionText,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.outline
            )
            content()
        }
    }
}

@Composable
internal fun EmptyStateView() {
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = stringResource(R.string.empty_state_title),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
internal fun StatusChip(status: String, isGridMode: Boolean) {
    val (color, icon, label) = when (status) {
        OcrTaskStatus.SUCCESS.value -> Triple(
            MaterialTheme.colorScheme.primary,
            com.t8rin.imagetoolbox.core.resources.Icons.Outlined.CheckCircle,
            stringResource(R.string.status_success)
        )

        OcrTaskStatus.FAILED.value -> Triple(
            MaterialTheme.colorScheme.error,
            com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineWarning,
            stringResource(R.string.status_failed)
        )

        OcrTaskStatus.PROCESSING.value -> Triple(
            MaterialTheme.colorScheme.secondary,
            com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Refresh,
            stringResource(R.string.status_processing)
        )

        else -> Triple(
            MaterialTheme.colorScheme.onSurfaceVariant,
            com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineHistory,
            stringResource(R.string.status_pending)
        )
    }
    val size = if (isGridMode) {
        24.dp
    } else {
        36.dp
    }
    AssistChip(
        modifier = Modifier.height(size),
        onClick = {},
        label = { Text(label) },
        leadingIcon = {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = color
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = color.copy(0.2f),
            labelColor = color
        ),
        border = null,
        shape = MaterialTheme.shapes.medium
    )
}

@Composable
internal fun DeleteConfirmationDialog(
    visible: Boolean,
    fileName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    EnhancedAlertDialog(
        visible = visible,
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Delete,
                contentDescription = null
            )
        },
        title = { Text(stringResource(R.string.delete_task)) },
        text = {
            Text(
                text = stringResource(R.string.delete_task_confirmation, fileName),
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            EnhancedButton(
                onClick = onConfirm,
                containerColor = MaterialTheme.colorScheme.error
            ) {
                Text(stringResource(R.string.delete))
            }
        },
        dismissButton = {
            EnhancedButton(
                onClick = onDismiss,
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

internal fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
