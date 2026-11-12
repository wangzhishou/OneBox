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

package com.wanbaohe.base.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.DialogProperties
import com.wanbaohe.base.ui.button.CancelButton
import com.wanbaohe.base.ui.button.ConfirmButton
import com.shifenmiao.core.R
import com.shifenmiao.theme.AppTheme
import com.wanbaohe.core.ui.widget.enhanced.EnhancedAlertDialog
import com.t8rin.imagetoolbox.core.resources.icons.Delete

@Composable
fun DeleteConfirmDialog(
    onDelete: () -> Unit,
    showDeleteDialogState: MutableState<Boolean>,
    message: String = stringResource(R.string.ai_chat_delete_message)
) {
    ConfirmDialog(
        title = stringResource(R.string.ai_chat_delete_title),
        message = message,
        confirmButtonText = stringResource(R.string.button_confirm),
        dismissButtonText = stringResource(R.string.button_cancel),
        onConfirm = {
            onDelete.invoke()
        },
        onDismiss = {
            showDeleteDialogState.value = false
        },
        showDialog = showDeleteDialogState,
        icon = {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Delete,
                contentDescription = null
            )
        }
    )

}

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmButtonText: String,
    dismissButtonText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    showDialog: MutableState<Boolean>,
    properties: DialogProperties = DialogProperties(),
    icon: @Composable (() -> Unit)? = null,
) {
    ConfirmContentDialog(
        title = title,
        confirmButtonText = confirmButtonText,
        dismissButtonText = dismissButtonText,
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        icon = icon,
        showDialog = showDialog,
        properties = properties
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = message,
            textAlign = TextAlign.Start
        )
    }
}

@Composable
fun ConfirmContentDialog(
    title: String,
    confirmButtonText: String,
    dismissButtonText: String,
    icon: @Composable (() -> Unit)? = null,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    showDialog: Boolean,
    properties: DialogProperties = DialogProperties(),
    onDismissRequest: () -> Unit = {
        onDismiss()
    },
    content: @Composable () -> Unit
) {
    val showDialogState = remember { mutableStateOf(showDialog) }

    ConfirmContentDialog(
        title = title,
        confirmButtonText = confirmButtonText,
        dismissButtonText = dismissButtonText,
        icon = icon,
        onConfirm = {
            onConfirm()
            showDialogState.value = false
        },
        onDismiss = {
            onDismiss()
            showDialogState.value = false
        },
        showDialog = showDialogState,
        properties = properties,
        onDismissRequest = {
            onDismissRequest()
            showDialogState.value = false
        },
        content = content
    )
}

@Composable
fun ConfirmContentDialog(
    title: String,
    confirmButtonText: String,
    dismissButtonText: String,
    icon: @Composable (() -> Unit)? = null,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    showDialog: MutableState<Boolean>,
    properties: DialogProperties = DialogProperties(),
    onDismissRequest: () -> Unit = {
        showDialog.value = false
        onDismiss()
    },
    content: @Composable () -> Unit
) {
    EnhancedAlertDialog(
        visible = showDialog.value,
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = title)
        },
        text = {
            content()
        },
        confirmButton = {
            ConfirmButton(
                text = confirmButtonText,
                onClick = {
                    onConfirm()
                    showDialog.value = false
                }
            )
        },
        dismissButton = {
            CancelButton(
                text = dismissButtonText,
                onClick = {
                    showDialog.value = false
                    onDismiss()
                }
            )
        },
        icon = icon,
        containerColor = AppTheme.colors.getContainerSurfaceColor(),
        properties = properties
    )
}

@Composable
fun OptionConfirmDialog(
    title: String,
    message: String,
    primaryButtonText: String,
    secondaryButtonText: String,
    cancelButtonText: String,
    onPrimary: () -> Unit,
    onSecondary: () -> Unit,
    onCancel: () -> Unit,
    showDialog: MutableState<Boolean>,
    icon: @Composable (() -> Unit)? = null,
) {
    EnhancedAlertDialog(
        visible = showDialog.value,
        onDismissRequest = {
            showDialog.value = false
            onCancel()
        },
        title = {
            Text(
                modifier = Modifier
                    .fillMaxWidth(),
                text = title
            )
        },
        text = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = message,
                textAlign = TextAlign.Start
            )
        },
        confirmButton = {
            CancelButton(
                text = primaryButtonText,
                onClick = {
                    showDialog.value = false
                    onPrimary()
                }
            )
            CancelButton(
                text = secondaryButtonText,
                onClick = {
                    showDialog.value = false
                    onSecondary()
                }
            )
            ConfirmButton(
                text = cancelButtonText,
                onClick = {
                    showDialog.value = false
                    onCancel()
                }
            )
        },
        icon = icon,
    )
}
