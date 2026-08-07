package com.wanbaohe.schedule.screen

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.shifenmiao.common.ui.BaseScreen
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.utils.provider.LocalComponentActivity
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedButton
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.wanbaohe.schedule.R
import com.wanbaohe.schedule.component.ScheduleComponent
import com.wanbaohe.schedule.integration.SystemCalendarContractExporter
import com.wanbaohe.schedule.integration.SystemCalendarProviderAdapter
import com.wanbaohe.schedule.model.ScheduleEvent
import com.wanbaohe.schedule.model.ScheduleProviderType
import com.wanbaohe.schedule.model.ScheduleSyncStatus
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCalendar
import com.t8rin.imagetoolbox.core.resources.icons.line.LineEventAvailable
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCloudDone
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSyncProblem

@Composable
fun ScheduleScreen(component: ScheduleComponent) {
    val state by component.uiState.collectAsState()
    val context = LocalContext.current
    val activity = LocalComponentActivity.current
    val calendarPermissions = remember {
        arrayOf(
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.WRITE_CALENDAR,
        )
    }

    var isCalendarPermissionGranted by remember {
        mutableStateOf(SystemCalendarProviderAdapter.hasCalendarPermissions(context))
    }
    var isCalendarPermissionPermanentlyDenied by remember { mutableStateOf(false) }
    var pendingWriteEvent by remember { mutableStateOf<ScheduleEvent?>(null) }
    var calendarPickerEvent by remember { mutableStateOf<ScheduleEvent?>(null) }
    var writableCalendars by remember {
        mutableStateOf<List<SystemCalendarProviderAdapter.WritableCalendar>>(emptyList())
    }

    val quickEventTitle = stringResource(R.string.schedule_quick_event_title)
    val focusEventTitle = stringResource(R.string.schedule_focus_event_title)
    val linkedTaskDescription = state.linkedTaskId?.let {
        stringResource(R.string.schedule_linked_task_description, it)
    }
    val calendarUnavailableMessage = stringResource(R.string.schedule_system_calendar_unavailable)
    val writableCalendarUnavailableMessage = stringResource(R.string.schedule_no_writable_calendar)
    val writeSuccessMessage = stringResource(R.string.schedule_saved_to_system_calendar)
    val writeFailedMessage = stringResource(R.string.schedule_save_to_system_calendar_failed)
    val calendarPermissionDeniedMessage = stringResource(R.string.schedule_calendar_permission_denied)

    fun openCalendarPicker(event: ScheduleEvent) {
        val calendars = SystemCalendarProviderAdapter.queryWritableCalendars(context)
        if (calendars.isEmpty()) {
            AppToastHost.showToast(writableCalendarUnavailableMessage)
            return
        }
        writableCalendars = calendars
        calendarPickerEvent = event
    }

    fun insertIntoSystemCalendar(
        event: ScheduleEvent,
        calendar: SystemCalendarProviderAdapter.WritableCalendar,
    ) {
        component.saveEventToSystemCalendar(
            eventId = event.id,
            calendarId = calendar.id,
        ) { result ->
            result
                .onSuccess {
                    AppToastHost.showToast(writeSuccessMessage)
                }
                .onFailure {
                    AppToastHost.showToast(writeFailedMessage)
                }
        }
    }

    val calendarPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = calendarPermissions.all { permission -> result[permission] == true }
        isCalendarPermissionGranted = granted
        if (granted) {
            pendingWriteEvent?.let(::openCalendarPicker)
        } else {
            isCalendarPermissionPermanentlyDenied = calendarPermissions.any { permission ->
                !ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
            }
            AppToastHost.showToast(calendarPermissionDeniedMessage)
        }
        pendingWriteEvent = null
    }

    LaunchedEffect(Unit) {
        isCalendarPermissionGranted = SystemCalendarProviderAdapter.hasCalendarPermissions(context)
    }

    BaseScreen(
        title = stringResource(R.string.schedule_screen_title),
        onGoBack = component.onGoBack,
        isShowDefaultActions = true,
        showNavigationBarsPadding = false,
        supportGlassEffect = true,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                SyncStatusCard(
                    linkedTaskId = state.linkedTaskId,
                    eventCount = state.events.size,
                    syncTokenReady = !state.googleSyncState?.syncToken.isNullOrBlank(),
                    lastError = state.googleSyncState?.lastErrorMessage,
                )
            }

            item {
                ActionCard(
                    hasFocusDate = state.focusDateMillis != null,
                    onCreateQuickEvent = {
                        component.createQuickEvent(
                            title = quickEventTitle,
                            description = linkedTaskDescription,
                        )
                    },
                    onCreateFocusEvent = {
                        component.createTodayFocusEvent(
                            title = focusEventTitle
                        )
                    },
                    onOpenTodo = component::openTodoList,
                    onOpenCalendar = component::openChineseCalendar,
                )
            }

            item {
                Text(
                    text = stringResource(R.string.schedule_event_section_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            if (state.events.isEmpty()) {
                item {
                    EmptyStateCard()
                }
            } else {
                items(state.events, key = ScheduleEvent::id) { event ->
                    EventCard(
                        event = event,
                        onSaveToSystemCalendar = {
                            if (isCalendarPermissionGranted) {
                                openCalendarPicker(event)
                            } else {
                                pendingWriteEvent = event
                                calendarPermissionLauncher.launch(calendarPermissions)
                            }
                        },
                        onExportToSystemCalendar = {
                            val exported = SystemCalendarContractExporter.exportEvent(
                                context = context,
                                event = event,
                            )
                            if (!exported) {
                                AppToastHost.showToast(calendarUnavailableMessage)
                            }
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    val selectedEvent = calendarPickerEvent
    if (selectedEvent != null) {
        AlertDialog(
            onDismissRequest = { calendarPickerEvent = null },
            title = {
                Text(text = stringResource(R.string.schedule_choose_calendar_title))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    writableCalendars.forEach { calendar ->
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    insertIntoSystemCalendar(selectedEvent, calendar)
                                    calendarPickerEvent = null
                                },
                            shape = MaterialTheme.shapes.medium,
                            borderWidth = 0.dp,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = calendar.displayName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    calendar.accountName?.takeIf { it.isNotBlank() }?.let {
                                        Text(
                                            text = it,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                                if (calendar.isPrimary) {
                                    AssistChip(
                                        onClick = {},
                                        enabled = false,
                                        label = { Text(text = stringResource(R.string.schedule_calendar_primary_badge)) }
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                OutlinedButton(onClick = { calendarPickerEvent = null }) {
                    Text(text = stringResource(R.string.schedule_dialog_close))
                }
            }
        )
    }

    if (isCalendarPermissionPermanentlyDenied) {
        AlertDialog(
            onDismissRequest = { isCalendarPermissionPermanentlyDenied = false },
            title = {
                Text(text = stringResource(R.string.schedule_permission_dialog_title))
            },
            text = {
                Text(text = stringResource(R.string.schedule_permission_dialog_message))
            },
            confirmButton = {
                OutlinedButton(
                    onClick = {
                        isCalendarPermissionPermanentlyDenied = false
                        activity.startActivity(
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", activity.packageName, null)
                            }
                        )
                    }
                ) {
                    Text(text = stringResource(R.string.schedule_open_settings))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { isCalendarPermissionPermanentlyDenied = false }) {
                    Text(text = stringResource(R.string.schedule_dialog_close))
                }
            }
        )
    }
}

@Composable
private fun SyncStatusCard(
    linkedTaskId: String?,
    eventCount: Int,
    syncTokenReady: Boolean,
    lastError: String?,
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        borderWidth = 0.dp,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.schedule_sync_card_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                AssistChip(
                    onClick = {},
                    enabled = false,
                    label = {
                        Text(
                            text = if (syncTokenReady) {
                                stringResource(R.string.schedule_sync_ready)
                            } else {
                                stringResource(R.string.schedule_sync_not_connected)
                            }
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = if (syncTokenReady) com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCloudDone else com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSyncProblem,
                            contentDescription = null,
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors()
                )
            }

            Text(
                text = stringResource(R.string.schedule_sync_card_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AssistChip(onClick = {}, enabled = false, label = {
                    Text(stringResource(R.string.schedule_event_count_label, eventCount))
                })
                linkedTaskId?.let {
                    AssistChip(onClick = {}, enabled = false, label = {
                        Text(stringResource(R.string.schedule_linked_task_chip))
                    })
                }
            }

            if (!lastError.isNullOrBlank()) {
                Text(
                    text = stringResource(R.string.schedule_last_error_label, lastError),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun ActionCard(
    hasFocusDate: Boolean,
    onCreateQuickEvent: () -> Unit,
    onCreateFocusEvent: () -> Unit,
    onOpenTodo: () -> Unit,
    onOpenCalendar: () -> Unit,
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        borderWidth = 0.dp,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.schedule_action_card_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            EnhancedButton(
                onClick = onCreateQuickEvent,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineEventAvailable, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(R.string.schedule_action_create_local))
            }
            if (hasFocusDate) {
                OutlinedButton(
                    onClick = onCreateFocusEvent,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineEventAvailable, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(R.string.schedule_action_create_focus))
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenTodo,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(text = stringResource(R.string.schedule_action_open_todo))
                }
                OutlinedButton(
                    onClick = onOpenCalendar,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCalendar, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(R.string.schedule_action_open_calendar))
                }
            }
        }
    }
}

@Composable
private fun EmptyStateCard() {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        borderWidth = 0.dp,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.schedule_empty_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(R.string.schedule_empty_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EventCard(
    event: ScheduleEvent,
    onSaveToSystemCalendar: () -> Unit,
    onExportToSystemCalendar: () -> Unit,
) {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    val zoneId = runCatching { ZoneId.of(event.timeZoneId) }.getOrDefault(ZoneId.systemDefault())
    val startText = Instant.ofEpochMilli(event.startUtcMillis).atZone(zoneId).format(formatter)
    val endText = Instant.ofEpochMilli(event.endUtcMillis).atZone(zoneId).format(formatter)

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        borderWidth = 0.dp,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    event.description?.takeIf { it.isNotBlank() }?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                AssistChip(
                    onClick = {},
                    enabled = false,
                    label = {
                        Text(
                            text = when (event.providerType) {
                                ScheduleProviderType.LOCAL -> stringResource(R.string.schedule_provider_local)
                                ScheduleProviderType.SYSTEM_CALENDAR -> stringResource(R.string.schedule_provider_system)
                                ScheduleProviderType.GOOGLE_CALENDAR -> stringResource(R.string.schedule_provider_google)
                            }
                        )
                    }
                )
            }

            Text(
                text = stringResource(R.string.schedule_event_time_range, startText, endText),
                style = MaterialTheme.typography.bodyMedium,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = {}, enabled = false, label = {
                    Text(
                        text = when (event.syncStatus) {
                            ScheduleSyncStatus.LOCAL_ONLY -> stringResource(R.string.schedule_status_local_only)
                            ScheduleSyncStatus.PENDING_UPLOAD -> stringResource(R.string.schedule_status_pending_upload)
                            ScheduleSyncStatus.SYNCED -> stringResource(R.string.schedule_status_synced)
                            ScheduleSyncStatus.PENDING_DELETE -> stringResource(R.string.schedule_status_pending_delete)
                            ScheduleSyncStatus.CONFLICT -> stringResource(R.string.schedule_status_conflict)
                            ScheduleSyncStatus.FAILED -> stringResource(R.string.schedule_status_failed)
                        }
                    )
                })
                event.linkedTaskId?.let {
                    AssistChip(onClick = {}, enabled = false, label = {
                        Text(stringResource(R.string.schedule_linked_task_badge))
                    })
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onSaveToSystemCalendar,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCalendar,
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(R.string.schedule_save_to_system_calendar))
                }

                OutlinedButton(
                    onClick = onExportToSystemCalendar,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCalendar,
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(R.string.schedule_export_to_system_calendar))
                }
            }
        }
    }
}


