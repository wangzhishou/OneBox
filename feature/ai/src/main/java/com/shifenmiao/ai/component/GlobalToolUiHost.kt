package com.shifenmiao.ai.component

import com.google.gson.Gson
import com.shifenmiao.ai.agent.tool.AgentUserQuestionRequest
import com.shifenmiao.ai.agent.tool.FilePickerRequest
import com.shifenmiao.ai.agent.tool.FolderPickerRequest
import com.shifenmiao.ai.agent.tool.InteractiveToolRuntime
import com.shifenmiao.ai.agent.tool.ToolConfirmationRequest
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 全局工具 UI 宿主。
 *
 * 这个对象只负责暴露"需要上层渲染/处理"的工具交互请求：
 * - 交互式请求：直接代理 [InteractiveToolRuntime] 的请求与回填（动态表单/工具确认）
 * - 全局导航请求：把工具触发的页面打开请求提升到 App 顶层统一消费
 *
 * 设计目标：
 * 1. AIChatComponent 只负责调用工具，不再承担 UI 宿主职责
 * 2. App 顶层统一监听该宿主，从任意入口都能渲染交互 UI
 * 3. 保持当前默认渲染方案不变，后续可按 tool/type 扩展不同渲染器
 */
@Singleton
class GlobalToolUiHost @Inject constructor(
    private val interactiveToolRuntime: InteractiveToolRuntime,
    private val gson: Gson
) {

    val confirmationRequest: StateFlow<ToolConfirmationRequest?> =
        interactiveToolRuntime.confirmationRequest

    val questionRequest: StateFlow<AgentUserQuestionRequest?> =
        interactiveToolRuntime.questionRequest

    /** 文件选取请求，由 PickFilesTool 发布，由 AIGlobalActionOverlay 消费 */
    val filePickerRequest: StateFlow<FilePickerRequest?> =
        interactiveToolRuntime.filePickerRequest

    /** 目录选取请求，由 PickFolderTool 发布，由 AIGlobalActionOverlay 消费 */
    val folderPickerRequest: StateFlow<FolderPickerRequest?> =
        interactiveToolRuntime.folderPickerRequest

    private val _pendingScreenNavigations =
        MutableStateFlow<List<PendingScreenNavigation>>(emptyList())
    val pendingScreenNavigations: StateFlow<List<PendingScreenNavigation>> = _pendingScreenNavigations

    /**
     * 工具触发页面打开时，统一压入全局队列，由 App 顶层串行消费。
     * 队列化而非单值覆盖，避免同一轮工具链里连续打开多个页面时丢请求。
     */
    fun enqueueScreenNavigation(
        requestId: String,
        screen: Screen
    ) {
        _pendingScreenNavigations.value = _pendingScreenNavigations.value + PendingScreenNavigation(
            requestId = requestId,
            screen = screen
        )
    }

    /**
     * 顶层宿主在真正触发导航后需要显式确认消费，避免重复打开同一页面。
     */
    fun acknowledgeScreenNavigation(requestId: String) {
        _pendingScreenNavigations.value = _pendingScreenNavigations.value
            .filterNot { it.requestId == requestId }
    }

    fun submitConfirmation(payload: String) {
        interactiveToolRuntime.submitConfirmation(payload)
    }

    fun cancelConfirmation() {
        interactiveToolRuntime.cancelConfirmation()
    }

    fun submitUserQuestion(answersJson: String) {
        interactiveToolRuntime.submitUserQuestion(answersJson)
    }

    fun submitUserQuestionAnswers(answers: Map<String, Any>) {
        interactiveToolRuntime.submitUserQuestion(gson.toJson(answers))
    }

    fun cancelUserQuestion() {
        interactiveToolRuntime.cancelUserQuestion()
    }

    /** 用户通过系统文件选择器选取了文件，提交 content URI 逗号分隔列表 */
    fun submitFilePicker(urisCsv: String) {
        interactiveToolRuntime.submitFilePicker(urisCsv)
    }

    fun cancelFilePicker() {
        interactiveToolRuntime.cancelFilePicker()
    }

    /** 用户通过系统目录选择器选取了目录，提交目录 URI */
    fun submitFolderPicker(uriString: String) {
        interactiveToolRuntime.submitFolderPicker(uriString)
    }

    fun cancelFolderPicker() {
        interactiveToolRuntime.cancelFolderPicker()
    }

    data class PendingScreenNavigation(
        val requestId: String,
        val screen: Screen
    )
}
