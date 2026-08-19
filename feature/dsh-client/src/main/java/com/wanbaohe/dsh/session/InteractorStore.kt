package com.wanbaohe.dsh.session

import com.wanbaohe.dsh.connection.ConnectionPhase
import com.wanbaohe.dsh.connection.DshApiClient
import com.wanbaohe.dsh.connection.DshConnectionController
import com.wanbaohe.dsh.wire.AddressedMuxFrame
import com.wanbaohe.dsh.wire.MuxFrame
import com.wanbaohe.dsh.wire.RpcBusinessException
import com.wanbaohe.dsh.wire.model.AskUserQuestionItem
import com.wanbaohe.dsh.wire.model.RespondReceipt
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

/**
 * 待审批条目(rpcId 来自可应答帧的信封层,应答时逐字回显)。
 */
data class PendingApproval(
    val rpcId: String,
    val sessionId: String,
    val approvalId: String,
    val toolName: String,
    val callId: String? = null,
    val reason: String? = null
)

/**
 * 待答问批复次(rpcId 同上;[questions] 为一批 [AskUserQuestionItem])。
 */
data class PendingQuestion(
    val rpcId: String,
    val sessionId: String,
    val questions: List<AskUserQuestionItem>
) {
    /** label 精确匹配表:questionId → 合法 label 集(本地预校验用,服务端仍是权威) */
    fun allowedLabelsFor(questionId: String): Set<String> =
        questions.firstOrNull { it.id == questionId }
            ?.options?.map { it.label }?.toSet()
            .orEmpty()
}

/** UI 收集的单题应答草稿(预校验 + 组payload 用) */
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
    /** 已被主机收走(含 not-pending:权威已 resolved,本地已清场) */
    data object Accepted : QuestionSubmitOutcome

    /** 本地预校验拦截(未发请求) */
    data class ValidationFailed(val failure: QuestionValidationFailure) : QuestionSubmitOutcome

    /** 服务端权威拒绝(bad-response):label/批次/互斥等校验未过 */
    data object BadResponse : QuestionSubmitOutcome

    /** 载波/超时/业务异常(消息折叠为可读串) */
    data class TransportFailed(val message: String) : QuestionSubmitOutcome
}

/**
 * 交互帧域 store(对齐 Flutter interactor_store.dart,DSH-PROTOCOL §1/§4/§5)。
 *
 * - 数据源是连接层的 addressedMuxFrames(信封 rpcId + MuxFrame);
 *   pending 按 rpcId 去重 —— mux 重连原样重放(rpcId 逐字复用),天然重放安全
 * - approval/resolved、question/resolved 帧清场;resolved 只 broadcast 一次绝不重放,
 *   断线窗口错过的靠「新代际(connecting)清场 + 基线重放重建」对账;
 *   应答回执 not-pending 是第二道清场信号(立即移除本地 pending)
 * - question 应答先本地预校验(漏答/未知 label/单选互斥/重复 id/批次完整性/空 custom),
 *   省一次 bad-response 往返;服务端仍是权威
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
    /** 待审批列表(按到达序;重放同 rpcId 覆盖不翻倍) */
    val approvals: StateFlow<List<PendingApproval>> = _approvals.asStateFlow()

    private val _questions = MutableStateFlow<List<PendingQuestion>>(emptyList())
    /** 待答问批复次列表(按到达序) */
    val questions: StateFlow<List<PendingQuestion>> = _questions.asStateFlow()

    // LinkedHashMap:按 rpcId 去重 + 保持到达序
    private val approvalsByRpcId = LinkedHashMap<String, PendingApproval>()
    private val questionsByRpcId = LinkedHashMap<String, PendingQuestion>()

    @Volatile
    private var disposed = false
    private var started = false

    fun start() {
        if (started) return
        started = true
        scope.launch { connection.addressedMuxFrames.collect(::onAddressedFrame) }
        scope.launch {
            connection.snapshots.collect { snapshot ->
                // 新代际开始(connecting):旧代际已死、基线重放帧尚未流入,此刻清场无竞态。
                // resolved 帧断线窗口错过没有补偿,不清场会让交互卡永久滞留。
                if (snapshot.phase != ConnectionPhase.Connecting) return@collect
                if (approvalsByRpcId.isEmpty() && questionsByRpcId.isEmpty()) return@collect
                approvalsByRpcId.clear()
                questionsByRpcId.clear()
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
     * 审批应答:value 槽装 {sessionId, approvalId, outcome: allowed-once|rejected}。
     * not-pending 回执 = host 侧已 resolved(另一端先答/turn 取消),本地立即清场。
     */
    suspend fun respondApproval(pending: PendingApproval, allow: Boolean): RespondReceipt {
        val value = buildJsonObject {
            put("sessionId", pending.sessionId)
            put("approvalId", pending.approvalId)
            put("outcome", if (allow) OutcomeAllowedOnce else OutcomeRejected)
        }
        val receipt = api.respond(pending.rpcId, value)
        if (receipt.isLate && approvalsByRpcId.remove(pending.rpcId) != null) {
            _approvals.value = approvalsByRpcId.values.toList()
        }
        return receipt
    }

    /**
     * 问答应答:先本地预校验(失败不发请求),再组 {sessionId, answer:{answers:[...]}}。
     * 空 custom 一律不发送(空串会被服务端拒);not-pending 同审批:权威清场。
     */
    suspend fun respondQuestions(
        pending: PendingQuestion,
        drafts: List<QuestionAnswerDraft>
    ): QuestionSubmitOutcome {
        validateQuestionAnswers(pending, drafts)?.let {
            return QuestionSubmitOutcome.ValidationFailed(it)
        }
        val value = buildJsonObject {
            put("sessionId", pending.sessionId)
            putJsonObject("answer") {
                putJsonArray("answers") {
                    for (draft in drafts) {
                        add(
                            buildJsonObject {
                                put("id", draft.questionId)
                                putJsonArray("selected") {
                                    draft.selected.forEach { add(it) }
                                }
                                if (!draft.custom.isNullOrEmpty()) {
                                    put("custom", draft.custom)
                                }
                            }
                        )
                    }
                }
            }
        }
        val receipt = try {
            api.respond(pending.rpcId, value)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            return QuestionSubmitOutcome.TransportFailed(errorMessage(e))
        }
        if (receipt.isLate) {
            if (questionsByRpcId.remove(pending.rpcId) != null) {
                _questions.value = questionsByRpcId.values.toList()
            }
            return QuestionSubmitOutcome.Accepted
        }
        if (receipt.isMalformed) return QuestionSubmitOutcome.BadResponse
        return QuestionSubmitOutcome.Accepted
    }

    /**
     * 本地预校验(服务端仍是权威;本地预拒只为省一次 bad-response 往返)。
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

    /** 可应答帧折叠:requested 入 pending(按 rpcId 去重),resolved 清场 */
    private fun onAddressedFrame(addressed: AddressedMuxFrame) {
        if (disposed) return
        when (val frame = addressed.frame) {
            is MuxFrame.ApprovalRequested -> {
                approvalsByRpcId[addressed.rpcId] = PendingApproval(
                    rpcId = addressed.rpcId,
                    sessionId = frame.sessionId,
                    approvalId = frame.approvalId,
                    toolName = frame.toolName,
                    callId = frame.callId,
                    reason = frame.reason
                )
                _approvals.value = approvalsByRpcId.values.toList()
            }

            is MuxFrame.ApprovalResolved -> {
                // resolved 即清场(无论 outcome),按 approvalId 寻址
                val before = approvalsByRpcId.size
                approvalsByRpcId.values.removeIf { it.approvalId == frame.approvalId }
                if (approvalsByRpcId.size != before) {
                    _approvals.value = approvalsByRpcId.values.toList()
                }
            }

            is MuxFrame.QuestionRequested -> {
                questionsByRpcId[addressed.rpcId] = PendingQuestion(
                    rpcId = addressed.rpcId,
                    sessionId = frame.sessionId,
                    questions = frame.questions
                )
                _questions.value = questionsByRpcId.values.toList()
            }

            is MuxFrame.QuestionResolved -> {
                // questionRpcId 对应请求帧的 rpcId
                if (questionsByRpcId.remove(frame.questionRpcId) != null) {
                    _questions.value = questionsByRpcId.values.toList()
                }
            }

            else -> Unit
        }
    }

    /** 业务错误取规范 message,其余折叠为 toString(与组件层 chatError 同规则) */
    private fun errorMessage(e: Throwable): String =
        (e as? RpcBusinessException)?.error?.message ?: (e.message ?: e.toString())

    companion object {
        private const val OutcomeAllowedOnce = "allowed-once"
        private const val OutcomeRejected = "rejected"
    }
}
