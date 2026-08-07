package com.wanbaohe.altitude.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.TextButton
import com.shifenmiao.theme.AppTheme
import com.shifenmiao.core.R as CoreR

/**
 * 保存海拔记录的对话框
 * 可选填备注；确认后回调并携带备注内容
 */
@Composable
internal fun SaveNoteDialog(
    currentDisplay: String,
    unit: String,
    onConfirm: (note: String) -> Unit,
    onDismiss: () -> Unit
) {
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = stringResource(CoreR.string.altitude_save_record),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column {
                // 当前海拔预览
                Text(
                    text = "$currentDisplay $unit",
                    style = MaterialTheme.typography.displaySmall.copy(
                        color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                // 备注输入框
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    placeholder = {
                        Text(
                            text = stringResource(CoreR.string.altitude_note_hint),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { onConfirm(note) }),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            FilledTonalButton(
                onClick = { onConfirm(note) },
                colors = AppTheme.colors.filledTonalButtonColors(),
                modifier = Modifier.padding(end = 4.dp, bottom = 4.dp)
            ) {
                Text(text = stringResource(android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Text(text = stringResource(android.R.string.cancel))
            }
        }
    )
}

