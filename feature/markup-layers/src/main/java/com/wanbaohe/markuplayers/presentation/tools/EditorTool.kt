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
    const val ID_AI_GENERATE = "ai_generate"
    const val ID_LAYERS = "layers"

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
        // AI 生成图片:侧栏入口,开共享 AiGenerateImageSheet(文生图;选中图片图层时图生图)
        EditorTool(
            id = ID_AI_GENERATE,
            titleRes = R.string.markup_rail_ai,
            icon = MaterialIcons.Outlined.AutoAwesome,
            placement = EditorTool.Placement.SideBar,
            mode = EditorTool.Mode.Sheet
        ),
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
        EditorTool(
            id = ID_ADJUST,
            titleRes = R.string.markup_tool_adjust,
            icon = Icons.Outlined.LineTune,
            placement = EditorTool.Placement.BottomTab,
            mode = EditorTool.Mode.Sheet
        ),
        // 「图层」Tab:右侧浮动图层面板的显隐开关(完整 LayersSheet 由面板内「展开」打开)
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
