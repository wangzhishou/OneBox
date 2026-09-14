package com.shifenmiao.ai.agent.tool.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.shifenmiao.ai.agent.tool.AgentQuestionType
import com.shifenmiao.ai.agent.tool.AgentUserQuestionItem
import com.shifenmiao.ai.agent.tool.AgentUserQuestionRequest
import com.wanbaohe.a2ui.catalog.A2uiRenderProvider
import com.wanbaohe.a2ui.state.A2uiSurfaceHolder
import com.wanbaohe.a2ui.ui.A2uiSurfaceView
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import java.util.UUID

/**
 * ask_user 表单的 A2UI 渲染桥接。
 *
 * LLM 侧 `ask_user` 的 schema 保持不变(零额外 token),
 * 这里在客户端把 [AgentUserQuestionRequest] 转换为 A2UI JSON,
 * 复用 core/a2ui 渲染器(与 Agent 表单、聊天 ```a2ui``` 代码块同一套),
 * `message` / `question` 文本经 Markdown 组件渲染,支持 Markdown 语法。
 */
fun buildAskUserA2uiJson(request: AgentUserQuestionRequest): String {
    val childIds = mutableListOf<String>()
    val formJson = buildJsonObject {
        putJsonArray("components") {
            request.message.takeIf { it.isNotBlank() }?.let { message ->
                childIds.add("message")
                addJsonObject {
                    put("id", "message")
                    put("component", "Markdown")
                    put("text", message)
                }
            }
            request.questions.forEachIndexed { index, question ->
                val prefix = "q${index}_"
                if (question.header.isNotBlank()) {
                    childIds.add(prefix + "header")
                    addJsonObject {
                        put("id", prefix + "header")
                        put("component", "Text")
                        put("text", question.header)
                        put("style", "labelLarge")
                        put("weight", "semibold")
                    }
                }
                childIds.add(prefix + "question")
                addJsonObject {
                    put("id", prefix + "question")
                    put("component", "Markdown")
                    // Markdown 语境下 "*" 是强调符号,反斜杠转义为字面星号(必填标记)
                    put("text", if (question.required) question.question + " \\*" else question.question)
                }
                childIds.add(prefix + "input")
                addJsonObject {
                    put("id", prefix + "input")
                    putQuestionInput(question)
                }
            }
            addJsonObject {
                put("id", "root")
                put("component", "Column")
                put("spacing", 16)
                putJsonArray("children") {
                    childIds.forEach { add(it) }
                }
            }
        }
        putJsonObject("dataModel") {
            request.questions.forEach { question ->
                if (question.isChoiceQuestion && question.multiSelect) {
                    put(question.name, JsonArray(emptyList()))
                } else {
                    put(question.name, JsonPrimitive(""))
                }
            }
        }
    }
    return formJson.toString()
}

private fun JsonObjectBuilder.putQuestionInput(
    question: AgentUserQuestionItem,
) {
    val pointer = buildJsonObject { put("path", "/${question.name}") }
    when {
        question.isChoiceQuestion -> {
            put("component", "ListSelector")
            put("value", pointer)
            put("maxSelected", if (question.multiSelect) 0 else 1)
            putJsonArray("options") {
                question.options.forEach { option ->
                    addJsonObject {
                        put("label", option.label)
                        put("value", option.value)
                    }
                }
            }
        }

        else -> when (question.type) {
            AgentQuestionType.text -> {
                put("component", "TextField")
                put("value", pointer)
                if (question.placeholder.isNotBlank()) put("placeholder", question.placeholder)
                put("singleLine", !question.multiline)
                if (question.multiline) {
                    put("minLines", 4)
                    put("maxLines", 6)
                }
            }

            AgentQuestionType.time -> {
                put("component", "TimeInput")
                put("value", pointer)
                put("label", "")
                put("mode", "time")
            }

            AgentQuestionType.time_range -> {
                put("component", "TimeInput")
                put("value", pointer)
                put("label", "")
                put("mode", "timerange")
            }

            AgentQuestionType.date -> {
                put("component", "DateInput")
                put("value", pointer)
                put("label", "")
                put("mode", "date")
            }

            AgentQuestionType.date_range -> {
                put("component", "DateInput")
                put("value", pointer)
                put("label", "")
                put("mode", "daterange")
            }

            AgentQuestionType.color -> {
                put("component", "ColorPicker")
                put("value", pointer)
            }

            AgentQuestionType.city -> {
                put("component", "LocationPicker")
                put("value", pointer)
            }

            AgentQuestionType.image -> {
                put("component", "ImagePicker")
                put("value", pointer)
            }

            AgentQuestionType.file -> {
                put("component", "FilePicker")
                put("value", pointer)
            }

            AgentQuestionType.folder -> {
                put("component", "FolderPicker")
                put("value", pointer)
            }
        }
    }
}

/**
 * ask_user 表单状态:必填校验与 answers 组装,语义与原 AIQuestionFormState 一致
 * (未作答的问题不出现在 answers 中;多选返回字符串数组,其余返回字符串)。
 *
 * [isValid] 读取的是 a2ui dataModel 的快照状态,在组合中读取可随输入自动重组。
 */
class AskUserA2uiFormState(
    val surfaceId: String,
    private val surfaceHolder: A2uiSurfaceHolder,
    private val questions: List<AgentUserQuestionItem>,
) {
    val isValid: Boolean
        get() = questions.all { !it.required || answerOf(it) != null }

    fun buildAnswers(): Map<String, Any> = buildMap {
        questions.forEach { question ->
            answerOf(question)?.let { put(question.name, it) }
        }
    }

    private fun answerOf(question: AgentUserQuestionItem): Any? {
        val value = dataModel()[question.name] ?: return null
        return if (question.isChoiceQuestion && question.multiSelect) {
            (value as? JsonArray)
                ?.mapNotNull { (it as? JsonPrimitive)?.contentOrNull }
                ?.takeIf { it.isNotEmpty() }
        } else {
            (value as? JsonPrimitive)?.contentOrNull?.trim()?.takeIf { it.isNotEmpty() }
        }
    }

    private fun dataModel(): JsonObject =
        surfaceHolder.get(surfaceId)?.dataModel ?: JsonObject(emptyMap())
}

@Composable
fun rememberAskUserA2uiFormState(
    renderProvider: A2uiRenderProvider,
    request: AgentUserQuestionRequest,
): AskUserA2uiFormState {
    val surfaceId = remember(request.toolCallId) {
        "ask_user_form_${request.toolCallId.ifBlank { UUID.randomUUID() }}"
    }
    // 同步建 surface:首帧即存在 dataModel,必填校验才能正确订阅快照状态
    remember(request.toolCallId) {
        renderProvider.surfaceHolder.applyCreateSurfaceJson(
            buildAskUserA2uiJson(request),
            surfaceId,
        )
    }
    DisposableEffect(surfaceId) {
        onDispose {
            renderProvider.surfaceHolder.remove(surfaceId)
        }
    }
    return remember(request.toolCallId) {
        AskUserA2uiFormState(
            surfaceId = surfaceId,
            surfaceHolder = renderProvider.surfaceHolder,
            questions = request.questions,
        )
    }
}

@Composable
fun AskUserA2uiForm(
    formState: AskUserA2uiFormState,
    renderProvider: A2uiRenderProvider,
    modifier: Modifier = Modifier,
) {
    A2uiSurfaceView(
        surfaceId = formState.surfaceId,
        viewerContext = renderProvider.viewerContext(),
        modifier = modifier,
    )
}
