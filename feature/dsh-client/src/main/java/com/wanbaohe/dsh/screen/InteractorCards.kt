package com.wanbaohe.dsh.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wanbaohe.dsh.R
import com.wanbaohe.dsh.session.PendingApproval
import com.wanbaohe.dsh.session.PendingQuestion
import com.wanbaohe.dsh.session.QuestionAnswerDraft
import com.wanbaohe.dsh.session.QuestionSubmitOutcome
import com.wanbaohe.dsh.session.QuestionValidationFailure
import com.wanbaohe.dsh.wire.model.AskUserQuestionItem
import com.wanbaohe.dsh.wire.model.QueueItem
import com.wanbaohe.dsh.wire.model.textPreview
import kotlinx.coroutines.launch

/**
 * P3 交互区(对齐 Flutter interactor_widgets.dart):审批卡 + 问答表单 + 队列 Dock,
 * 固定在消息流与输入框之间;多卡并存时限高内部滚动,不把输入框挤出屏幕。
 *
 * 刷新安全:问答表单以 rpcId 为 remember 键 —— 列表增删/重组不丢已选选项与已输入文本;
 * 重连重放同 rpcId 只是覆盖 pending,表单状态天然保留。
 */
@Composable
fun InteractorSection(
    approvals: List<PendingApproval>,
    questions: List<PendingQuestion>,
    queueItems: List<QueueItem>,
    currentSessionId: String?,
    onApprovalRespond: (PendingApproval, Boolean) -> Unit,
    onQuestionSubmit: suspend (PendingQuestion, List<QuestionAnswerDraft>) -> QuestionSubmitOutcome,
    onQueueRemove: (QueueItem) -> Unit
) {
    val queued = queueItems.filter { it.isQueued }
    if (approvals.isEmpty() && questions.isEmpty() && queued.isEmpty()) return
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 340.dp)
            .verticalScroll(rememberScrollState())
    ) {
        for (approval in approvals) {
            ApprovalCard(
                approval = approval,
                foreignSession = currentSessionId != null && approval.sessionId != currentSessionId,
                onRespond = onApprovalRespond
            )
        }
        for (question in questions) {
            QuestionFormCard(
                pending = question,
                foreignSession = currentSessionId != null && question.sessionId != currentSessionId,
                onSubmit = onQuestionSubmit
            )
        }
        if (queued.isNotEmpty()) {
            QueueDock(items = queued, onRemove = onQueueRemove)
        }
    }
}

/** 归属会话短标签(交互来自非当前会话时显示,取 sessionId 前 8 位) */
@Composable
private fun SessionBadge(sessionId: String) {
    Surface(
        shape = RoundedCornerShape(9.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
    ) {
        Text(
            text = sessionId.take(8),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** 审批卡:工具名 + 理由 + 允许一次/拒绝 二键 */
@Composable
private fun ApprovalCard(
    approval: PendingApproval,
    foreignSession: Boolean,
    onRespond: (PendingApproval, Boolean) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.dsh_approval_request),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                    )
                    Text(
                        text = approval.toolName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (foreignSession) SessionBadge(approval.sessionId)
            }
            if (!approval.reason.isNullOrEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = approval.reason,
                        modifier = Modifier.padding(10.dp),
                        fontSize = 13.sp
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(onClick = { onRespond(approval, false) }) {
                    Text(stringResource(R.string.dsh_approval_reject))
                }
                Spacer(Modifier.width(10.dp))
                Button(onClick = { onRespond(approval, true) }) {
                    Text(stringResource(R.string.dsh_approval_allow_once))
                }
            }
        }
    }
}

/**
 * 问答表单:选项 chips(单选/多选按 multiSelect)+ custom 输入(仅多选可带,协议语义)
 * + 提交前本地预校验 + bad-response/回执错误内联展示(不清空已填内容)。
 */
@Composable
private fun QuestionFormCard(
    pending: PendingQuestion,
    foreignSession: Boolean,
    onSubmit: suspend (PendingQuestion, List<QuestionAnswerDraft>) -> QuestionSubmitOutcome
) {
    val scope = rememberCoroutineScope()
    // rpcId 为 remember 键:重连重放同 rpcId 不丢草稿
    var selections by remember(pending.rpcId) { mutableStateOf<Map<String, Set<String>>>(emptyMap()) }
    var customs by remember(pending.rpcId) { mutableStateOf<Map<String, String>>(emptyMap()) }
    var validationFailure by remember(pending.rpcId) {
        mutableStateOf<QuestionValidationFailure?>(null)
    }
    var receiptError by remember(pending.rpcId) { mutableStateOf<String?>(null) }
    var submitting by remember(pending.rpcId) { mutableStateOf(false) }

    val badResponseText = stringResource(R.string.dsh_question_bad_response)
    val inlineError = validationFailure?.let { validationText(it) } ?: receiptError

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.dsh_question_title),
                    modifier = Modifier.weight(1f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                if (foreignSession) SessionBadge(pending.sessionId)
            }
            for (question in pending.questions) {
                QuestionBlock(
                    question = question,
                    selected = selections[question.id].orEmpty(),
                    custom = customs[question.id],
                    onSelect = { label ->
                        validationFailure = null
                        val current = selections[question.id].orEmpty()
                        val next = if (question.multiSelect == true) {
                            if (label in current) current - label else current + label
                        } else {
                            setOf(label)
                        }
                        selections = selections + (question.id to next)
                    },
                    onCustomChange = { text ->
                        validationFailure = null
                        customs = customs + (question.id to text)
                    }
                )
            }
            if (inlineError != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.55f)
                ) {
                    Text(
                        text = inlineError,
                        modifier = Modifier.padding(8.dp),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    enabled = !submitting,
                    onClick = {
                        if (submitting) return@Button
                        scope.launch {
                            submitting = true
                            receiptError = null
                            val drafts = pending.questions.map { q ->
                                QuestionAnswerDraft(
                                    questionId = q.id,
                                    selected = selections[q.id]?.toList().orEmpty(),
                                    custom = customs[q.id]
                                )
                            }
                            when (val outcome = onSubmit(pending, drafts)) {
                                // Accepted:卡片随 store 清场消失,无需提示
                                QuestionSubmitOutcome.Accepted -> Unit
                                QuestionSubmitOutcome.BadResponse -> receiptError = badResponseText
                                is QuestionSubmitOutcome.ValidationFailed ->
                                    validationFailure = outcome.failure
                                is QuestionSubmitOutcome.TransportFailed ->
                                    receiptError = outcome.message
                            }
                            submitting = false
                        }
                    }
                ) {
                    if (submitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(stringResource(R.string.dsh_question_submit))
                    }
                }
            }
        }
    }
}

/** 单题块:header 徽标 + 题干 + detail + 选项 chips + custom 输入(仅多选) */
@Composable
private fun QuestionBlock(
    question: AskUserQuestionItem,
    selected: Set<String>,
    custom: String?,
    onSelect: (String) -> Unit,
    onCustomChange: (String) -> Unit
) {
    val multi = question.multiSelect == true
    Column(modifier = Modifier.padding(top = 12.dp)) {
        if (!question.header.isNullOrEmpty()) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.14f)
            ) {
                Text(
                    text = question.header,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Text(
            text = question.question,
            modifier = Modifier.padding(top = 6.dp),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        if (!question.detail.isNullOrEmpty()) {
            Text(
                text = question.detail,
                modifier = Modifier.padding(top = 3.dp),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        FlowRow(
            modifier = Modifier.padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (option in question.options.orEmpty()) {
                FilterChip(
                    selected = option.label in selected,
                    onClick = { onSelect(option.label) },
                    label = {
                        Text(
                            text = option.description?.let { "${option.label} — $it" }
                                ?: option.label,
                            fontSize = 13.sp
                        )
                    }
                )
            }
        }
        if (multi) {
            OutlinedTextField(
                value = custom.orEmpty(),
                onValueChange = onCustomChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                placeholder = { Text(stringResource(R.string.dsh_question_custom_hint)) },
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
                maxLines = 3
            )
        }
    }
}

/** 预校验失败原因 → 本地化文案 */
@Composable
private fun validationText(failure: QuestionValidationFailure): String = when (failure) {
    is QuestionValidationFailure.DuplicateAnswer ->
        stringResource(R.string.dsh_qv_duplicate, failure.questionId)
    is QuestionValidationFailure.MissingAnswer ->
        stringResource(R.string.dsh_qv_missing, failure.questionId)
    is QuestionValidationFailure.UnknownLabel ->
        stringResource(R.string.dsh_qv_unknown_label, failure.questionId)
    is QuestionValidationFailure.SingleSelectMultiple ->
        stringResource(R.string.dsh_qv_single_multiple, failure.questionId)
    is QuestionValidationFailure.EmptyAnswer ->
        stringResource(R.string.dsh_qv_empty, failure.questionId)
    is QuestionValidationFailure.CustomWithSelectionOnSingle ->
        stringResource(R.string.dsh_qv_custom_single, failure.questionId)
    QuestionValidationFailure.UnknownQuestionId ->
        stringResource(R.string.dsh_qv_unknown_id)
}

/** 队列 Dock:待处理收件箱快照(仅 placement == queued),逐条删除 */
@Composable
private fun QueueDock(
    items: List<QueueItem>,
    onRemove: (QueueItem) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(
                text = stringResource(R.string.dsh_queue_title, items.size),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            for (item in items) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.textPreview()
                            ?: stringResource(R.string.dsh_queue_non_text),
                        modifier = Modifier.weight(1f),
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val id = item.id
                    if (id != null) {
                        IconButton(
                            onClick = { onRemove(item) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.dsh_queue_remove),
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
