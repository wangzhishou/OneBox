package com.shifenmiao.ai.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.NoteAdd
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.shifenmiao.base.ui.ActionButton
import com.shifenmiao.base.ui.DeleteConfirmDialog
import com.shifenmiao.database.ai.entity.MessageEntity
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.utils.helper.Clipboard
import com.t8rin.imagetoolbox.core.resources.icons.Delete
import com.t8rin.imagetoolbox.core.resources.icons.NoteAdd
import com.t8rin.imagetoolbox.core.resources.icons.Refresh
import com.t8rin.imagetoolbox.core.resources.icons.ContentCopy
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCallMade

@Composable
fun AIUsageBar(
    modifier: Modifier = Modifier,
    message: MessageEntity,
    isShowReGenerate: Boolean = false,
    onDelete: (String) -> Unit = {},
    reGenerate: () -> Unit = {},
    onShare: (String) -> Unit = {},
    onNoteAdd: () -> Unit = {},
) {
    val showDeleteDialog = remember { mutableStateOf(false) }

    Row(
        modifier = modifier.padding(
            horizontal = AppTheme.dimens.paddingNormal,
            vertical = AppTheme.dimens.paddingNormal
        ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isShowReGenerate) {
            ActionButton(
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Refresh,
                contentDescription = "",
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                onClick = {
                    reGenerate.invoke()
                }
            )
            Spacer(modifier = Modifier.width(AppTheme.dimens.spaceSmall))
        }
        ActionButton(
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ContentCopy,
            contentDescription = "",
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            onClick = {
                Clipboard.copy(message.answer)
            }
        )
        Spacer(modifier = Modifier.width(AppTheme.dimens.spaceSmall))
        ActionButton(
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Delete,
            contentDescription = "",
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            onClick = {
                showDeleteDialog.value = true
            }
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = message.totalTokens.toString() + " Tokens",
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        )
        Spacer(modifier = Modifier.width(AppTheme.dimens.spaceSmall))
        ActionButton(
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCallMade,
            contentDescription = "",
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            onClick = {
                onShare("")
            }
        )
        Spacer(modifier = Modifier.width(AppTheme.dimens.spaceSmall))
        ActionButton(
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.NoteAdd,
            contentDescription = "",
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            onClick = {
                onNoteAdd()
            }
        )
    }
    if (showDeleteDialog.value) {
        DeleteConfirmDialog(
            showDeleteDialogState = showDeleteDialog,
            onDelete = {
                onDelete.invoke(message.completionId)
            }
        )
    }
}
