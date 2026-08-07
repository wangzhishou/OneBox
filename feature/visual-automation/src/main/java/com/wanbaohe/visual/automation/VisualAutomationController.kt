package com.wanbaohe.visual.automation

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.t8rin.logger.makeLog
import com.shifenmiao.model.automation.AIAction
import com.shifenmiao.model.automation.AutomationResult
import com.shifenmiao.model.automation.ScreenSize
import com.wanbaohe.visual.automation.ai.VisualAIClient
import com.wanbaohe.visual.automation.capturer.ScreenshotCapturer
import com.wanbaohe.visual.automation.injector.TextInputInjector
import com.wanbaohe.visual.automation.injector.TouchInjector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 视觉自动化控制器。
 *
 * 核心流程：
 * 1. 截图当前 Activity
 * 2. 发送给 AI 分析
 * 3. 解析 AI 返回的动作
 * 4. 在 App 内执行动作（点击、滑动、输入等）
 * 5. 循环直到任务完成
 *
 * 使用方式：
 * ```
 * @Inject lateinit var controller: VisualAutomationController
 *
 * // 启动自动化任务
 * controller.startTask(activity, "帮我搜索天气预报")
 *
 * // 观察状态
 * lifecycleScope.launch {
 *     controller.state.collect { state ->
 *         // IDLE, RUNNING, WAITING_AI, EXECUTING, COMPLETED, ERROR
 *     }
 * }
 *
 * // 观察执行的动作
 * lifecycleScope.launch {
 *     controller.actions.collect { action ->
 *         // 处理 AI 决定的动作
 *     }
 * }
 * ```
 */
@Singleton
class VisualAutomationController @Inject constructor(
    private val visualAIClient: VisualAIClient,
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _state = MutableStateFlow(AutomationState.IDLE)
    val state: StateFlow<AutomationState> = _state.asStateFlow()

    private val _actions = MutableSharedFlow<AIAction>(extraBufferCapacity = 10)
    val actions: SharedFlow<AIAction> = _actions.asSharedFlow()

    private val _results = MutableSharedFlow<AutomationResult>(extraBufferCapacity = 10)
    val results: SharedFlow<AutomationResult> = _results.asSharedFlow()

    private val actionHistory = mutableListOf<String>()
    private var isRunning = false
    private var currentActivity: Activity? = null

    /**
     * 最大循环次数，防止无限循环。
     */
    var maxSteps = 20

    /**
     * 每步之间的延迟，给 UI 留出响应时间。
     */
    var stepDelayMs = 800L

    /**
     * 是否显示点击位置的视觉指示器（红色圆圈）。
     */
    var showClickIndicator = true

    /**
     * 启动自动化任务。
     * @param activity 当前 Activity，用于截图和注入事件
     * @param taskDescription 任务描述，如"帮我搜索天气预报"
     */
    fun startTask(activity: Activity, taskDescription: String) {
        if (isRunning) {
            makeLog { "VisualAutomationController: Task already running" }
            return
        }

        isRunning = true
        currentActivity = activity
        actionHistory.clear()
        _state.value = AutomationState.RUNNING

        scope.launch {
            runAutomationLoop(activity, taskDescription)
        }
    }

    /**
     * 停止当前任务。
     */
    fun stopTask() {
        isRunning = false
        currentActivity = null
        _state.value = AutomationState.IDLE
        makeLog { "VisualAutomationController: Task stopped" }
    }

    /**
     * 释放资源。
     */
    fun release() {
        stopTask()
        scope.cancel()
    }

    private suspend fun runAutomationLoop(activity: Activity, taskDescription: String) {
        var steps = 0

        while (isRunning && steps < maxSteps) {
            steps++
            makeLog { "VisualAutomationController: Step $steps" }

            // 1. 截图
            _state.value = AutomationState.CAPTURING
            val dataUri = try {
                withContext(Dispatchers.Main) {
                    ScreenshotCapturer.captureToDataUri(activity)
                }
            } catch (e: Exception) {
                makeLog { "Screenshot failed: ${e.message}" }
                _state.value = AutomationState.ERROR
                _results.emit(AutomationResult.Failure("Screenshot failed: ${e.message}"))
                break
            }

            // 2. 请求 AI
            _state.value = AutomationState.WAITING_AI
            val action = visualAIClient.requestAction(
                imageDataUri = dataUri,
                taskDescription = taskDescription,
                history = actionHistory.toList()
            )

            if (!isRunning) break

            _actions.emit(action)

            // 3. 执行动作
            _state.value = AutomationState.EXECUTING
            val result = executeAction(activity, action)
            _results.emit(result)

            // 4. 记录历史
            actionHistory.add(actionToString(action))

            // 5. 判断结束条件
            when (action) {
                is AIAction.Done -> {
                    _state.value = AutomationState.COMPLETED
                    isRunning = false
                    makeLog { "VisualAutomationController: Task completed - ${action.message}" }
                    break
                }
                is AIAction.Error -> {
                    _state.value = AutomationState.ERROR
                    isRunning = false
                    makeLog { "VisualAutomationController: Error - ${action.message}" }
                    break
                }
                else -> {
                    delay(stepDelayMs)
                }
            }
        }

        if (steps >= maxSteps) {
            _state.value = AutomationState.ERROR
            _results.emit(AutomationResult.Failure("Reached max steps ($maxSteps)"))
            isRunning = false
        }

        currentActivity = null
    }

    private suspend fun executeAction(activity: Activity, action: AIAction): AutomationResult {
        return try {
            when (action) {
                is AIAction.Click -> {
                    val screenSize = ScreenshotCapturer.getScreenSize(activity)
                    val (actualX, actualY) = mapAICoordinates(
                        action.x.toFloat(), action.y.toFloat(),
                        screenSize.width, screenSize.height,
                        activity.window.decorView.width, activity.window.decorView.height
                    )
                    if (showClickIndicator) {
                        withContext(Dispatchers.Main) {
                            showClickIndicator(activity, actualX, actualY)
                        }
                        delay(300)
                    }
                    TouchInjector.performClick(activity, actualX, actualY)
                    AutomationResult.Success(action)
                }
                is AIAction.LongPress -> {
                    val screenSize = ScreenshotCapturer.getScreenSize(activity)
                    val (actualX, actualY) = mapAICoordinates(
                        action.x.toFloat(), action.y.toFloat(),
                        screenSize.width, screenSize.height,
                        activity.window.decorView.width, activity.window.decorView.height
                    )
                    if (showClickIndicator) {
                        withContext(Dispatchers.Main) {
                            showClickIndicator(activity, actualX, actualY, durationMs = action.durationMs.toLong())
                        }
                    }
                    TouchInjector.performLongPress(activity, actualX, actualY, action.durationMs.toLong())
                    AutomationResult.Success(action)
                }
                is AIAction.Swipe -> {
                    val screenSize = ScreenshotCapturer.getScreenSize(activity)
                    val (fromX, fromY) = mapAICoordinates(
                        action.fromX.toFloat(), action.fromY.toFloat(),
                        screenSize.width, screenSize.height,
                        activity.window.decorView.width, activity.window.decorView.height
                    )
                    val (toX, toY) = mapAICoordinates(
                        action.toX.toFloat(), action.toY.toFloat(),
                        screenSize.width, screenSize.height,
                        activity.window.decorView.width, activity.window.decorView.height
                    )
                    TouchInjector.performSwipe(activity, fromX, fromY, toX, toY, action.durationMs.toLong())
                    AutomationResult.Success(action)
                }
                is AIAction.InputText -> {
                    // 对于输入文字，先尝试找到当前焦点的 EditText
                    // 如果没有，需要 AI 同时提供点击坐标
                    val focusedView = activity.currentFocus
                    if (focusedView is android.widget.EditText) {
                        TextInputInjector.inputTextDirectly(focusedView, action.text)
                    } else {
                        // 如果 AI 没有提供坐标，尝试在屏幕中心点击后输入
                        val decorView = activity.window.decorView
                        val centerX = decorView.width / 2f
                        val centerY = decorView.height / 2f
                        TextInputInjector.inputText(activity, centerX, centerY, action.text)
                    }
                    AutomationResult.Success(action)
                }
                is AIAction.GoBack -> {
                    withContext(Dispatchers.Main) {
                        (activity as? androidx.activity.ComponentActivity)
                            ?.onBackPressedDispatcher?.onBackPressed()
                            ?: @Suppress("DEPRECATION") activity.onBackPressed()
                    }
                    AutomationResult.Success(action)
                }
                is AIAction.Wait -> {
                    delay(action.durationMs.toLong())
                    AutomationResult.Success(action)
                }
                is AIAction.Done -> AutomationResult.Success(action)
                is AIAction.Error -> AutomationResult.Failure(action.message)
            }
        } catch (e: Exception) {
            makeLog { "Execute action failed: ${e.message}" }
            AutomationResult.Failure("Execute action failed: ${e.message}")
        }
    }

    /**
     * 将 AI 返回的坐标（基于截图尺寸）映射到实际屏幕坐标。
     */
    private fun mapAICoordinates(
        aiX: Float, aiY: Float,
        screenshotWidth: Int, screenshotHeight: Int,
        screenWidth: Int, screenHeight: Int
    ): Pair<Float, Float> {
        val x = aiX * screenWidth / screenshotWidth.toFloat()
        val y = aiY * screenHeight / screenshotHeight.toFloat()
        return x to y
    }

    /**
     * 公开版本,供同模块的 [com.wanbaohe.visual.automation.service.VisualAutomationService]
     * 复用坐标映射逻辑,避免 AgentTool 路径与 Controller 路径各自重复一份。
     */
    fun mapCoordinates(
        aiX: Float, aiY: Float,
        screenshotWidth: Int, screenshotHeight: Int,
        screenWidth: Int, screenHeight: Int
    ): Pair<Float, Float> = mapAICoordinates(
        aiX, aiY, screenshotWidth, screenshotHeight, screenWidth, screenHeight
    )

    /**
     * 在屏幕上显示点击位置的红色圆圈指示器。
     */
    private fun showClickIndicator(
        activity: Activity,
        x: Float,
        y: Float,
        durationMs: Long = 400
    ) {
        val decorView = activity.window.decorView as? ViewGroup ?: return
        val indicator = View(activity).apply {
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#FF5252"))
            }
        }
        val size = (40 * activity.resources.displayMetrics.density).toInt()
        val params = FrameLayout.LayoutParams(size, size)
        indicator.layoutParams = params
        indicator.x = x - size / 2
        indicator.y = y - size / 2
        indicator.alpha = 0.7f
        indicator.elevation = 999f

        decorView.addView(indicator)

        indicator.animate()
            .alpha(0f)
            .scaleX(2f)
            .scaleY(2f)
            .setDuration(durationMs)
            .withEndAction {
                decorView.removeView(indicator)
            }
            .start()
    }

    private fun actionToString(action: AIAction): String {
        return when (action) {
            is AIAction.Click -> "点击 (${action.x}, ${action.y})"
            is AIAction.LongPress -> "长按 (${action.x}, ${action.y}) ${action.durationMs}ms"
            is AIAction.Swipe -> "滑动 (${action.fromX},${action.fromY}) -> (${action.toX},${action.toY})"
            is AIAction.InputText -> "输入文字: ${action.text}"
            is AIAction.GoBack -> "返回"
            is AIAction.Wait -> "等待 ${action.durationMs}ms"
            is AIAction.Done -> "完成: ${action.message}"
            is AIAction.Error -> "错误: ${action.message}"
        }
    }

    /**
     * 公开版本,供同模块 Service 复用历史格式化逻辑。
     */
    fun formatActionForHistory(action: AIAction): String = actionToString(action)

    enum class AutomationState {
        IDLE,           // 空闲
        RUNNING,        // 任务运行中
        CAPTURING,      // 截图中
        WAITING_AI,     // 等待 AI 响应
        EXECUTING,      // 执行动作中
        COMPLETED,      // 任务完成
        ERROR,          // 出错
    }
}
