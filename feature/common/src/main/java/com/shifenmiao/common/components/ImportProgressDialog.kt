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

package com.shifenmiao.common.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.ui.res.stringResource
import com.shifenmiao.common.logic.CommonComponent
import com.shifenmiao.core.R
import com.shifenmiao.model.common.ProgressType
import com.wanbaohe.core.ui.widget.enhanced.EnhancedAlertDialog

@Composable
fun ImportProgressDialog(
    commonComponent: CommonComponent
) {
    val progressState = commonComponent.importOrExportProgress.collectAsState().value
    if (progressState.visible) {
        EnhancedAlertDialog(
            visible = true,
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