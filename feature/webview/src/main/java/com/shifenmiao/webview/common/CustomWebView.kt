package com.shifenmiao.webview.common

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.webkit.WebView

class CustomWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr) {

    // 拖动手势回调
    var onScrollEdgeReached: ((Boolean) -> Unit)? = null
    var onDragGesture: ((Float) -> Unit)? = null
    
    // 控制是否启用自定义触摸事件处理
    var enableCustomTouch: Boolean = false

    // 控制是否启用文本选择（长按选中），默认禁止
    var enableTextSelection: Boolean = false
        set(value) {
            field = value
            updateTextSelectionState()
        }

    // 拖动状态变量
    private var startY = 0f
    private var isAtTop = false
    private var isDragging = false
    private var lastY = 0f
    private var velocityTracker: VelocityTracker? = null
    private val dragThreshold = 8f
    private val recentYPositions = ArrayDeque<Float>(5)

    init {
        setupWebViewDefaults()
    }

    /**
     * 设置 WebView 的通用配置
     */
    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebViewDefaults() {
        // 使用统一的完整配置
        WebViewSettings.applyCommonSettings(this, context)
        // 滚动条样式
        scrollBarStyle = SCROLLBARS_INSIDE_OVERLAY
        // 设置焦点属性
        isFocusable = true
        isFocusableInTouchMode = true

        // 根据初始值设置文本选择状态
        updateTextSelectionState()
    }

    /**
     * 更新文本选择状态
     */
    private fun updateTextSelectionState() {
        if (enableTextSelection) {
            // 允许文本选择
            setOnLongClickListener(null)
            isLongClickable = true
        } else {
            // 禁用长按菜单
            setOnLongClickListener {
                // 返回true表示事件已处理，不会显示默认菜单
                true
            }
            // 禁用文本选择
            isLongClickable = false
            // 禁用ActionMode菜单（文本选择菜单）
            setOnCreateContextMenuListener(null)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return super.onTouchEvent(event)

        // 如果未启用自定义触摸事件处理，直接使用默认实现
        if (!enableCustomTouch) {
            return super.onTouchEvent(event)
        }

        // 初始化速度追踪器
        if (velocityTracker == null && (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE)) {
            velocityTracker = VelocityTracker.obtain()
        }
        velocityTracker?.addMovement(event)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startY = event.y
                lastY = startY
                // 清除之前的位置记录
                recentYPositions.clear()
                recentYPositions.add(startY)

                // 检查是否在顶部，增加容差范围避免边界判断抖动
                isAtTop = scrollY <= 5
                onScrollEdgeReached?.invoke(isAtTop) // 更新边缘状态
                isDragging = false // 重置拖动状态
                return super.onTouchEvent(event) // 继续传递事件以保持WebView原生滚动
            }

            MotionEvent.ACTION_MOVE -> {
                val currentY = event.y
                val deltaY = currentY - lastY

                // 添加到位置历史
                if (recentYPositions.size >= 5) {
                    recentYPositions.removeFirst()
                }
                recentYPositions.add(currentY)

                // 检查WebView是否已滚动到顶部
                val currentIsAtTop = scrollY <= 1
                if (currentIsAtTop != isAtTop) {
                    isAtTop = currentIsAtTop
                    onScrollEdgeReached?.invoke(isAtTop)
                }

                // 只有当WebView在顶部且向下拖动时才触发
                if (isAtTop && deltaY > 0) {
                    if (!isDragging) {
                        // 确保必须超过阈值才开始拖动，避免轻微触碰就触发
                        if (currentY - startY > dragThreshold) {
                            isDragging = true
                        }
                    }

                    if (isDragging) {
                        // 使用平滑计算的拖动距离，减少抖动
                        val totalDrag = currentY - startY
                        // 应用阻尼效果使拖动感觉更自然
                        val dampedDrag = if (totalDrag > 300) {
                            300 + (totalDrag - 300) * 0.5f
                        } else {
                            totalDrag
                        }
                        onDragGesture?.invoke(dampedDrag)
                        lastY = currentY
                        return true // 消耗事件
                    }
                }

                lastY = currentY
                return super.onTouchEvent(event) // 不消耗事件，允许WebView正常滚动
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                // 计算更准确的速度
                velocityTracker?.computeCurrentVelocity(1000) // 1000表示以像素/秒为单位
                val yVelocity = velocityTracker?.yVelocity ?: 0f

                if (isDragging) {
                    // 计算平均移动速度作为补充判断
                    var avgVelocity = 0f
                    if (recentYPositions.size >= 3) {
                        val startPos = recentYPositions.first()
                        val endPos = recentYPositions.last()
                        val timeDiff = 50f * (recentYPositions.size - 1) // 假设每次移动间隔约50ms
                        avgVelocity = (endPos - startPos) / timeDiff * 1000f
                    }

                    // 综合考量官方速度和自计算速度
                    val effectiveVelocity = maxOf(yVelocity, avgVelocity)

                    // 快速向下滑动或者拖动较大距离时，触发关闭
                    if (effectiveVelocity > 800 || (lastY - startY > 120)) {
                        // 传递足够大的值让BottomSheet知道需要关闭，并带上实际速度信息
                        onDragGesture?.invoke(1500f + effectiveVelocity * 0.5f)
                    } else {
                        // 正常结束手势
                        onDragGesture?.invoke(0f)
                    }

                    isDragging = false
                    velocityTracker?.recycle()
                    velocityTracker = null
                    return true
                }

                // 回收速度追踪器
                velocityTracker?.recycle()
                velocityTracker = null
                return super.onTouchEvent(event)
            }

            else -> return super.onTouchEvent(event)
        }
    }

    override fun destroy() {
        velocityTracker?.recycle()
        velocityTracker = null
        super.destroy()
    }
}
