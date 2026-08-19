package com.wanbaohe.dsh.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.theme.AppTheme
import com.wanbaohe.dsh.R
import com.wanbaohe.dsh.session.QueueStore
import com.wanbaohe.dsh.session.formatJobDuration
import com.wanbaohe.dsh.session.jobElapsedMs
import com.wanbaohe.dsh.wire.model.TaskView
import kotlinx.coroutines.delay

/**
 * jobs 弹层(对齐 Flutter jobs_sheet.dart,DSH-PROTOCOL §4):
 * - 行:上 = label + 状态徽章(detail 有则取代状态词 —— 失败 detail 是唯一可读处),
 *   下 = kind + 耗时
 * - 活跃行耗时每秒走表(弹层关闭即停);排序 = QueueStore 快照序(活跃在前)
 * - 顶栏角标 = running+stopping,为 0 无角标(触发器在 ChatScreen 顶栏)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobsSheet(
    sessionId: String,
    queueStore: QueueStore,
    onDismiss: () -> Unit
) {
    val jobsBySession by queueStore.jobs.collectAsState()
    val jobs = jobsBySession[sessionId].orEmpty()
    // 活跃行耗时每秒走表
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1_000)
            now = System.currentTimeMillis()
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.dsh_jobs_title, jobs.size),
                modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 8.dp),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (jobs.isEmpty()) {
                Text(
                    text = stringResource(R.string.dsh_jobs_empty),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                ) {
                    items(jobs.size, key = { "job_${jobs[it].id}_$it" }) { index ->
                        JobRow(task = jobs[index], now = now)
                    }
                }
            }
        }
    }
}

/** 单行任务:label + 状态徽章 / kind + 耗时 */
@Composable
private fun JobRow(task: TaskView, now: Long) {
    // detail 有则取代状态词(失败 detail 是唯一可读处)
    val statusWord = task.detail ?: task.status.orEmpty()
    val duration = formatJobDuration(jobElapsedMs(task, now))
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = task.label,
                modifier = Modifier.weight(1f),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.width(8.dp))
            JobStatusBadge(text = statusWord, color = jobStatusColor(task))
        }
        Row(
            modifier = Modifier.padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = task.kind,
                modifier = Modifier.weight(1f),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.width(8.dp))
            Text(text = duration, fontSize = 12.sp)
        }
    }
}

/** 状态徽章:小圆角底色 + 状态词(或 detail) */
@Composable
private fun JobStatusBadge(text: String, color: Color) {
    Text(
        text = text,
        modifier = Modifier
            .widthIn(max = 160.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.14f))
            .padding(horizontal = 8.dp, vertical = 2.dp),
        fontSize = 11.sp,
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

/** 徽章色:活跃 running=主色/stopping=tertiary(橙系);终态 success=主色/error 系=红/其余灰 */
@Composable
private fun jobStatusColor(task: TaskView): Color {
    if (task.isActive) {
        return if (task.status == TaskView.StatusStopping) {
            MaterialTheme.colorScheme.tertiary
        } else {
            AppTheme.colors.getPrimaryColor()
        }
    }
    return when (task.status) {
        "success" -> AppTheme.colors.getPrimaryColor()
        "error", "failed", "cancelled" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}
