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
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.shifenmiao.model.ui.picker.SelectedCountryData
import com.wanbaohe.core.ui.widget.glass.GlassOutlinedTextField
import com.wanbaohe.a2ui.catalog.A2uiComponentRenderer
import com.wanbaohe.a2ui.catalog.A2uiRenderContext
import com.wanbaohe.a2ui.domain.model.A2uiComponent
import com.wanbaohe.a2ui.domain.model.DynamicValue
import kotlinx.serialization.json.JsonPrimitive
import javax.inject.Inject
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLocationOn

class LocationPickerRenderer @Inject constructor() : A2uiComponentRenderer {

    override val componentType = "LocationPicker"

    @Composable
    override fun Render(
        component: A2uiComponent,
        context: A2uiRenderContext,
        children: @Composable () -> Unit,
    ) {
        val valueDynamic = component.properties["value"]
        val currentValue = context.resolveString(valueDynamic) ?: ""
        val pointerPath = (valueDynamic as? DynamicValue.Pointer)?.path
        val provincePath = (component.properties["provincePath"] as? DynamicValue.Pointer)?.path
        val cityPath = (component.properties["cityPath"] as? DynamicValue.Pointer)?.path
        val districtPath = (component.properties["districtPath"] as? DynamicValue.Pointer)?.path
        val label = context.resolveString(component.properties["label"]) ?: ""
        val enabled = context.resolveBoolean(component.properties["enabled"]) ?: true
        val separator = context.resolveString(component.properties["separator"]) ?: " "
        val layer = context.resolveInt(component.properties["layer"])?.coerceIn(1, 3) ?: 3

        val locationPicker = rememberPlatformLocationPickerState()

        fun updateValue(value: String) {
            pointerPath?.let {
                context.updateDataModel(it, JsonPrimitive(value))
            }
            val parsed = parseLocation(value, separator)
            provincePath?.let { parsed?.province?.let { v -> context.updateDataModel(it, JsonPrimitive(v)) } }
            cityPath?.let { parsed?.city?.let { v -> context.updateDataModel(it, JsonPrimitive(v)) } }
            districtPath?.let { parsed?.district?.let { v -> context.updateDataModel(it, JsonPrimitive(v)) } }
        }

        GlassOutlinedTextField(
            value = currentValue,
            onValueChange = { newValue ->
                if (enabled) {
                    updateValue(newValue)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            label = label.takeIf { it.isNotBlank() }?.let { { Text(it) } },
            trailingIcon = {
                IconButton(onClick = {
                    if (enabled) {
                        locationPicker.show(
                            title = label.takeIf { it.isNotBlank() },
                            initData = parseLocation(currentValue, separator),
                            initLayer = layer,
                            onCancel = { locationPicker.hide() },
                            onChange = { selected ->
                                locationPicker.hide()
                                val joined = listOfNotNull(
                                    selected.province,
                                    selected.city,
                                    selected.district,
                                ).filter { it.isNotBlank() }.joinToString(separator)
                                updateValue(joined)
                            },
                        )
                    }
                }) {
                    Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineLocationOn, contentDescription = null)
                }
            },
        )
    }

    private fun parseLocation(value: String, separator: String): SelectedCountryData? {
        if (value.isBlank()) return null
        val parts = value.split(separator)
        return SelectedCountryData(
            province = parts.getOrNull(0)?.takeIf { it.isNotBlank() },
            city = parts.getOrNull(1)?.takeIf { it.isNotBlank() },
            district = parts.getOrNull(2)?.takeIf { it.isNotBlank() },
        )
    }
}
