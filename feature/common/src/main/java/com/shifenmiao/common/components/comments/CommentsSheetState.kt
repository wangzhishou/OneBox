package com.shifenmiao.common.components.comments

/**
 * 评论浮动层的 UI 状态机 —— 由调用方持有, 控制显隐与上下文.
 *
 * 用法:
 * ```
 * val state = rememberCommentsSheetState()
 * // 点击评论图标:
 * state.documentId = item.documentId ?: item.remoteId?.toString().orEmpty()
 * state.itemTitle = item.title
 * state.visible = true
 *
 * CommentsBottomSheet(state = state, uid = "api::item-list.item-list")
 *
 * // 关闭后建议重置:
 * LaunchedEffect(state.visible) { if (!state.visible) state.reset() }
 * ```
 *
 * 设计上把 [visible] / [documentId] / [itemTitle] 暴露为可变属性,
 * 方便调用方在不重组 composable 的前提下切换上下文 (同一屏多个 item 共用一个 sheet).
 */
class CommentsSheetState {
    /** 是否显示. 切换为 true 时 BottomSheet 会拉取首页. */
    var visible: Boolean = false

    /**
     * Strapi v5 documentId (cuid).
     *
     * 优先用 documentId; 旧版 v4 数据未携带 documentId 时, 调用方应降级使用 remoteId.toString().
     */
    var documentId: String = ""

    /** 顶部显示的标题 (一般是 item.title). 空字符串时显示默认 "评论". */
    var itemTitle: String = ""

    /** 清空上下文, 通常在 sheet 关闭动画结束后调用. */
    fun reset() {
        visible = false
        documentId = ""
        itemTitle = ""
    }
}

/** 工厂: remember 一个与 composition 生命周期绑定的状态机. */
@androidx.compose.runtime.Composable
fun rememberCommentsSheetState(): CommentsSheetState =
    androidx.compose.runtime.remember { CommentsSheetState() }
