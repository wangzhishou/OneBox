package com.shifenmiao.common.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.ui.res.stringResource
import com.shifenmiao.common.logic.CommonComponent
import com.shifenmiao.core.R
import com.shifenmiao.model.common.ProgressType

@Composable
fun ImportProgressDialog(
    commonComponent: CommonComponent
) {
    val progressState = commonComponent.importOrExportProgress.collectAsState().value
    if (progressState.visible) {
        AlertDialog(
            onDismissRequest = {},
            title = {
                if(progressState.type == ProgressType.EXPORT) {
                    Text(text = stringResource(R.string.exporting))
                } else {
                    Text(text = stringResource(R.string.importing))
                }
            },
            text = {
                Column {
                    LinearProgressIndicator(
                        progress = { progressState.progress },
                    )
                    Text("${(progressState.progress * 100).toInt()}% - ${progressState.message}")
                }
            },
            confirmButton = {}
        )
    }
}