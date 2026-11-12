/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.wanbaohe.dsh.session

import com.wanbaohe.dsh.connection.ConnectionPhase
import com.wanbaohe.dsh.connection.DshApiClient
import com.wanbaohe.dsh.connection.DshConnectionController
import com.wanbaohe.dsh.wire.ApprovalOutcome
import com.wanbaohe.dsh.wire.ApprovalRequestPayload
import com.wanbaohe.dsh.wire.CarrierException
import com.wanbaohe.dsh.wire.DshJson
import com.wanbaohe.dsh.wire.EventResultOutcome
import com.wanbaohe.dsh.wire.QuestionRequestPayload
import com.wanbaohe.dsh.wire.RemoteEventFrame
import com.wanbaohe.dsh.wire.RemoteEventNames
import com.wanbaohe.dsh.wire.RpcBusinessException
import com.wanbaohe.dsh.wire.model.AskUserQuestionAnswer
import com.wanbaohe.dsh.wire.model.AskUserQuestionAnswerItem
import com.wanbaohe.dsh.wire.model.AskUserQuestionItem
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

/**
 * 待审批条目(0.1.5:应答凭 `eventId` 走 `$events/result`,不再有 rpcId + /api/respond)。
 */
data class PendingApproval(
    val eventId: String,
    val sessionId: String,
    val toolName: String,
    val callId: String? = null,
    val reason: String? = null
)

/** 待答问批复次 */
data class PendingQuestion(
    val eventId: String,
    val sessionId: String,
    val questions: List<AskUserQuestionItem>
) {
    /** label 精确匹配表:questionId → 合法 label 集(本地预校验用,服务端仍是权威) */
    fun allowedLabelsFor(questionId: String): Set<String> =
        questions.firstOrNull { it.id == questionId }
            ?.options?.map { it.label }?.toSet()
            .orEmpty()
}

/** UI 收集的单题应答草稿(预校验 + 组 payload 用) */
data class QuestionAnswerDraft(
    val questionId: String,
    val selected: List<String> = emptyList(),
    val custom: String? = null
)

/** 问答本地预校验失败原因(UI 映射为本地化文案;不发请求) */
sealed interface QuestionValidationFailure {
    data class DuplicateAnswer(val questionId: String) : QuestionValidationFailure
    data class MissingAnswer(val questionId: String) : QuestionValidationFailure
    data class UnknownLabel(val questionId: String) : QuestionValidationFailure
    data class SingleSelectMultiple(val questionId: String) : QuestionValidationFailure
    data class EmptyAnswer(val questionId: String) : QuestionValidationFailure
    data class CustomWithSelectionOnSingle(val questionId: String) : QuestionValidationFailure
    data object UnknownQuestionId : QuestionValidationFailure
}

/** 问答提交结局:UI 据此内联展示(Accepted 时卡片随 store 清场消失) */
sealed interface QuestionSubmitOutcome {
    /** 已被主机收走(应答已发出,无论主机是否仍在等) */
    data object Accepted : QuestionSubmitOutcome

    /** 本地预校验拦截(未发请求) */
    data class ValidationFailed(val failure: QuestionValidationFailure) : QuestionSubmitOutcome

    /** 服务端权威拒绝(应答体被拒) */
    data object BadResponse : QuestionSubmitOutcome

    /** 载波/超时/业务异常(消息折叠为可读串) */
    data class TransportFailed(val message: String) : QuestionSubmitOutcome
}

/**
 * 交互帧域 store(DSH 0.1.5-rc.2)。
 *
 * **0.1.5 的应答模型变了**:审批/问答是 `$events` 流上的 **waterfall 帧**
 * (旧版是带 rpcId 的可应答帧 + `POST /api/respond`),应答走
 * `POST /api/$events/result`,body `{clientId,eventId,outcome}`。
 *
 * - pending 按 `eventId` 去重;同一 eventId 的应答只有第一个到达的有效
 * - `cancel` 帧(Host 侧不再等待)与本流换代(Ready→Connecting)都会清场:
 *   重连后 Host 会**重新发起**仍未决的 waterfall,不会漏
 * - question 应答先本地预校验(漏答/未知 label/单选互斥/重复 id/空 custom),
 *   省一次服务端拒绝往返;服务端仍是权威
 *
 * 生命周期与连接实例绑定:由组件层创建并 [dispose](不做 @Singleton)。
 */
class InteractorStore(
    private val api: DshApiClient,
    private val connection: DshConnectionController,
    parentScope: CoroutineScope
) {

    /** 子 scope:dispose 只取消自己,不动组件 scope */
    private val scope = CoroutineScope(
        parentScope.coroutineContext + SupervisorJob(parentScope.coroutineContext[Job])
    )

    private val _approvals = MutableStateFlow<List<PendingApproval>>(emptyList())

    /** 待审批列表(按到达序;同 eventId 覆盖不翻倍) */
    val approvals: StateFlow<List<PendingApproval>> = _approvals.asStateFlow()

    private val _questions = MutableStateFlow<List<PendingQuestion>>(emptyList())

    /** 待答问批复次列表(按到达序) */
    val questions: StateFlow<List<PendingQuestion>> = _questions.asStateFlow()

    // LinkedHashMap:按 eventId 去重 + 保持到达序
    private val approvalsByEventId = LinkedHashMap<String, PendingApproval>()
    private val questionsByEventId = LinkedHashMap<String, PendingQuestion>()

    @Volatile
    private var disposed = false
    private var started = false

    fun start() {
        if (started) return
        started = true
        scope.launch { connection.eventFrames.collect(::onEventFrame) }
        scope.launch {
            connection.snapshots.collect { snapshot ->
                // 新代际开始(connecting):旧代的待答项已随流作废;Host 会重新发起未决项
                if (snapshot.phase != ConnectionPhase.Connecting) return@collect
                if (approvalsByEventId.isEmpty() && questionsByEventId.isEmpty()) return@collect
                approvalsByEventId.clear()
                questionsByEventId.clear()
                _approvals.value = emptyList()
                _questions.value = emptyList()
            }
        }
    }

    fun dispose() {
        disposed = true
        scope.cancel()
    }

    /**
     * 审批应答:outcome 是闭合词表里的纯字符串(allowed-once / rejected)。
     * 应答立即清场;迟到/重复应答由主机忽略。
     */
    suspend fun respondApproval(pending: PendingApproval, allow: Boolean): Boolean {
        val clientId = connection.eventClientId
            ?: throw CarrierException("events stream 未就绪,无法应答审批")
        val outcome = EventResultOutcome(
            kind = EventResultOutcome.KIND_RESULT,
            value = JsonPrimitive(if (allow) ApprovalOutcome.ALLOWED_ONCE else ApprovalOutcome.REJECTED)
        )
        api.eventsResult(clientId, pending.eventId, outcome)
        clearApproval(pending.eventId)
        return true
    }

    /**
     * 问答应答:先本地预校验(失败不发请求),再组 `{answers:[{id,selected,custom?}]}`。
     * 空 custom 一律不发送(空串会被服务端拒)。
     */
    suspend fun respondQuestions(
        pending: PendingQuestion,
        drafts: List<QuestionAnswerDraft>
    ): QuestionSubmitOutcome {
        validateQuestionAnswers(pending, drafts)?.let {
            return QuestionSubmitOutcome.ValidationFailed(it)
        }
        val clientId = connection.eventClientId
            ?: return QuestionSubmitOutcome.TransportFailed("events stream 未就绪")
        val answer = AskUserQuestionAnswer(
            answers = drafts.map { draft ->
                AskUserQuestionAnswerItem(
                    id = draft.questionId,
                    selected = draft.selected,
                    custom = draft.custom?.takeIf { it.isNotEmpty() }
                )
            }
        )
        return try {
            api.eventsResultValue(
                clientId = clientId,
                eventId = pending.eventId,
                value = DshJson.encodeToJsonElement(AskUserQuestionAnswer.serializer(), answer)
            )
            clearQuestion(pending.eventId)
            QuestionSubmitOutcome.Accepted
        } catch (e: CancellationException) {
            throw e
        } catch (e: RpcBusinessException) {
            QuestionSubmitOutcome.BadResponse
        } catch (e: Throwable) {
            QuestionSubmitOutcome.TransportFailed(errorMessage(e))
        }
    }

    /**
     * 本地预校验(服务端仍是权威;本地预拒只为省一次往返)。
     * 返回 null = 可发;否则返回拒绝原因。
     */
    fun validateQuestionAnswers(
        pending: PendingQuestion,
        drafts: List<QuestionAnswerDraft>
    ): QuestionValidationFailure? {
        val byId = HashMap<String, QuestionAnswerDraft>()
        for (draft in drafts) {
            if (byId.put(draft.questionId, draft) != null) {
                return QuestionValidationFailure.DuplicateAnswer(draft.questionId)
            }
        }
        for (question in pending.questions) {
            val draft = byId[question.id]
                ?: return QuestionValidationFailure.MissingAnswer(question.id)
            val allowed = pending.allowedLabelsFor(question.id)
            if (draft.selected.any { it !in allowed }) {
                return QuestionValidationFailure.UnknownLabel(question.id)
            }
            val multi = question.multiSelect == true
            if (!multi && draft.selected.size > 1) {
                return QuestionValidationFailure.SingleSelectMultiple(question.id)
            }
            if (draft.selected.isEmpty() && draft.custom.isNullOrEmpty()) {
                return QuestionValidationFailure.EmptyAnswer(question.id)
            }
            if (!multi && !draft.custom.isNullOrEmpty() && draft.selected.isNotEmpty()) {
                return QuestionValidationFailure.CustomWithSelectionOnSingle(question.id)
            }
        }
        if (byId.size > pending.questions.size) {
            return QuestionValidationFailure.UnknownQuestionId
        }
        return null
    }

    /** waterfall 帧折叠:按事件名路由;cancel 帧即清场 */
    private fun onEventFrame(frame: RemoteEventFrame) {
        if (disposed) return
        when (frame) {
            is RemoteEventFrame.Waterfall -> when (frame.event) {
                RemoteEventNames.APPROVAL_REQUEST -> {
                    val payload = runCatching {
                        DshJson.decodeFromJsonElement(
                            ApprovalRequestPayload.serializer(),
                            frame.request ?: return
                        )
                    }.getOrNull() ?: return
                    approvalsByEventId[frame.eventId] = PendingApproval(
                        eventId = frame.eventId,
                        sessionId = frame.agentId.orEmpty(),
                        toolName = payload.toolName,
                        callId = payload.callId,
                        reason = payload.reason
                    )
                    _approvals.value = approvalsByEventId.values.toList()
                }

                RemoteEventNames.USER_QUESTIONS_REQUEST -> {
                    val payload = runCatching {
                        DshJson.decodeFromJsonElement(
                            QuestionRequestPayload.serializer(),
                            frame.request ?: return
                        )
                    }.getOrNull() ?: return
                    questionsByEventId[frame.eventId] = PendingQuestion(
                        eventId = frame.eventId,
                        sessionId = frame.agentId.orEmpty(),
                        questions = payload.questions
                    )
                    _questions.value = questionsByEventId.values.toList()
                }
            }

            is RemoteEventFrame.Cancel -> {
                clearApproval(frame.eventId)
                clearQuestion(frame.eventId)
            }

            else -> Unit
        }
    }

    private fun clearApproval(eventId: String) {
        if (approvalsByEventId.remove(eventId) != null) {
            _approvals.value = approvalsByEventId.values.toList()
        }
    }

    private fun clearQuestion(eventId: String) {
        if (questionsByEventId.remove(eventId) != null) {
            _questions.value = questionsByEventId.values.toList()
        }
    }

    /** 业务错误取规范 message,其余折叠为 toString */
    private fun errorMessage(e: Throwable): String =
        (e as? RpcBusinessException)?.error?.message ?: (e.message ?: e.toString())
}
