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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wanbaohe.core.ui.widget.glass.GlassRadioButton
import com.wanbaohe.a2ui.catalog.A2uiComponentRenderer
import com.wanbaohe.a2ui.catalog.A2uiRenderContext
import com.wanbaohe.a2ui.domain.model.A2uiComponent
import com.wanbaohe.a2ui.domain.model.DynamicValue
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject

class RadioGroupRenderer @Inject constructor() : A2uiComponentRenderer {

    override val componentType = "RadioGroup"

    @Composable
    override fun Render(
        component: A2uiComponent,
        context: A2uiRenderContext,
        children: @Composable () -> Unit,
    ) {
        val valueDynamic = component.properties["value"]
        val selectedValue = context.resolveString(valueDynamic) ?: ""
        val pointerPath = (valueDynamic as? DynamicValue.Pointer)?.path
        val label = context.resolveString(component.properties["label"])
        val options = parseOptions(context.resolveDynamic(component.properties["options"]))
        val enabled = context.resolveBoolean(component.properties["enabled"]) ?: true
        val spacing = context.resolveInt(component.properties["spacing"])?.dp ?: 0.dp

        if (!label.isNullOrBlank()) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
            options.forEach { option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = enabled) {
                            pointerPath?.let {
                                context.updateDataModel(it, JsonPrimitive(option))
                            }
                        }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    GlassRadioButton(
                        selected = option == selectedValue,
                        onClick = null,
                        enabled = enabled,
                    )
                    Text(text = option)
                }
            }
        }
    }

    private fun parseOptions(json: JsonElement?): List<String> {
        if (json == null) return emptyList()
        return runCatching {
            (json as? JsonArray)?.mapNotNull { element ->
                runCatching { element.jsonPrimitive.contentOrNull }.getOrNull()
            } ?: emptyList()
        }.getOrDefault(emptyList())
    }
}
