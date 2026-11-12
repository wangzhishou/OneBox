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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wanbaohe.a2ui.catalog.A2uiComponentRenderer
import com.wanbaohe.a2ui.catalog.A2uiRenderContext
import com.wanbaohe.a2ui.domain.model.A2uiComponent
import javax.inject.Inject

class TabsRenderer @Inject constructor() : A2uiComponentRenderer {

    override val componentType = "Tabs"

    @Composable
    override fun Render(
        component: A2uiComponent,
        context: A2uiRenderContext,
        children: @Composable () -> Unit,
    ) {
        val labels = component.childIds.mapNotNull { childId ->
            val child = context.surfaceState.components[childId] ?: return@mapNotNull null
            when (child.type) {
                "Tab" -> context.resolveString(child.properties["title"])
                    ?: context.resolveString(child.properties["label"])

                "Text" -> context.resolveString(child.properties["text"])

                else -> context.resolveString(child.properties["title"])
                    ?: context.resolveString(child.properties["label"])
                    ?: context.resolveString(child.properties["text"])
            }
        }

        if (labels.isEmpty()) {
            children()
            return
        }

        var selectedTab by remember { mutableIntStateOf(0) }

        Column(modifier = Modifier.fillMaxWidth()) {
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth(),
            ) {
                labels.forEachIndexed { index, label ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { androidx.compose.material3.Text(label) },
                    )
                }
            }

            val activeChildId = component.childIds.getOrNull(selectedTab)
            if (activeChildId != null) {
                Column(modifier = Modifier.padding(16.dp)) {
                    context.renderChild(activeChildId)
                }
            }
        }
    }
}
