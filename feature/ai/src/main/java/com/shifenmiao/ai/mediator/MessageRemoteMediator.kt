package com.shifenmiao.ai.mediator

import com.shifenmiao.ai.request.AiStreamKeepAlive
import com.shifenmiao.ai.request.LlmContextBudgetService
import com.shifenmiao.ai.request.LlmRequestGateway
import com.shifenmiao.ai.upload.AttachmentContentResolver
import com.shifenmiao.ai.utils.AiUtils
import com.shifenmiao.database.ai.entity.MessageEntity
import com.shifenmiao.database.image.dao.ImageDao
import com.shifenmiao.model.ai.AiRequestProtocol
import com.shifenmiao.model.ai.Conversation
import com.shifenmiao.model.ai.ToolDefinition
import com.shifenmiao.model.ai.unified.LlmStreamEvent
import com.shifenmiao.model.ai.unified.LlmTurnRequest
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import retrofit2.HttpException
import java.io.IOException

/**
 * 消息请求中介。
 *
 * Phase 1 起统一走 [LlmRequestGateway] + [LlmProviderAdapter]，
 * 不再直接依赖 [com.shifenmiao.ai.request.ProtocolRoutingAIRequestHandler]。
 *
 * 在请求发出前调用 [LlmContextBudgetService] 对 messages 做裁剪，
 * 保证 LOCAL_ON_DEVICE 等小上下文模型不会因历史超长导致 native 崩溃。
 */
class MessageRemoteMediator @Inject constructor(
    private val gateway: LlmRequestGateway,
    private val contextBudgetService: LlmContextBudgetService,
    private val attachmentContentResolver: AttachmentContentResolver,
    private val imageDao: ImageDao,
    private val streamKeepAlive: AiStreamKeepAlive,
) {

    /**
     * 生成期间挂前台服务，退后台/锁屏时不让系统冻结进程或 defer 网络。
     *
     * 放在所有流（首轮、Agent 续轮、双人对话）的共同入口，用引用计数保证
     * 最后一条流结束才停服务；停服务失败/启动被拒都只降级，不影响数据流。
     */
    private fun Flow<LlmStreamEvent>.withAiForegroundKeepAlive(): Flow<LlmStreamEvent> =
        onStart { streamKeepAlive.begin() }
            .onCompletion { streamKeepAlive.end() }

    /**
     * 回合级保活：一个回合可能包含"首轮流 → 工具执行 → 续轮流"多段，
     * 只在流期间挂服务的话，工具执行那段空窗会被停掉，锁屏后下一段流就起不来。
     * 调用方在回合开始/结束时成对调用，与逐流引用计数叠加。
     */
    fun beginTurnKeepAlive() = streamKeepAlive.begin()

    fun endTurnKeepAlive() = streamKeepAlive.end()

    suspend fun fetchAndSaveMessages(
        conversation: Conversation,
        questionMessageEntityList: List<MessageEntity>,
        enableWebSearch: Boolean = false,
        enableReasoning: Boolean = false,
        tools: List<ToolDefinition>? = null
    ): Flow<LlmStreamEvent> = try {
        val rawRequest = AiUtils.buildTurnRequest(
            conversation = conversation,
            messageEntityList = questionMessageEntityList,
            contentReader = { path -> attachmentContentResolver.readContentFromPath(path) },
            imageDao = imageDao,
            tools = tools,
            enableWebSearch = enableWebSearch,
            enableReasoning = enableReasoning,
        )

        val trimmedRequest = applyContextBudget(conversation, rawRequest)

        val responseFlow = gateway.streamTurn(conversation, trimmedRequest)
        responseFlow.buffer(64)
            .catch { e ->
                if (e is CancellationException) throw e
                val errorMsg = e.message ?: when (e) {
                    is IOException -> "Failed to get response from local runtime. IOException"
                    is HttpException -> "Failed to get response from Server. HttpException"
                    else -> "Unknown error occurred"
                }
                emit(LlmStreamEvent.Error(errorMessage = errorMsg))
            }
            .withAiForegroundKeepAlive()
    } catch (e: Exception) {
        if (e is CancellationException) throw e
        flowOf(LlmStreamEvent.Error(errorMessage = e.message ?: "Error initializing chat request"))
            .buffer(64)
    }

    suspend fun fetchWithDirectRequest(
        conversation: Conversation,
        chatCompletionRequest: LlmTurnRequest
    ): Flow<LlmStreamEvent> = try {
        val trimmed = applyContextBudget(conversation, chatCompletionRequest)
        gateway.streamTurn(conversation, trimmed)
            .buffer(64)
            .catch { e ->
                if (e is CancellationException) throw e
                emit(LlmStreamEvent.Error(errorMessage = e.message ?: "Unknown error occurred"))
            }
            .withAiForegroundKeepAlive()
    } catch (e: Exception) {
        if (e is CancellationException) throw e
        flowOf(LlmStreamEvent.Error(errorMessage = e.message ?: "Error initializing direct request"))
            .buffer(64)
    }

    private suspend fun applyContextBudget(
        conversation: Conversation,
        request: LlmTurnRequest,
    ): LlmTurnRequest {
        val model = conversation.engine.model
        val isLocal = conversation.engine.requestProtocol == AiRequestProtocol.LOCAL_ON_DEVICE

        // contextWindowTokens == 0 显式表示"未配置 / 未知"，跳过裁剪。
        // 注意：这里使用原始 contextWindowTokens 而非 effectiveContextWindow()，
        // 因为 effective 会按模型名回退到内置窗口（如 gpt-4 → 8192），
        // 但本地模型是用户自导入、没有"已知模型名"概念，effective fallback 对其无意义。
        val contextWindow = model.contextWindowTokens.takeIf { it > 0 } ?: 0
        if (contextWindow == 0) return request

        val reservedOutput = if (isLocal) {
            (contextWindow / 4).coerceAtLeast(256)
        } else {
            (contextWindow / 8).coerceAtLeast(512)
        }

        val supportsVision = model.canImage
        val trimmed = contextBudgetService.trimMessages(
            messages = request.messages,
            contextWindowTokens = contextWindow,
            reservedOutputTokens = reservedOutput,
            protocol = conversation.engine.requestProtocol,
            supportsVision = supportsVision,
        )
        return if (trimmed === request.messages) {
            request
        } else {
            request.copy(messages = trimmed)
        }
    }
}
