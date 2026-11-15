package com.hrm.markdown.renderer.internal.selection

import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import com.hrm.markdown.renderer.LocalMarkdownStyle
import com.hrm.markdown.renderer.LocalMarkdownTextSearchState
import com.hrm.markdown.renderer.internal.layout.model.InternalLayoutBlockModel

/**
 * 把一个可选 inline block 接入自研选区层：
 * - [onGloballyPositioned] 把当前 block 的 [LayoutCoordinates] 注册到坐标表（支持 window↔local 换算）；
 * - [DisposableEffect] 在 block 滚出屏 / 离开组合时反注册（支持 LazyColumn 虚拟化）；
 * - [drawWithContent] 按控制器给出的 block-local 高亮矩形绘制选中背景。
 */
internal fun Modifier.selectableBlock(
    block: InternalLayoutBlockModel,
    controller: MarkdownSelectionController?,
    selectionHighlight: Boolean = true,
): Modifier {
    if (controller == null) return this
    return this.composed {
        val stableId = block.identity.stableId
        val highlightColor = LocalMarkdownStyle.current.colors.link.copy(alpha = 0.3f)
        val search = LocalMarkdownTextSearchState.current
        val requester = if (search == null) null else remember(controller, stableId) { BringIntoViewRequester() }
        // The Compose node outlives layout-model revisions. Disposing on each new model
        // loses coordinates when its size/position are unchanged (no positioning callback).
        SideEffect { controller.registerVisibleBlock(block) }
        DisposableEffect(stableId, controller) {
            onDispose {
                controller.unregisterVisibleBlock(stableId)
                controller.registry.unregister(stableId)
            }
        }
        this
            .then(if (requester == null) Modifier else Modifier.bringIntoViewRequester(requester))
            .onGloballyPositioned { controller.registry.register(stableId, it, requester) }
            .drawWithContent {
                // 搜索只绘制背景，不写入 controller.state.range，因此不会触发选区菜单/手柄。
                if (search != null) {
                    val active = search.activeMatch.takeIf { search.highlightActiveMatch }
                    for (match in search.matchesByGeometry[stableId].orEmpty()) {
                        if (match == active) continue
                        for (box in controller.highlightBoxesFor(stableId, match.logicalRange, textOnly = true)) {
                            drawRect(search.allMatchHighlightColor, box.topLeft, box.size)
                        }
                    }
                    if (active?.geometryStableId == stableId) {
                        for (box in controller.highlightBoxesFor(stableId, active.logicalRange, textOnly = true)) {
                            drawRect(search.highlightColor, box.topLeft, box.size)
                        }
                    }
                }
                drawContent()
                val highlightBoxes = if (!selectionHighlight || controller.state.range == null) {
                    emptyList()
                } else {
                    controller.highlightBoxesFor(stableId)
                }
                for (box in highlightBoxes) {
                    drawRect(
                        color = highlightColor,
                        topLeft = Offset(box.left, box.top),
                        size = Size(box.width, box.height),
                    )
                }
            }
    }
}
