package com.wanbaohe.schedule.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.ui.BaseScreen
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.core.ui.widget.glass.GlassCard
import com.wanbaohe.core.ui.widget.system.OnePrimaryButton
import com.wanbaohe.core.ui.widget.system.OneSecondaryButton
import com.wanbaohe.schedule.R
import com.wanbaohe.schedule.component.ScheduleComponent
import com.wanbaohe.schedule.integration.SystemCalendarContractExporter
import com.wanbaohe.schedule.model.ScheduleEvent
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Composable
fun ScheduleScreen(component: ScheduleComponent) {
    val state by component.uiState.collectAsState()
    val context = LocalContext.current
    val zoneId = remember { ZoneId.systemDefault() }
    val focusDate = state.focusDateMillis?.let {
        Instant.ofEpochMilli(it).atZone(zoneId).toLocalDate()
    }
    val rows = remember(state.events, focusDate, state.linkedTaskId, zoneId, state.isLoading) {
        if (state.isLoading) {
            emptyList()
        } else {
            buildAgendaRows(
                events = state.events,
                focusDate = focusDate,
                linkedTaskId = state.linkedTaskId,
                zoneId = zoneId,
            )
        }
    }
    val listState = rememberLazyListState()
    val focusIndex = rows.indexOfFirst { row ->
        when (row) {
            is AgendaRow.Header -> row.isFocus
            is AgendaRow.Event -> row.highlighted
        }
    }
    val calendarUnavailableMessage = stringResource(R.string.schedule_system_calendar_unavailable)

    LaunchedEffect(focusIndex, state.isLoading) {
        if (!state.isLoading && focusIndex >= 0) {
            listState.scrollToItem(focusIndex + 1)
        }
    }

    BaseScreen(
        title = stringResource(R.string.schedule_screen_title),
        onGoBack = component.onGoBack,
        isShowDefaultActions = true,
        showNavigationBarsPadding = false,
        supportGlassEffect = true,
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(key = "top-spacer") {
                Spacer(modifier = Modifier.height(8.dp))
            }
            if (!state.isLoading && rows.isEmpty()) {
                item(key = "empty") {
                    EmptyAgendaCard()
                }
            } else {
                agendaItems(
                    rows = rows,
                    zoneId = zoneId,
                    onAddToSystemCalendar = { event ->
                        val exported = SystemCalendarContractExporter.exportEvent(
                            context = context,
                            event = event,
                        )
                        if (!exported) {
                            AppToastHost.showToast(calendarUnavailableMessage)
                        }
                    },
                    onOpenTask = component::openTask,
                )
            }
            item(key = "bottom-spacer") {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

private fun LazyListScope.agendaItems(
    rows: List<AgendaRow>,
    zoneId: ZoneId,
    onAddToSystemCalendar: (ScheduleEvent) -> Unit,
    onOpenTask: (String) -> Unit,
) {
    items(rows, key = AgendaRow::key) { row ->
        when (row) {
            is AgendaRow.Header -> DayHeader(date = row.date, isEmpty = row.isEmpty)
            is AgendaRow.Event -> EventCard(
                event = row.event,
                highlighted = row.highlighted,
                zoneId = zoneId,
                onAddToSystemCalendar = { onAddToSystemCalendar(row.event) },
                onOpenTask = row.event.linkedTaskId?.let { taskId ->
                    { onOpenTask(taskId) }
                },
            )
        }
    }
}

@Composable
private fun DayHeader(date: LocalDate, isEmpty: Boolean) {
    val today = LocalDate.now()
    val dateText = date.format(
        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault()),
    )
    val title = when (date) {
        today -> stringResource(R.string.schedule_day_today, dateText)
        today.plusDays(1) -> stringResource(R.string.schedule_day_tomorrow, dateText)
        today.minusDays(1) -> stringResource(R.string.schedule_day_yesterday, dateText)
        else -> dateText
    }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        if (isEmpty) {
            Text(
                text = stringResource(R.string.schedule_day_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EmptyAgendaCard() {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        borderWidth = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
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
    highlighted: Boolean,
    zoneId: ZoneId,
    onAddToSystemCalendar: () -> Unit,
    onOpenTask: (() -> Unit)?,
) {
    val note = event.description?.takeIf { text ->
        val taskId = event.linkedTaskId
        text.isNotBlank() && (taskId == null || !text.contains(taskId))
    }
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        borderWidth = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (highlighted) {
                Text(
                    text = stringResource(R.string.schedule_current_task),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Text(
                text = event.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = eventTimeText(event, zoneId),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            note?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            EventActions(
                onAddToSystemCalendar = onAddToSystemCalendar,
                onOpenTask = onOpenTask,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EventActions(
    onAddToSystemCalendar: () -> Unit,
    onOpenTask: (() -> Unit)?,
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OneSecondaryButton(
            text = stringResource(R.string.schedule_export_to_system_calendar),
            onClick = onAddToSystemCalendar,
        )
        if (onOpenTask != null) {
            OnePrimaryButton(
                text = stringResource(R.string.schedule_open_task),
                onClick = onOpenTask,
            )
        }
    }
}

@Composable
private fun eventTimeText(event: ScheduleEvent, zoneId: ZoneId): String {
    if (event.isAllDay) return stringResource(R.string.schedule_all_day)
    val zone = runCatching { ZoneId.of(event.timeZoneId) }.getOrDefault(zoneId)
    val formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(Locale.getDefault())
    val start = Instant.ofEpochMilli(event.startUtcMillis).atZone(zone).format(formatter)
    val end = Instant.ofEpochMilli(event.endUtcMillis).atZone(zone).format(formatter)
    return stringResource(R.string.schedule_event_time_range, start, end)
}

private sealed interface AgendaRow {
    val key: String

    data class Header(
        val date: LocalDate,
        val isFocus: Boolean,
        val isEmpty: Boolean,
    ) : AgendaRow {
        override val key: String = "day-$date"
    }

    data class Event(
        val event: ScheduleEvent,
        val highlighted: Boolean,
    ) : AgendaRow {
        override val key: String = "event-${event.id}"
    }
}

private fun buildAgendaRows(
    events: List<ScheduleEvent>,
    focusDate: LocalDate?,
    linkedTaskId: String?,
    zoneId: ZoneId,
): List<AgendaRow> {
    val byDate = events.groupBy { event ->
        Instant.ofEpochMilli(event.startUtcMillis).atZone(zoneId).toLocalDate()
    }
    val dates = byDate.keys.toMutableSet()
    if (focusDate != null) dates += focusDate
    return dates.sorted().flatMap { date ->
        val dayEvents = byDate[date].orEmpty().sortedBy { it.startUtcMillis }
        buildList {
            add(
                AgendaRow.Header(
                    date = date,
                    isFocus = date == focusDate,
                    isEmpty = dayEvents.isEmpty(),
                )
            )
            dayEvents.forEach { event ->
                add(
                    AgendaRow.Event(
                        event = event,
                        highlighted = !linkedTaskId.isNullOrBlank() && event.linkedTaskId == linkedTaskId,
                    )
                )
            }
        }
    }
}
