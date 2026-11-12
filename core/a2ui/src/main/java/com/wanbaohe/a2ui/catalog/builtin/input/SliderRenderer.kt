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

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.wanbaohe.core.ui.widget.glass.GlassCustomSlider
import com.wanbaohe.a2ui.catalog.A2uiComponentRenderer
import com.wanbaohe.a2ui.catalog.A2uiRenderContext
import com.wanbaohe.a2ui.domain.model.A2uiComponent
import com.wanbaohe.a2ui.domain.model.DynamicValue
import kotlinx.serialization.json.JsonPrimitive
import javax.inject.Inject

class SliderRenderer @Inject constructor() : A2uiComponentRenderer {

    override val componentType = "Slider"

    @Composable
    override fun Render(
        component: A2uiComponent,
        context: A2uiRenderContext,
        children: @Composable () -> Unit,
    ) {
        val valueDynamic = component.properties["value"]
        val value = context.resolveFloat(valueDynamic) ?: 0f
        val pointerPath = (valueDynamic as? DynamicValue.Pointer)?.path
        val min = context.resolveFloat(component.properties["min"]) ?: 0f
        val max = context.resolveFloat(component.properties["max"]) ?: 100f
        val steps = context.resolveInt(component.properties["steps"]) ?: 0
        val enabled = context.resolveBoolean(component.properties["enabled"]) ?: true
        val label = context.resolveString(component.properties["label"])

        if (!label.isNullOrBlank()) {
            Text(text = "$label: ${value.toInt()}")
        }

        GlassCustomSlider(
            value = value,
            onValueChange = { newValue ->
                pointerPath?.let {
                    val primitive = if (newValue % 1f == 0f) {
                        JsonPrimitive(newValue.toInt())
                    } else {
                        JsonPrimitive(newValue)
                    }
                    context.updateDataModel(it, primitive)
                }
            },
            modifier = Modifier,
            enabled = enabled,
            valueRange = min..max,
            steps = steps,
        )
    }
}
