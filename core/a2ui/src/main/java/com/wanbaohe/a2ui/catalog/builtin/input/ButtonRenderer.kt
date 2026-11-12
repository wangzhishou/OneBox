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

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.wanbaohe.core.ui.widget.glass.GlassButton
import com.wanbaohe.core.ui.widget.glass.GlassOutlinedButton
import com.wanbaohe.core.ui.widget.glass.GlassTextButton
import com.wanbaohe.core.ui.widget.glass.GlassTonalButton
import com.wanbaohe.a2ui.catalog.A2uiComponentRenderer
import com.wanbaohe.a2ui.catalog.A2uiRenderContext
import com.wanbaohe.a2ui.domain.model.A2uiComponent
import javax.inject.Inject

class ButtonRenderer @Inject constructor() : A2uiComponentRenderer {

    override val componentType = "Button"

    @Composable
    override fun Render(
        component: A2uiComponent,
        context: A2uiRenderContext,
        children: @Composable () -> Unit,
    ) {
        val label = context.resolveString(component.properties["label"])
            ?: context.resolveString(component.properties["text"])
        val variant = context.resolveString(component.properties["variant"]) ?: "filled"
        val enabled = context.resolveBoolean(component.properties["enabled"]) ?: true

        val onClick: () -> Unit = {
            component.action?.let { action ->
                context.dispatchAction(sourceComponentId = component.id, action = action)
            }
        }

        val content: @Composable RowScope.() -> Unit = {
            if (label != null) {
                Text(text = label)
            } else {
                children()
            }
        }

        when (variant.lowercase()) {
            "outlined" -> GlassOutlinedButton(
                onClick = onClick,
                modifier = Modifier,
                enabled = enabled,
                content = content,
            )

            "text", "ghost" -> GlassTextButton(
                onClick = onClick,
                modifier = Modifier,
                enabled = enabled,
                content = content,
            )

            "tonal" -> GlassTonalButton(
                onClick = onClick,
                modifier = Modifier,
                enabled = enabled,
                content = content,
            )

            else -> GlassButton(
                onClick = onClick,
                modifier = Modifier,
                enabled = enabled,
                content = content,
            )
        }
    }
}
