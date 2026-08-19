package com.wanbaohe.dsh.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.theme.AppTheme
import com.wanbaohe.dsh.R
import com.wanbaohe.dsh.session.FeedbackNoteTooLargeException
import com.wanbaohe.dsh.session.FeedbackStore
import com.wanbaohe.dsh.session.FeedbackStoreException
import com.wanbaohe.dsh.session.FeedbackVersionConflictException
import com.wanbaohe.dsh.wire.model.FeedbackItem
import com.wanbaohe.dsh.wire.model.FeedbackNoteMaxBytes
import com.wanbaohe.dsh.wire.model.FeedbackRatingNegative
import com.wanbaohe.dsh.wire.model.FeedbackRatingPositive
import kotlinx.coroutines.launch

/**
 * 消息反馈行(对齐 Flutter feedback_row.dart,DSH-PROTOCOL §9 messageFeedback 契约):
 * - 👍/👎/备注 常显按钮(触控目标 ≥44dp);已评高亮;再点同一侧 = 撤回(delete);
 *   切换另一侧保留已有 note
 * - 备注是评分条目的属性(put 必须带 rating);未评分保存 → 内联提示先评分
 * - CAS:ifVersion 取条目当前 version;version-conflict → 权威条目直接对账
 * - 请求在途禁用;错误内联展示(不弹横幅)
 */
@Composable
fun FeedbackRow(
    store: FeedbackStore,
    sessionId: String,
    messageId: String,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val rateFirstText = stringResource(R.string.dsh_feedback_rate_first)
    var item by remember(messageId) { mutableStateOf(store.itemsFor(sessionId).find(messageId)) }
    var busy by remember(messageId) { mutableStateOf(false) }
    var error by remember(messageId) { mutableStateOf<String?>(null) }
    var noteEditorOpen by remember(messageId) { mutableStateOf(false) }

    // 变更广播 → 重读本行条目;首挂时拉取整段对话(缓存命中零往返)
    LaunchedEffect(sessionId, messageId) {
        launch {
            store.changed.collect {
                item = store.itemsFor(sessionId).find(messageId)
            }
        }
        busy = true
        item = try {
            store.list(sessionId).find(messageId)
        } catch (_: Throwable) {
            null // 首拉失败保持未评态;错误在动作时内联呈现
        } finally {
            busy = false
        }
    }

    fun runPut(rating: String, note: String?) {
        busy = true
        error = null
        scope.launch {
            try {
                item = store.put(
                    sessionId,
                    messageId,
                    rating,
                    note = note,
                    ifVersion = item?.version
                )
            } catch (e: FeedbackVersionConflictException) {
                // 直接对账:权威条目覆盖本地(并发删除 → 未评)
                item = e.authoritative
            } catch (e: FeedbackNoteTooLargeException) {
                error = e.message
            } catch (e: FeedbackStoreException) {
                error = e.message ?: e.code
            } catch (e: Throwable) {
                error = e.message
            }
            busy = false
        }
    }

    fun runDelete() {
        busy = true
        error = null
        scope.launch {
            try {
                store.delete(sessionId, messageId, ifVersion = item?.version)
                item = null
            } catch (e: FeedbackStoreException) {
                error = e.message ?: e.code
            } catch (e: Throwable) {
                error = e.message
            }
            busy = false
        }
    }

    /** 评分:已评同侧 = 撤回;切换另一侧保留已有 note */
    fun rate(rating: String) {
        if (busy) return
        val current = item
        if (current != null && current.rating == rating) {
            runDelete()
        } else {
            runPut(rating, note = current?.note)
        }
    }

    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val positiveSelected = item?.rating == FeedbackRatingPositive
            IconButton(
                onClick = { rate(FeedbackRatingPositive) },
                enabled = !busy,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = if (positiveSelected) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                    contentDescription = stringResource(R.string.dsh_feedback_positive),
                    modifier = Modifier.size(18.dp),
                    tint = if (positiveSelected) {
                        AppTheme.colors.getPrimaryColor()
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            val negativeSelected = item?.rating == FeedbackRatingNegative
            IconButton(
                onClick = { rate(FeedbackRatingNegative) },
                enabled = !busy,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = if (negativeSelected) Icons.Filled.ThumbDown else Icons.Outlined.ThumbDown,
                    contentDescription = stringResource(R.string.dsh_feedback_negative),
                    modifier = Modifier.size(18.dp),
                    tint = if (negativeSelected) {
                        AppTheme.colors.getPrimaryColor()
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            val hasNote = !item?.note.isNullOrEmpty()
            IconButton(
                onClick = { noteEditorOpen = true },
                enabled = !busy,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notes,
                    contentDescription = stringResource(R.string.dsh_feedback_note),
                    modifier = Modifier.size(18.dp),
                    tint = if (hasNote) {
                        AppTheme.colors.getPrimaryColor()
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
        error?.let {
            Text(
                text = it,
                modifier = Modifier.padding(top = 2.dp),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.error
            )
        }
    }

    if (noteEditorOpen) {
        FeedbackNoteSheet(
            initialNote = item?.note.orEmpty(),
            currentRating = item?.rating,
            onDismiss = { noteEditorOpen = false },
            onSave = { note ->
                noteEditorOpen = false
                val rating = item?.rating
                if (rating == null) {
                    // 备注是评分条目的属性:未评分先评分
                    error = rateFirstText
                } else {
                    runPut(rating, note = note.takeIf { it.isNotEmpty() })
                }
            }
        )
    }
}

/** 备注编辑 sheet:8192 上限本地预拒 + note-too-large 文案;键盘弹起时整体上移 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeedbackNoteSheet(
    initialNote: String,
    currentRating: String?,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var note by rememberSaveable { mutableStateOf(initialNote) }
    var error by remember { mutableStateOf<String?>(null) }
    val tooLargeText = stringResource(R.string.dsh_feedback_note_too_large, FeedbackNoteMaxBytes)
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(start = 16.dp, top = 4.dp, end = 16.dp, bottom = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.dsh_feedback_note_title),
                    modifier = Modifier.weight(1f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = stringResource(R.string.dsh_close)
                    )
                }
            }
            currentRating?.let {
                Text(
                    text = stringResource(
                        if (it == FeedbackRatingPositive) {
                            R.string.dsh_feedback_current_positive
                        } else {
                            R.string.dsh_feedback_current_negative
                        }
                    ),
                    modifier = Modifier.padding(bottom = 8.dp),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            OutlinedTextField(
                value = note,
                onValueChange = { value ->
                    // 本地预拒(服务端 maxNoteBytes=8192 同值)
                    if (value.length <= FeedbackNoteMaxBytes) {
                        note = value
                        error = null
                    } else {
                        error = tooLargeText
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.dsh_feedback_note_hint)) },
                minLines = 2,
                maxLines = 4
            )
            error?.let {
                Text(
                    text = it,
                    modifier = Modifier.padding(top = 4.dp),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.align(Alignment.End)) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.dsh_cancel))
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (note.length > FeedbackNoteMaxBytes) {
                            error = tooLargeText
                        } else {
                            onSave(note.trim())
                        }
                    }
                ) {
                    Text(stringResource(R.string.dsh_confirm))
                }
            }
        }
    }
}

private fun List<FeedbackItem>.find(messageId: String): FeedbackItem? =
    firstOrNull { it.messageId == messageId }
