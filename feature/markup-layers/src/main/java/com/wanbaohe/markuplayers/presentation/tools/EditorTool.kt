package com.wanbaohe.markuplayers.presentation.tools

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.AutoFixHigh
import com.t8rin.imagetoolbox.core.resources.icons.FreeDraw
import com.t8rin.imagetoolbox.core.resources.icons.SelectAll
import com.t8rin.imagetoolbox.core.resources.icons.Stacks
import com.t8rin.imagetoolbox.core.resources.icons.Star
import com.t8rin.imagetoolbox.core.resources.icons.line.LineContentCut
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCrop
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFilters
import com.t8rin.imagetoolbox.core.resources.icons.line.LineStickerEmoji
import com.t8rin.imagetoolbox.core.resources.icons.line.LineText
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTune
import com.wanbaohe.markuplayers.R
import androidx.compose.material.icons.Icons as MaterialIcons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Gradient
import androidx.compose.material.icons.outlined.Tune

/** 编辑器工具。新增工具:在这里登记一行,再在 EditorScaffold 挂对应行为 */
data class EditorTool(
    val id: String,
    @StringRes val titleRes: Int,
    val icon: ImageVector,
    val placement: Placement,
    val mode: Mode,
) {
    /** SideBar = 画布左侧竖排;BottomTab = 底部主 Tab 栏;Hidden = 不在栏位展示(由其他入口打开) */
    enum class Placement { SideBar, BottomTab, Hidden }

    /** Sheet = 底部弹面板;FullScreen = 全屏子页;Inline = 画布内模式(不跳页,由 EditorScaffold 内切换);Action = 立即动作 */
    enum class Mode { Sheet, FullScreen, Inline, Action }
}

object EditorTools {

    const val ID_SELECT = "select"
    const val ID_DRAW = "draw"
    const val ID_TEXT = "text"
    const val ID_STICKER = "sticker"
    const val ID_SHAPE = "shape"
    const val ID_BASIC = "basic"
    const val ID_CROP = "crop"
    const val ID_FILTER = "filter"
    const val ID_ADJUST = "adjust"
    const val ID_AI = "ai"
    const val ID_LAYERS = "layers"
    const val ID_MASK = "mask"
    const val ID_EFFECTS = "effects"
    const val ID_TEXT_STYLE = "text_style"

    val all: List<EditorTool> = listOf(
        EditorTool(
            id = ID_SELECT,
            titleRes = R.string.markup_tool_select,
            icon = Icons.Outlined.SelectAll,
            placement = EditorTool.Placement.SideBar,
            mode = EditorTool.Mode.Action
        ),
        EditorTool(
            id = ID_DRAW,
            titleRes = R.string.markup_tool_draw,
            icon = Icons.Rounded.FreeDraw,
            placement = EditorTool.Placement.SideBar,
            mode = EditorTool.Mode.Inline
        ),
        EditorTool(
            id = ID_TEXT,
            titleRes = R.string.markup_tool_text,
            icon = Icons.Outlined.LineText,
            placement = EditorTool.Placement.SideBar,
            mode = EditorTool.Mode.Action
        ),
        EditorTool(
            id = ID_STICKER,
            titleRes = R.string.markup_tool_sticker,
            icon = Icons.Outlined.LineStickerEmoji,
            placement = EditorTool.Placement.SideBar,
            mode = EditorTool.Mode.Sheet
        ),
        EditorTool(
            id = ID_SHAPE,
            titleRes = R.string.markup_tool_shape,
            icon = Icons.Outlined.Star,
            placement = EditorTool.Placement.SideBar,
            mode = EditorTool.Mode.Sheet
        ),
        // 裁剪旋转页:左侧工具栏入口,点击直接进全屏工具页
        EditorTool(
            id = ID_CROP,
            titleRes = R.string.markup_tool_crop,
            icon = Icons.Outlined.LineCrop,
            placement = EditorTool.Placement.SideBar,
            mode = EditorTool.Mode.FullScreen
        ),
        EditorTool(
            id = ID_MASK,
            titleRes = R.string.studio_mask,
            icon = MaterialIcons.Outlined.Gradient,
            placement = EditorTool.Placement.SideBar,
            mode = EditorTool.Mode.Sheet
        ),
        // 调色(亮度/对比度/饱和度等):左侧工具栏入口,开 AdjustToolSheet
        EditorTool(
            id = ID_ADJUST,
            titleRes = R.string.markup_tool_adjust,
            icon = Icons.Outlined.LineTune,
            placement = EditorTool.Placement.SideBar,
            mode = EditorTool.Mode.Sheet
        ),
        EditorTool(
            id = ID_EFFECTS,
            titleRes = R.string.studio_effects,
            icon = MaterialIcons.Outlined.Tune,
            placement = EditorTool.Placement.Hidden,
            mode = EditorTool.Mode.Sheet
        ),
        // AI 生成图片不在侧栏:入口并入底部「AI 处理」面板(AiToolSheet 顶部生成卡)
        // 「基础工具」Tab:左侧工具栏的显隐开关(侧栏承载选择/画笔/文字/贴纸/形状/裁剪)
        EditorTool(
            id = ID_BASIC,
            titleRes = R.string.markup_tool_basic,
            icon = Icons.Outlined.LineContentCut,
            placement = EditorTool.Placement.BottomTab,
            mode = EditorTool.Mode.Action
        ),
        EditorTool(
            id = ID_AI,
            titleRes = R.string.markup_tool_ai,
            icon = Icons.Outlined.AutoFixHigh,
            placement = EditorTool.Placement.BottomTab,
            mode = EditorTool.Mode.Sheet
        ),
        EditorTool(
            id = ID_FILTER,
            titleRes = R.string.markup_tool_filter,
            icon = Icons.Outlined.LineFilters,
            placement = EditorTool.Placement.BottomTab,
            mode = EditorTool.Mode.Sheet
        ),
        // 「文字设置」Tab(对齐图文卡片):只作用于当前选中的文字图层;
        // 未选中文字时提示先选中,不再兜底改别的图层或新建
        EditorTool(
            id = ID_TEXT_STYLE,
            titleRes = R.string.markup_tool_text_style,
            icon = MaterialIcons.Outlined.Tune,
            placement = EditorTool.Placement.BottomTab,
            mode = EditorTool.Mode.Sheet
        ),
        // Phones open LayersSheet directly; wide layouts retain the compact floating panel.
        EditorTool(
            id = ID_LAYERS,
            titleRes = R.string.markup_tool_layers,
            icon = Icons.Outlined.Stacks,
            placement = EditorTool.Placement.BottomTab,
            mode = EditorTool.Mode.Action
        ),
    )

    val sideBar: List<EditorTool> get() = all.filter { it.placement == EditorTool.Placement.SideBar }

    val bottomTab: List<EditorTool> get() = all.filter { it.placement == EditorTool.Placement.BottomTab }

    fun byId(id: String): EditorTool? = all.firstOrNull { it.id == id }
}
