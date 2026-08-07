package com.wanbaohe.visual.automation.service

import android.app.Activity
import com.shifenmiao.model.automation.AIAction
import com.shifenmiao.model.automation.AutomationResult
import com.shifenmiao.model.automation.ScreenSize
import com.wanbaohe.visual.automation.VisualAutomationController
import com.wanbaohe.visual.automation.ai.VisualAIClient
import com.wanbaohe.visual.automation.capturer.ScreenshotCapturer
import com.wanbaohe.visual.automation.injector.TextInputInjector
import com.wanbaohe.visual.automation.injector.TouchInjector
import com.t8rin.logger.makeLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * UI 自动化领域 Service。
 *
 * 与 [VisualAutomationController] 的区别:
 * - Controller 是有状态自循环(`startTask` 内部跑 while + maxSteps),
 *   适合传统"用户点按钮 → 自动跑完"场景。
 * - Service 是 stateless,提供细粒度方法(screenshot / execute / decideNextAction / runTask),
 *   适合 Agent Tool 拆解后的组合调用。
 *
 * 调用方建议:
 * - AgentTool 只做薄适配(参数解析 + 调 Service + 格式化结果),不直接持有 Activity / Injector / AI Client。
 * - Activity 通过 [currentActivityProvider] 注入,严禁在 Tool 中 `LocalContext.current as Activity`。
 */
@Singleton
class VisualAutomationService @Inject constructor(
    private val visualAIClient: VisualAIClient,
    private val controller: VisualAutomationController,
    private val currentActivityProvider: CurrentActivityProvider,
    private val screenshotProcessor: ScreenshotImageProcessor,
) {

    /**
     * 单步截图 + 元数据,供 [ScreenshotUiTool] 使用。
     *
     * 截图后立即走与聊天附件一致的 WebP 压缩管道(1920px / WebP 80 / 1MB 目标),
     * 产出的 base64 / data URI 可直接作为多模态 message 的 image_url 字段。
     *
     * @param maxBase64Length 截断 base64 长度,避免超大图片撑爆 tool result。0 表示不截断。
     */
    suspend fun captureScreenshot(
        maxBase64Length: Int = 0,
    ): Result<ScreenshotSnapshot> = runCatching {
        val activity = currentActivityProvider.requireActivity()
        val bitmap = withContext(Dispatchers.Main) {
            ScreenshotCapturer.capture(activity)
        }
        val screenSize = ScreenSize(
            width = activity.window.decorView.width,
            height = activity.window.decorView.height,
        )
        val output = screenshotProcessor.process(
            bitmap = bitmap,
            name = "screenshot_${System.currentTimeMillis()}",
        )
        val finalBase64 = if (maxBase64Length > 0 && output.base64.length > maxBase64Length) {
            output.base64.substring(0, maxBase64Length)
        } else {
            output.base64
        }
        ScreenshotSnapshot(
            base64Webp = finalBase64,
            mimeType = output.mimeType,
            screenSize = screenSize,
            processedSize = output.processedSize,
            processedWidth = output.processedWidth,
            processedHeight = output.processedHeight,
            dataUri = if (finalBase64.length != output.base64.length) {
                "data:${output.mimeType};base64,$finalBase64"
            } else {
                output.dataUri
            },
            truncated = finalBase64.length != output.base64.length,
            cachedFilePath = output.cachedFilePath,
            thumbnailBase64 = output.thumbnailBase64,
        )
    }

    /**
     * 单步执行 UI 动作,供 [ActOnUiTool] 使用。
     * 不进行 AI 决策,纯动作执行。
     */
    suspend fun executeAction(action: AIAction): AutomationResult {
        val activity = currentActivityProvider.requireActivity()
        return runCatching {
            when (action) {
                is AIAction.Click -> performClick(activity, action)
                is AIAction.LongPress -> performLongPress(activity, action)
                is AIAction.Swipe -> performSwipe(activity, action)
                is AIAction.InputText -> performInputText(activity, action)
                is AIAction.GoBack -> performGoBack(activity)
                is AIAction.Wait -> {
                    kotlinx.coroutines.delay(action.durationMs.toLong())
                    AutomationResult.Success(action)
                }
                is AIAction.Done -> AutomationResult.Success(action)
                is AIAction.Error -> AutomationResult.Failure(action.message)
            }
        }.getOrElse { e ->
            makeLog { "VisualAutomationService.executeAction failed: ${e.message}" }
            AutomationResult.Failure(e.message ?: "Unknown error")
        }
    }

    /**
     * 单步 AI 决策:截一张图,让 AI 决定下一步动作。供编排器 [AutomateUiTaskTool] 内部循环。
     *
     * @param taskDescription 用户的任务目标
     * @param history 已执行过的动作历史,用于给 AI 提供上下文
     */
    suspend fun decideNextAction(
        taskDescription: String,
        history: List<String>,
    ): AIAction {
        val snapshot = captureScreenshot().getOrElse { e ->
            return AIAction.Error("Screenshot failed: ${e.message}")
        }
        return visualAIClient.requestAction(
            imageDataUri = snapshot.dataUri,
            taskDescription = taskDescription,
            history = history,
        )
    }

    /**
     * 全自动编排:循环 decide → execute,直到 AI 返回 done / error 或达最大步数。
     * 供 [AutomateUiTaskTool] 作为一键式入口。
     *
     * @param taskDescription 用户的任务目标
     * @param maxSteps 最大步数,默认 20
     * @return 最终结果(成功 / 失败 / 步数耗尽)
     */
    suspend fun runAutomationTask(
        taskDescription: String,
        maxSteps: Int = 20,
    ): AutomationTaskResult {
        val history = mutableListOf<String>()
        val stepResults = mutableListOf<StepResult>()
        var step = 0

        while (step < maxSteps) {
            step++
            val action = decideNextAction(taskDescription, history.toList())
            stepResults.add(StepResult(step, action))
            if (action is AIAction.Error) {
                return AutomationTaskResult(
                    status = AutomationTaskStatus.FAILED,
                    message = action.message,
                    steps = stepResults,
                )
            }
            if (action is AIAction.Done) {
                return AutomationTaskResult(
                    status = AutomationTaskStatus.COMPLETED,
                    message = action.message,
                    steps = stepResults,
                )
            }
            val result = executeAction(action)
            history.add(formatActionForHistory(action, result))
            if (result is AutomationResult.Failure) {
                return AutomationTaskResult(
                    status = AutomationTaskStatus.FAILED,
                    message = result.message,
                    steps = stepResults,
                )
            }
            kotlinx.coroutines.delay(controller.stepDelayMs)
        }
        return AutomationTaskResult(
            status = AutomationTaskStatus.STEP_LIMIT_REACHED,
            message = "Reached max steps ($maxSteps)",
            steps = stepResults,
        )
    }

    // ---- private helpers ----

    private suspend fun performClick(activity: Activity, action: AIAction.Click): AutomationResult {
        val screenSize = ScreenshotCapturer.getScreenSize(activity)
        val (actualX, actualY) = controller.mapCoordinates(
            aiX = action.x.toFloat(),
            aiY = action.y.toFloat(),
            screenshotWidth = screenSize.width,
            screenshotHeight = screenSize.height,
            screenWidth = activity.window.decorView.width,
            screenHeight = activity.window.decorView.height,
        )
        TouchInjector.performClick(activity, actualX, actualY)
        return AutomationResult.Success(action)
    }

    private suspend fun performLongPress(activity: Activity, action: AIAction.LongPress): AutomationResult {
        val screenSize = ScreenshotCapturer.getScreenSize(activity)
        val (actualX, actualY) = controller.mapCoordinates(
            aiX = action.x.toFloat(),
            aiY = action.y.toFloat(),
            screenshotWidth = screenSize.width,
            screenshotHeight = screenSize.height,
            screenWidth = activity.window.decorView.width,
            screenHeight = activity.window.decorView.height,
        )
        TouchInjector.performLongPress(activity, actualX, actualY, action.durationMs.toLong())
        return AutomationResult.Success(action)
    }

    private suspend fun performSwipe(activity: Activity, action: AIAction.Swipe): AutomationResult {
        val screenSize = ScreenshotCapturer.getScreenSize(activity)
        val (fromX, fromY) = controller.mapCoordinates(
            aiX = action.fromX.toFloat(),
            aiY = action.fromY.toFloat(),
            screenshotWidth = screenSize.width,
            screenshotHeight = screenSize.height,
            screenWidth = activity.window.decorView.width,
            screenHeight = activity.window.decorView.height,
        )
        val (toX, toY) = controller.mapCoordinates(
            aiX = action.toX.toFloat(),
            aiY = action.toY.toFloat(),
            screenshotWidth = screenSize.width,
            screenshotHeight = screenSize.height,
            screenWidth = activity.window.decorView.width,
            screenHeight = activity.window.decorView.height,
        )
        TouchInjector.performSwipe(activity, fromX, fromY, toX, toY, action.durationMs.toLong())
        return AutomationResult.Success(action)
    }

    private suspend fun performInputText(activity: Activity, action: AIAction.InputText): AutomationResult {
        val focusedView = activity.currentFocus
        if (focusedView is android.widget.EditText) {
            TextInputInjector.inputTextDirectly(focusedView, action.text)
        } else {
            val decorView = activity.window.decorView
            val centerX = decorView.width / 2f
            val centerY = decorView.height / 2f
            TextInputInjector.inputText(activity, centerX, centerY, action.text)
        }
        return AutomationResult.Success(action)
    }

    private suspend fun performGoBack(activity: Activity): AutomationResult {
        withContext(Dispatchers.Main) {
            (activity as? androidx.activity.ComponentActivity)
                ?.onBackPressedDispatcher?.onBackPressed()
                ?: @Suppress("DEPRECATION") activity.onBackPressed()
        }
        return AutomationResult.Success(AIAction.GoBack())
    }

    private fun formatActionForHistory(action: AIAction, result: AutomationResult): String {
        val status = if (result is AutomationResult.Success) "ok" else "fail"
        return "$status: ${controller.formatActionForHistory(action)}"
    }
}

/**
 * 单步截图结果(WebP 格式,与聊天附件管道一致)。
 */
data class ScreenshotSnapshot(
    /** 纯 base64,不含 data URI 前缀 */
    val base64Webp: String,
    val mimeType: String,
    val screenSize: ScreenSize,
    /** 完整的 data URI,可直接作为多模态 message 的 image_url 字段 */
    val dataUri: String,
    /** 是否因长度限制被截断 */
    val truncated: Boolean = false,
    /** 压缩后字节数(截断前) */
    val processedSize: Long = 0L,
    /** 压缩后尺寸(截断前) */
    val processedWidth: Int = 0,
    val processedHeight: Int = 0,
    /** 持久化到 cache 的本地路径,供 UI 渲染或后续重发 */
    val cachedFilePath: String? = null,
    /** 200px 边长的 WebP 缩略图 data URI,用于消息列表展示 */
    val thumbnailBase64: String? = null,
)

/**
 * 单步执行结果(用于编排器返回 LLM)。
 */
data class StepResult(
    val step: Int,
    val action: AIAction,
)

enum class AutomationTaskStatus { COMPLETED, FAILED, STEP_LIMIT_REACHED }

data class AutomationTaskResult(
    val status: AutomationTaskStatus,
    val message: String,
    val steps: List<StepResult>,
)