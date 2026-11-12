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

package com.wanbaohe.a2ui.catalog.builtin.input

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wanbaohe.core.ui.widget.glass.GlassCheckbox
import com.wanbaohe.a2ui.catalog.A2uiComponentRenderer
import com.wanbaohe.a2ui.catalog.A2uiRenderContext
import com.wanbaohe.a2ui.domain.model.A2uiComponent
import com.wanbaohe.a2ui.domain.model.DynamicValue
import kotlinx.serialization.json.JsonPrimitive
import javax.inject.Inject

class CheckboxRenderer @Inject constructor() : A2uiComponentRenderer {

    override val componentType = "CheckBox"

    @Composable
    override fun Render(
        component: A2uiComponent,
        context: A2uiRenderContext,
        children: @Composable () -> Unit,
    ) {
        val checkedDynamic = component.properties["checked"]
        val checked = context.resolveBoolean(checkedDynamic) ?: false
        val pointerPath = (checkedDynamic as? DynamicValue.Pointer)?.path
        val label = context.resolveString(component.properties["label"])
        val enabled = context.resolveBoolean(component.properties["enabled"]) ?: true

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GlassCheckbox(
                checked = checked,
                onCheckedChange = { newValue ->
                    pointerPath?.let { context.updateDataModel(it, JsonPrimitive(newValue)) }
                },
                enabled = enabled,
            )
            if (!label.isNullOrBlank()) {
                Text(text = label)
            }
        }
    }
}
