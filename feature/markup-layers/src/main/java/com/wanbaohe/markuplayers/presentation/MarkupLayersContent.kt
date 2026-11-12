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

package com.wanbaohe.markuplayers.presentation

import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.wanbaohe.core.ui.widget.dialogs.LoadingDialog
import com.wanbaohe.markuplayers.presentation.editor.EditorScaffold
import com.wanbaohe.markuplayers.presentation.home.MarkupHomeContent
import com.wanbaohe.markuplayers.presentation.screenLogic.MarkupLayersComponent

@Composable
fun MarkupLayersContent(
    component: MarkupLayersComponent
) {
    if (component.hasImage) {
        EditorScaffold(component = component)
    } else {
        MarkupHomeContent(component = component)
    }

    LoadingDialog(
        visible = component.isSaving || component.isImageLoading || component.isAiProcessing,
        onCancelLoading = component::cancelAllImageTasks,
        canCancel = component.isSaving || component.isImageLoading || component.isAiProcessing,
        content = {
            component.taskLabel?.let { label ->
                Text(text = label, style = MaterialTheme.typography.bodyMedium)
            }
        }
    )
}
