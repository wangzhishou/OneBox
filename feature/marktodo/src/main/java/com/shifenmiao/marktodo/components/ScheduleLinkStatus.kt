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

package com.shifenmiao.marktodo.components

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.shifenmiao.marktodo.R
import com.shifenmiao.model.event.PermissionRequest
import com.wanbaohe.core.ui.utils.helper.ContextUtils.requestPermissions
import com.wanbaohe.core.ui.utils.provider.LocalComponentActivity
import com.wanbaohe.core.ui.widget.system.OneBoxDesignSystem
import com.wanbaohe.core.ui.widget.system.OnePrimaryButton
import com.wanbaohe.core.ui.widget.system.OneSecondaryButton
import com.wanbaohe.schedule.model.LinkedScheduleSnapshot
import com.wanbaohe.schedule.reminder.ScheduleReminders
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/**
 * 截止日期下面的日程状态：没日期只提示，已加入则留在本页显示结果。
 */
@Composable
fun ScheduleLinkStatus(
    dueDateMillis: Long?,
    hasDueTime: Boolean,
    linked: LinkedScheduleSnapshot?,
    joinOnSave: Boolean,
    joining: Boolean,
    explainPendingSave: Boolean = true,
    onJoin: () -> Unit,
    onView: () -> Unit,
    onRemove: () -> Unit,
    onCancelJoinOnSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val notificationsEnabled = rememberNotificationsEnabled()
    val requestNotifications = rememberNotificationPermissionRequest()
    val matches = linked != null && dueDateMillis != null && ScheduleReminders.matchesDue(
        startUtcMillis = linked.startUtcMillis,
        isAllDay = linked.isAllDay,
        dueAtMillis = dueDateMillis,
        hasDueTime = hasDueTime,
    )
    val reminds = linked != null && ScheduleReminders.willRemind(
        startUtcMillis = linked.startUtcMillis,
        isAllDay = linked.isAllDay,
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
    ) {
        when {
            dueDateMillis == null && linked == null -> {
                Hint(stringResource(R.string.schedule_hint_set_due))
            }
            linked == null && joinOnSave && dueDateMillis != null -> {
                Text(
                    text = stringResource(R.string.schedule_pending_join),
                    style = MaterialTheme.typography.bodyMedium,
                )
                ActionRow {
                    OneSecondaryButton(
                        text = stringResource(R.string.schedule_pending_cancel),
                        onClick = onCancelJoinOnSave,
                    )
                }
                if (!notificationsEnabled) {
                    Hint(stringResource(R.string.schedule_notification_off))
                }
            }
            linked == null && dueDateMillis != null -> {
                ActionRow {
                    OnePrimaryButton(
                        text = stringResource(
                            if (joining) R.string.schedule_joining else R.string.schedule_action_join,
                        ),
                        onClick = {
                            requestNotifications()
                            onJoin()
                        },
                        enabled = !joining,
                    )
                }
            }
            linked != null -> {
                val whenText = formatScheduleWhen(linked.startUtcMillis, linked.isAllDay)
                Text(
                    text = stringResource(
                        if (reminds) R.string.schedule_status_joined_remind else R.string.schedule_status_joined,
                        whenText,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (explainPendingSave && dueDateMillis == null) {
                    Hint(stringResource(R.string.schedule_will_remove))
                } else if (explainPendingSave && !matches) {
                    Hint(stringResource(R.string.schedule_will_update))
                } else if (reminds && !notificationsEnabled) {
                    Hint(stringResource(R.string.schedule_notification_off))
                }
                ActionRow {
                    OneSecondaryButton(
                        text = stringResource(R.string.schedule_action_remove),
                        onClick = onRemove,
                    )
                    OnePrimaryButton(
                        text = stringResource(R.string.schedule_action_view),
                        onClick = onView,
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionRow(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(
            OneBoxDesignSystem.compactSpacing,
            Alignment.End,
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        content()
    }
}

@Composable
private fun Hint(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun formatScheduleWhen(startUtcMillis: Long, isAllDay: Boolean): String {
    val zoned = Instant.ofEpochMilli(startUtcMillis).atZone(ZoneId.systemDefault())
    val locale = Locale.getDefault()
    return if (isAllDay) {
        val date = zoned.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale))
        stringResource(R.string.schedule_when_all_day, date)
    } else {
        zoned.format(
            DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT).withLocale(locale),
        )
    }
}

@Composable
private fun rememberNotificationsEnabled(): Boolean {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var enabled by remember {
        mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled())
    }
    DisposableEffect(lifecycle, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                enabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    return enabled
}

@Composable
private fun rememberNotificationPermissionRequest(): () -> Unit {
    val context = LocalContext.current
    val activity = LocalComponentActivity.current
    // 走全局申请流程: 先弹用途说明(同屏告知目的)再拉起系统权限框
    return {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !NotificationManagerCompat.from(context).areNotificationsEnabled()
        ) {
            activity.requestPermissions(
                permissions = arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                permissionRequest = PermissionRequest.ALL,
                onSuccess = {},
                onFailed = {}
            )
        }
    }
}
