package com.hrm.markdown.renderer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp

/** A bounded slot measured by the Markdown layout engine before it is composed. */
@Immutable
data class MarkdownBlockSlot<T>(
    val height: Dp,
    val content: @Composable (T) -> Unit,
) {
    init {
        require(height.value.isFinite() && height.value >= 0f) {
            "Markdown slot height must be finite and non-negative"
        }
    }
}

@Immutable
data class MarkdownCodeBlockInfo(
    val code: String,
    val language: String,
    val title: String?,
)

@Immutable
data class MarkdownTableInfo(val rowCount: Int, val columnCount: Int)

/** Optional chrome around standard blocks. It does not replace the selectable body. */
@Immutable
data class MarkdownComponents(
    val header: (@Composable () -> Unit)? = null,
    val footer: (@Composable () -> Unit)? = null,
    val codeBlockHeader: MarkdownBlockSlot<MarkdownCodeBlockInfo>? = null,
    val tableToolbar: MarkdownBlockSlot<MarkdownTableInfo>? = null,
    /** Optional image loader/painter; image click handling is supplied through interactions. */
    val image: MarkdownImageRenderer? = null,
    val admonitionIcons: Map<String, String> = mapOf("NOTE" to "ℹ️", "TIP" to "💡", "IMPORTANT" to "❗", "WARNING" to "⚠️", "CAUTION" to "🔴"),
    /**
     * 自定义选区工具栏：提供后替换平台 TextToolbar（复制/全选），选区完成时以
     * [MarkdownSelectionInfo]（文本、窗口包围盒、逐块区间）重组。宿主自行定位与
     * 消失（[MarkdownSelectionActions.dismiss] 清除选区）。
     */
    val selectionToolbar: (@Composable (MarkdownSelectionInfo, MarkdownSelectionActions) -> Unit)? = null,
    val blockSurface: MarkdownBlockSurface? = null,
    val inlineCode: MarkdownInlineCodeRenderer? = null,
) {
    companion object {
        val Default = MarkdownComponents()
    }
}

internal val LocalMarkdownComponents = compositionLocalOf { MarkdownComponents.Default }
