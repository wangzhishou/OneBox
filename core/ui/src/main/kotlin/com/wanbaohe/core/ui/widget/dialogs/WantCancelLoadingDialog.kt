/*
 * OneBox is a free AI agent and toolbox for Android
 * This file is part of OneBox, a derivative work based on
 * ImageToolbox (https://github.com/T8RIN/ImageToolbox) by T8RIN.
 *
 * Copyright (c) 2024 T8RIN (Malik Mukhametzyanov)
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * This file has been modified from the original ImageToolbox source.
 *
 * Origin: ImageToolbox derivative, modified
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

package com.wanbaohe.core.ui.widget.dialogs

import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.R
import com.wanbaohe.core.ui.widget.enhanced.EnhancedAlertDialog
import com.wanbaohe.core.ui.widget.enhanced.EnhancedCircularProgressIndicator
import com.wanbaohe.core.ui.widget.system.OneSecondaryButton

@Composable
fun WantCancelLoadingDialog(
    visible: Boolean,
    onCancelLoading: () -> Unit,
    onDismissDialog: () -> Unit,
    modifier: Modifier = Modifier,
    isForSaving: Boolean = true,
) {
    EnhancedAlertDialog(
        visible = visible,
        onDismissRequest = onDismissDialog,
        confirmButton = {
            OneSecondaryButton(
                text = stringResource(id = R.string.wait),
                onClick = onDismissDialog
            )
        },
        title = {
            Text(stringResource(id = R.string.loading))
        },
        text = {
            Text(
                text = stringResource(
                    if (isForSaving) R.string.saving_almost_complete
                    else R.string.operation_almost_complete
                )
            )
        },
        dismissButton = {
            OneSecondaryButton(
                text = stringResource(id = R.string.cancel),
                onClick = onCancelLoading
            )
        },
        icon = {
            EnhancedCircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(0.2f),
                strokeWidth = 3.dp
            )
        },
        modifier = modifier
    )
}