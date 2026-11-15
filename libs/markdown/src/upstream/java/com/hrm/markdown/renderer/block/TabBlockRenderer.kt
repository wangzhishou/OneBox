package com.hrm.markdown.renderer.block

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hrm.markdown.parser.ast.TabBlock
import com.hrm.markdown.parser.ast.TabItem
import com.hrm.markdown.renderer.LocalMarkdownStyle
import com.hrm.markdown.renderer.MarkdownBlockChildren
import com.hrm.markdown.renderer.internal.core.model.InternalRenderBlockModel
import com.hrm.markdown.renderer.internal.core.model.TabBlockModel
import com.hrm.markdown.renderer.internal.layout.model.InternalLayoutBlockModel
import com.hrm.markdown.renderer.internal.layout.model.LayoutTabBlockModel
import com.hrm.markdown.renderer.internal.layout.LayoutTokens

/**
 * 内容标签页渲染器（MkDocs Material 风格）。
 */
@Composable
internal fun TabBlockRenderer(
    node: TabBlock,
    modifier: Modifier = Modifier,
) {
    val tabItems = remember(node) { node.children.filterIsInstance<TabItem>() }
    if (tabItems.isEmpty()) return
    RenderTabContainer(
        tabs = tabItems.map { TabHeader(it.stableKey.toLong(), it.title) },
        modifier = modifier,
    ) { selectedIndex ->
        val selectedTab = tabItems.getOrNull(selectedIndex)
        if (selectedTab != null) {
            MarkdownBlockChildren(parent = selectedTab)
        }
    }
}

@Composable
internal fun RenderTabBlockModel(
    model: TabBlockModel,
    renderChildren: @Composable (List<InternalRenderBlockModel>) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (model.items.isEmpty()) return
    RenderTabContainer(
        tabs = model.items.map { TabHeader(it.identity.stableId, it.title) },
        modifier = modifier,
    ) { selectedIndex ->
        model.items.getOrNull(selectedIndex)?.let { tab ->
            renderChildren(tab.children)
        }
    }
}

@Composable
internal fun RenderTabLayoutBlockModel(
    model: LayoutTabBlockModel,
    renderChildren: @Composable (List<InternalLayoutBlockModel>) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (model.tabs.isEmpty()) return
    RenderTabContainer(
        tabs = model.tabs.map { TabHeader(it.identity.stableId, it.title) },
        modifier = modifier,
    ) { selectedIndex ->
        model.tabs.getOrNull(selectedIndex)?.let { tab ->
            renderChildren(tab.children)
        }
    }
}

@Composable
private fun RenderTabContainer(
    tabs: List<TabHeader>,
    modifier: Modifier = Modifier,
    content: @Composable (selectedIndex: Int) -> Unit,
) {
    val theme = LocalMarkdownStyle.current
    var selectedId by remember { mutableLongStateOf(tabs.first().stableId) }
    val selectedIndex = tabs.indexOfFirst { it.stableId == selectedId }.takeIf { it >= 0 } ?: 0
    if (tabs.none { it.stableId == selectedId }) {
        SideEffect { selectedId = tabs.first().stableId }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(theme.colors.containerBackground),
    ) {
        // 标签栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(theme.colors.containerHeaderBackground)
                .padding(horizontal = 4.dp),
        ) {
            tabs.forEachIndexed { index, tab ->
                val isSelected = index == selectedIndex
                Box(
                    modifier = Modifier
                        .clickable { selectedId = tab.stableId }
                        .padding(
                            horizontal = LayoutTokens.TabTitleHorizontalPadding,
                            vertical = LayoutTokens.TabTitleVerticalPadding,
                        ),
                ) {
                    Text(
                        text = tab.title,
                        style = theme.bodyStyle.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) theme.colors.link else Color.Gray,
                        ),
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(LayoutTokens.TabContentPadding),
        ) {
            content(selectedIndex)
        }
    }
}

private data class TabHeader(
    val stableId: Long,
    val title: String,
)
