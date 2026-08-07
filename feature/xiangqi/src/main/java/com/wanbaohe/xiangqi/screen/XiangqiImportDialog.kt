package com.wanbaohe.xiangqi.screen

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedTextField
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalButton
import com.wanbaohe.xiangqi.R

@Composable
fun XiangqiImportDialog(
    title: String,
    hint: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            GlassOutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(hint) },
                minLines = 4,
            )
        },
        confirmButton = {
            GlassTonalButton(onClick = { onConfirm(text) }) {
                Text(stringResource(R.string.xiangqi_confirm))
            }
        },
        dismissButton = {
            GlassTonalButton(onClick = onDismiss) {
                Text(stringResource(R.string.xiangqi_cancel))
            }
        },
    )
}
