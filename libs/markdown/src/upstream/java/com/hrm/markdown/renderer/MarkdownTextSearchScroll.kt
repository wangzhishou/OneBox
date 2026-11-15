package com.hrm.markdown.renderer

import androidx.compose.foundation.gestures.BringIntoViewSpec
import androidx.compose.foundation.gestures.LocalBringIntoViewSpec
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.coroutines.Job
import kotlin.math.abs

/**
 * 查找时将完全屏外的匹配行居中，可见匹配不动，部分可见匹配靠最近边缘。
 * 内部滚动自动使用；外部滚动宿主在创建滚动容器前提供此策略及实际遮挡像素。
 * 非查找请求原样交给 [fallback]，不会改变选区拖动或焦点滚动。
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun rememberMarkdownTextSearchBringIntoViewSpec(
    state: MarkdownTextSearchState,
    obscuredTopPx: Float = 0f,
    obscuredBottomPx: Float = 0f,
    fallback: BringIntoViewSpec = LocalBringIntoViewSpec.current,
): BringIntoViewSpec {
    val topInset by rememberUpdatedState(obscuredTopPx)
    val bottomInset by rememberUpdatedState(obscuredBottomPx)
    val fallbackSpec by rememberUpdatedState(fallback)
    return remember(state) {
        object : BringIntoViewSpec {
            private var request: MarkdownTextSearchRelocation? = null
            private var centerMatch = false

            override fun calculateScrollDistance(offset: Float, size: Float, containerSize: Float): Float {
                val current = state.relocation
                if (current == null || !current.job.isActive || state.selectionInProgress) {
                    if (state.selectionInProgress) current?.job?.cancel()
                    request = null
                    return fallbackSpec.calculateScrollDistance(offset, size, containerSize)
                }
                val bottom = (containerSize - bottomInset).coerceIn(0f, containerSize)
                val top = topInset.coerceIn(0f, bottom)
                val end = offset + size
                if (request !== current) {
                    request = current
                    // 每帧重算距离不能在匹配刚露出时撤销居中；lazy 粗定位前已记录屏外状态。
                    centerMatch = current.centerAfterMaterialization || end <= top || offset >= bottom
                }
                return when {
                    centerMatch -> offset + size / 2f - (top + bottom) / 2f
                    offset >= top && end <= bottom -> 0f
                    offset < top && end > bottom -> 0f
                    abs(offset - top) < abs(end - bottom) -> offset - top
                    else -> end - bottom
                }
            }
        }
    }
}

internal class MarkdownTextSearchRelocation(
    val job: Job,
    val centerAfterMaterialization: Boolean = false,
)
