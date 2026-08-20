package com.wanbaohe.dsh.screen

import android.text.format.DateFormat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.base.provider.LocalDataDraftHelper
import com.shifenmiao.model.ListItemType
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.utils.navigation.LocalOnNavigate
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.wanbaohe.dsh.R
import com.wanbaohe.dsh.screen.node.formatDurationMs
import com.wanbaohe.dsh.session.ChatNode
import com.wanbaohe.dsh.session.FeedbackNoteTooLargeException
import com.wanbaohe.dsh.session.FeedbackStore
import com.wanbaohe.dsh.session.FeedbackStoreException
import com.wanbaohe.dsh.session.FeedbackVersionConflictException
import com.wanbaohe.dsh.wire.model.FeedbackItem
import com.wanbaohe.dsh.wire.model.FeedbackNoteMaxBytes
import com.wanbaohe.dsh.wire.model.FeedbackRatingNegative
import com.wanbaohe.dsh.wire.model.FeedbackRatingPositive
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 消息反馈行(对齐 Flutter feedback_row.dart,DSH-PROTOCOL §9 messageFeedback 契约;
 * 协议字段与 web/packages/feedback/message-feedback spec 核对一致):
 * - 👍/👎/备注 + 复制/存笔记 常显小图标钮(32dp 圆形 ripple 触控区,plain Icon);
 *   乐观更新(点击立即高亮,失败回滚 + 内联报错);已评高亮;再点同一侧 = 撤回(delete);
 *   切换另一侧保留已有 note
 * - 备注是评分条目的属性(put 必须带 rating);未评分保存 → 内联提示先评分
 * - CAS:ifVersion 取条目当前 version;version-conflict → 权威条目直接对账
 * - 请求在途禁用;错误内联展示(不弹横幅)
 * - 顶部统计行(对齐 web MessageIconActions 的 clock + Ran for/TTFT/tok/s):
 *   消息完成时间(epoch ms)· 本轮用时 · 首 token 延迟 · 输出速度,
 *   数据来自 EventNodes 轮末 Stats 节点(turn/end 记账),缺项不展示
 */
@Composable
fun FeedbackRow(
    store: FeedbackStore,
    sessionId: String,
    messageId: String,
    modifier: Modifier = Modifier,
    /** 消息纯文本(复制/存笔记用;空串时两个按钮 no-op) */
    messageText: String = "",
    messageTime: Double? = null,
    stats: ChatNode.Stats? = null
) {
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current
    val dataDraftHelper = LocalDataDraftHelper.current
    val navigator = LocalOnNavigate.current
    val rateFirstText = stringResource(R.string.dsh_feedback_rate_first)
    var item by remember(messageId) { mutableStateOf(store.itemsFor(sessionId).find(messageId)) }
    var busy by remember(messageId) { mutableStateOf(false) }
    var error by remember(messageId) { mutableStateOf<String?>(null) }
    var noteEditorOpen by remember(messageId) { mutableStateOf(false) }
    // 复制成功短暂变对勾(web 同款 1s 窗口)
    var copied by remember(messageId) { mutableStateOf(false) }
    LaunchedEffect(copied) {
        if (copied) {
            delay(1000)
            copied = false
        }
    }

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

    /** 乐观更新 + 失败回滚([previous] = 动作前条目,失败时恢复) */
    fun runPut(rating: String, note: String?, previous: FeedbackItem?) {
        busy = true
        error = null
        scope.launch {
            try {
                item = store.put(
                    sessionId,
                    messageId,
                    rating,
                    note = note,
                    ifVersion = previous?.version
                )
            } catch (e: FeedbackVersionConflictException) {
                // 直接对账:权威条目覆盖本地(并发删除 → 未评)
                item = e.authoritative
            } catch (e: FeedbackNoteTooLargeException) {
                item = previous
                error = e.message
            } catch (e: FeedbackStoreException) {
                item = previous
                error = e.message ?: e.code
            } catch (e: Throwable) {
                item = previous
                error = e.message
            }
            busy = false
        }
    }

    fun runDelete(previous: FeedbackItem?) {
        busy = true
        error = null
        scope.launch {
            try {
                store.delete(sessionId, messageId, ifVersion = previous?.version)
            } catch (e: FeedbackStoreException) {
                item = previous
                error = e.message ?: e.code
            } catch (e: Throwable) {
                item = previous
                error = e.message
            }
            busy = false
        }
    }

    /** 评分:乐观更新(立即高亮/撤高亮,失败回滚);切换另一侧保留已有 note */
    fun rate(rating: String) {
        if (busy) return
        val previous = item
        if (previous != null && previous.rating == rating) {
            item = null
            runDelete(previous)
        } else {
            item = FeedbackItem(
                messageId = messageId,
                rating = rating,
                note = previous?.note,
                version = previous?.version
            )
            runPut(rating, note = previous?.note, previous = previous)
        }
    }

    Column(modifier = modifier.padding(top = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // 复制消息文本(成功短暂变对勾)
            FeedbackAction(
                icon = if (copied) Icons.Outlined.Check else Icons.Outlined.ContentCopy,
                contentDescription = stringResource(R.string.dsh_copy),
                tint = if (copied) {
                    AppTheme.colors.getPrimaryColor()
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                }
            ) {
                if (messageText.isBlank() || copied) return@FeedbackAction
                clipboard.setText(AnnotatedString(messageText))
                copied = true
            }
            // 存到笔记本:建 NOTE 草稿 → 跳转新建笔记页(AI 聊天同款链路)
            FeedbackAction(
                icon = Icons.Outlined.BookmarkAdd,
                contentDescription = stringResource(R.string.dsh_feedback_save_note),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            ) {
                if (messageText.isBlank()) return@FeedbackAction
                scope.launch {
                    val draftId = dataDraftHelper.createDraft(
                        draftType = ListItemType.NOTE.id,
                        data = messageText
                    )
                    navigator(Screen.CreateNote(draftId = draftId))
                }
            }
            val positiveSelected = item?.rating == FeedbackRatingPositive
            FeedbackAction(
                icon = if (positiveSelected) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                contentDescription = stringResource(R.string.dsh_feedback_positive),
                tint = if (positiveSelected) {
                    AppTheme.colors.getPrimaryColor()
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                },
                enabled = !busy
            ) {
                rate(FeedbackRatingPositive)
            }
            val negativeSelected = item?.rating == FeedbackRatingNegative
            FeedbackAction(
                icon = if (negativeSelected) Icons.Filled.ThumbDown else Icons.Outlined.ThumbDown,
                contentDescription = stringResource(R.string.dsh_feedback_negative),
                tint = if (negativeSelected) {
                    AppTheme.colors.getPrimaryColor()
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                },
                enabled = !busy
            ) {
                rate(FeedbackRatingNegative)
            }
            val hasNote = !item?.note.isNullOrEmpty()
            FeedbackAction(
                icon = Icons.Outlined.Notes,
                contentDescription = stringResource(R.string.dsh_feedback_note),
                tint = if (hasNote) {
                    AppTheme.colors.getPrimaryColor()
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                },
                enabled = !busy
            ) {
                noteEditorOpen = true
            }
            // 统计行与图标同一行:右侧「时间 · 用时 · 首 token · tok/s」(空间不足截断)
            val metaText = buildMessageMetaText(messageTime, stats)
            if (metaText.isNotEmpty()) {
                Text(
                    text = metaText,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
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
                    runPut(rating, note = note.takeIf { it.isNotEmpty() }, previous = item)
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
            GlassOutlinedTextField(
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

/** 反馈行小图标钮:plain Icon + 32dp 圆形 ripple 触控区(17dp 图标;替代 IconButton 的 48dp 触控 padding) */
@Composable
private fun FeedbackAction(
    icon: ImageVector,
    contentDescription: String,
    tint: Color,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(17.dp),
            tint = tint
        )
    }
}

private fun List<FeedbackItem>.find(messageId: String): FeedbackItem? =
    firstOrNull { it.messageId == messageId }

/**
 * 统计行文案(对齐 web MessageIconActions 的 clock + ranFor/TTFT/tok-s):
 * 消息完成时间(epoch ms,本地化日期格式)· 本轮用时 · 首 token 延迟 · 输出速度;
 * 缺项跳过,全缺返回空串(不渲染)。
 */
@Composable
private fun buildMessageMetaText(messageTime: Double?, stats: ChatNode.Stats?): String {
    val parts = ArrayList<String>(4)
    messageTime?.let { parts.add(formatMessageClock(it)) }
    stats?.let {
        parts.add(stringResource(R.string.dsh_msg_meta_run, formatDurationMs(it.runMs)))
        it.ttftMs?.let { ttft ->
            parts.add(stringResource(R.string.dsh_msg_meta_ttft, formatDurationMs(ttft)))
        }
        it.tokensPerSecond?.let { tps ->
            parts.add(stringResource(R.string.dsh_stats_tps, String.format(Locale.US, "%.1f", tps)))
        }
    }
    return parts.joinToString(" · ")
}

/** 消息完成时间(epoch ms)→ 本地化「月日 时分」(zh: 8月19日 12:53;en: Aug 19, 12:53) */
private fun formatMessageClock(timeEpochMs: Double): String {
    val pattern = DateFormat.getBestDateTimePattern(Locale.getDefault(), "MMMdHHmm")
    return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(timeEpochMs.toLong()))
}
