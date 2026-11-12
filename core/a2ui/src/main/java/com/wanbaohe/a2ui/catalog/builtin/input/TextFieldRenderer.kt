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

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.wanbaohe.core.ui.widget.glass.GlassOutlinedTextField
import com.wanbaohe.a2ui.catalog.A2uiComponentRenderer
import com.wanbaohe.a2ui.catalog.A2uiRenderContext
import com.wanbaohe.a2ui.domain.model.A2uiComponent
import com.wanbaohe.a2ui.domain.model.DynamicValue
import kotlinx.serialization.json.JsonPrimitive
import javax.inject.Inject

class TextFieldRenderer @Inject constructor() : A2uiComponentRenderer {

    override val componentType = "TextField"

    @Composable
    override fun Render(
        component: A2uiComponent,
        context: A2uiRenderContext,
        children: @Composable () -> Unit,
    ) {
        val valueDynamic = component.properties["value"]
        val currentValue = context.resolveString(valueDynamic) ?: ""
        val pointerPath = (valueDynamic as? DynamicValue.Pointer)?.path
        val label = context.resolveString(component.properties["label"])
        val placeholder = context.resolveString(component.properties["placeholder"])
        val enabled = context.resolveBoolean(component.properties["enabled"]) ?: true
        val isError = context.resolveBoolean(component.properties["error"]) ?: false
        val supportingText = context.resolveString(component.properties["supportingText"])
        val singleLine = context.resolveBoolean(component.properties["singleLine"]) ?: false
        val maxLines = context.resolveInt(component.properties["maxLines"])
        val minLines = context.resolveInt(component.properties["minLines"])

        GlassOutlinedTextField(
            value = currentValue,
            onValueChange = { newValue ->
                pointerPath?.let { context.updateDataModel(it, JsonPrimitive(newValue)) }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            label = label.takeIf { !it.isNullOrBlank() }?.let { { Text(it) } },
            placeholder = placeholder?.let { { Text(it) } },
            isError = isError,
            supportingText = supportingText?.let { { Text(it) } },
            singleLine = singleLine,
            maxLines = maxLines ?: if (singleLine) 1 else Int.MAX_VALUE,
            minLines = minLines ?: 1,
        )
    }
}
