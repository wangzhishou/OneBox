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

package com.wanbaohe.a2ui.catalog.builtin.display

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.wanbaohe.core.ui.widget.glass.GlassBadge
import com.wanbaohe.a2ui.catalog.A2uiComponentRenderer
import com.wanbaohe.a2ui.catalog.A2uiRenderContext
import com.wanbaohe.a2ui.domain.model.A2uiComponent
import javax.inject.Inject

class BadgeRenderer @Inject constructor() : A2uiComponentRenderer {

    override val componentType = "Badge"

    @Composable
    override fun Render(
        component: A2uiComponent,
        context: A2uiRenderContext,
        children: @Composable () -> Unit,
    ) {
        val text = context.resolveString(component.properties["text"]) ?: ""
        val style = context.resolveString(component.properties["style"])
        val containerColor = mapBadgeColor(style)
        val contentColor = if (containerColor.luminance() > 0.5f) Color.Black else Color.White

        GlassBadge(
            containerColor = containerColor,
            contentColor = contentColor,
        ) {
            Text(text = text)
        }
    }

    @Composable
    private fun mapBadgeColor(style: String?): Color = when (style?.lowercase()) {
        "error" -> MaterialTheme.colorScheme.onErrorContainer
        "success" -> MaterialTheme.colorScheme.onPrimaryContainer
        "warning" -> MaterialTheme.colorScheme.tertiaryContainer
        "info" -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.onPrimaryContainer
    }
}
