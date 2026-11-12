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

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.wanbaohe.base.ui.icon.IconRegistry
import com.wanbaohe.a2ui.catalog.A2uiComponentRenderer
import com.wanbaohe.a2ui.catalog.A2uiRenderContext
import com.wanbaohe.a2ui.domain.model.A2uiComponent
import javax.inject.Inject
import com.t8rin.imagetoolbox.core.resources.icons.line.LineHelp

class IconRenderer @Inject constructor() : A2uiComponentRenderer {

    override val componentType = "Icon"

    @Composable
    override fun Render(
        component: A2uiComponent,
        context: A2uiRenderContext,
        children: @Composable () -> Unit,
    ) {
        val iconName = context.resolveString(component.properties["name"])
        val size = context.resolveInt(component.properties["size"])?.dp ?: 24.dp

        Icon(
            imageVector = resolveIcon(iconName),
            contentDescription = context.resolveString(component.properties["description"]),
            modifier = Modifier.size(size),
            tint = context.resolveString(component.properties["color"])
                ?.let { runCatching { Color(android.graphics.Color.parseColor(it)) }.getOrNull() }
                ?: Color.Unspecified,
        )
    }

    private fun resolveIcon(name: String?): ImageVector {
        if (name == null) return com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineHelp
        return IconRegistry.resolve(name)
            ?: IconRegistry.resolve(name.replaceFirstChar { it.uppercase() })
            ?: com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineHelp
    }
}
