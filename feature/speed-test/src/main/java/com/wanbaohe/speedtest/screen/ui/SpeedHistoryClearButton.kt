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
