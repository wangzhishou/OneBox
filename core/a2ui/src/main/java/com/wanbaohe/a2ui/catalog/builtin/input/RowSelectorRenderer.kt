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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wanbaohe.a2ui.catalog.A2uiComponentRenderer
import com.wanbaohe.a2ui.catalog.A2uiRenderContext
import com.wanbaohe.a2ui.domain.model.A2uiComponent
import javax.inject.Inject

class RowSelectorRenderer @Inject constructor() : A2uiComponentRenderer {

    override val componentType = "RowSelector"

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    override fun Render(
        component: A2uiComponent,
        context: A2uiRenderContext,
        children: @Composable () -> Unit,
    ) {
        val options by remember(component.properties["options"], component.properties["data"]) {
            derivedStateOf {
                parseSelectorOptions(
                    context.resolveDynamic(component.properties["options"] ?: component.properties["data"]),
                )
            }
        }
        if (options.isEmpty()) return

        val state = rememberSelectorState(
            component = component,
            context = context,
            options = options,
            defaultMaxSelected = 0,
        )

        val label = context.resolveString(component.properties["label"])
        val enabled = context.resolveBoolean(component.properties["enabled"]) ?: true
        val spacing = context.resolveInt(component.properties["spacing"])?.dp ?: 8.dp
        val padding = context.resolveInt(component.properties["padding"])?.dp ?: 0.dp

        if (!label.isNullOrBlank()) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }

        FlowRow(
            modifier = Modifier
                .padding(padding),
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalArrangement = Arrangement.spacedBy(spacing),
        ) {
            options.forEach { option ->
                A2uiSelectorChip(
                    selected = option.value in state.selectedValues,
                    onClick = { state.toggle(option) },
                    label = option.label,
                    enabled = enabled,
                )
            }
        }

        if (state.showCustomInput) {
            children()
        }
    }
}
