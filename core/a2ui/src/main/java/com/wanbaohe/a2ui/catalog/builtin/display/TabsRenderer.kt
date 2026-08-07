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
