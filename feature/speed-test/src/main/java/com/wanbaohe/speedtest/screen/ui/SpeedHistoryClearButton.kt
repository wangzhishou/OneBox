package com.wanbaohe.speedtest.screen.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.theme.AppTheme
import com.wanbaohe.base.ui.DeleteConfirmDialog
import com.wanbaohe.speedtest.R
import com.shifenmiao.core.R as CoreR

@Composable
internal fun SpeedHistoryClearButton(onClear: () -> Unit) {
    val showDeleteDialog = remember { mutableStateOf(false) }

    TextButton(
        onClick = { showDeleteDialog.value = true },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        colors = AppTheme.colors.buttonColors()
    ) {
        Text(
            text = stringResource(R.string.speed_test_clear_records),
            fontSize = 15.sp,
            color = AppTheme.colors.getPrimaryColor()
        )
    }

    DeleteConfirmDialog(
        onDelete = onClear,
        showDeleteDialogState = showDeleteDialog,
        message = stringResource(
            CoreR.string.delete_item_confirm_message,
            stringResource(R.string.speed_test_history_title)
        )
    )
}
