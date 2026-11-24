package com.wanbaohe.a2ui.workbench

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlexBox
import androidx.compose.foundation.layout.FlexWrap
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.wanbaohe.a2ui.official.A2uiSessionEvent
import com.wanbaohe.a2ui.official.OfficialA2uiSession
import com.wanbaohe.core.ui.widget.enhanced.EnhancedAlertDialog
import com.wanbaohe.core.ui.widget.glass.GlassOutlinedTextField
import com.wanbaohe.core.ui.widget.glass.GlassTextButton
import java.text.DateFormat
import java.util.Date

@Composable
internal fun WorkbenchSamplesDialog(onChoose: (WorkbenchSample) -> Unit, onDismiss: () -> Unit) {
    EnhancedAlertDialog(
        visible = true, onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.a2wb_samples)) },
        confirmButton = { CloseButton(onDismiss) },
        text = {
            Column {
                WorkbenchSample.entries.forEach { item ->
                    GlassTextButton(onClick = { onChoose(item) }) { Text(stringResource(item.title)) }
                }
            }
        },
    )
}

@Composable
internal fun WorkbenchHistoryDialog(
    snapshots: List<WorkbenchSnapshot>,
    component: A2uiWorkbenchComponent,
    onOpen: (WorkbenchSnapshot) -> Unit,
    onDismiss: () -> Unit,
) {
    var renaming by remember { mutableStateOf<WorkbenchSnapshot?>(null) }
    var deleting by remember { mutableStateOf<(() -> Unit)?>(null) }
    if (renaming == null && deleting == null) EnhancedAlertDialog(
        visible = true, onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.a2wb_history)) },
        confirmButton = { CloseButton(onDismiss) },
        dismissButton = if (snapshots.isEmpty()) null else {
            {
                GlassTextButton(onClick = { deleting = component::clearHistory }) {
                    Text(stringResource(R.string.a2wb_clear_history))
                }
            }
        },
        text = {
            Column(Modifier.heightIn(max = 400.dp).verticalScroll(rememberScrollState())) {
                if (snapshots.isEmpty()) Text(stringResource(R.string.a2wb_empty_history))
                snapshots.forEach { snapshot ->
                    val title = snapshot.title.ifBlank { DateFormat.getDateTimeInstance().format(Date(snapshot.id)) }
                    FlexBox(config = { wrap(FlexWrap.Wrap) }) {
                        GlassTextButton(onClick = { onOpen(snapshot) }) { Text(title) }
                        GlassTextButton(onClick = { renaming = snapshot }) { Text(stringResource(R.string.a2wb_rename)) }
                        GlassTextButton(onClick = { deleting = { component.delete(snapshot.id) } }) {
                            Text(stringResource(R.string.a2wb_delete))
                        }
                    }
                }
            }
        },
    )
    renaming?.let { snapshot ->
        WorkbenchTitleDialog(snapshot.title, R.string.a2wb_rename, { renaming = null }) { title ->
            component.rename(snapshot.id, title)
            renaming = null
        }
    }
    deleting?.let { delete ->
        EnhancedAlertDialog(
            visible = true,
            onDismissRequest = { deleting = null },
            title = { Text(stringResource(R.string.a2wb_confirm_delete)) },
            dismissButton = { CloseButton { deleting = null } },
            confirmButton = {
                GlassTextButton(onClick = { delete(); deleting = null }) { Text(stringResource(R.string.a2wb_delete)) }
            },
        )
    }
}

@Composable
internal fun WorkbenchTitleDialog(
    initial: String,
    confirmLabel: Int,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var title by remember(initial) { mutableStateOf(initial) }
    EnhancedAlertDialog(
        visible = true, onDismissRequest = onDismiss,
        title = { Text(stringResource(confirmLabel)) },
        dismissButton = { CloseButton(onDismiss) },
        confirmButton = {
            GlassTextButton(onClick = { onConfirm(title) }) { Text(stringResource(confirmLabel)) }
        },
        text = {
            GlassOutlinedTextField(
                value = title, onValueChange = { title = it.take(80) }, singleLine = true,
                label = { Text(stringResource(R.string.a2wb_snapshot_name)) },
            )
        },
    )
}

@Composable
internal fun WorkbenchEvents(session: OfficialA2uiSession) {
    val events by session.events.collectAsState()
    var open by remember { mutableStateOf(false) }
    GlassTextButton(onClick = { open = true }) { Text(stringResource(R.string.a2wb_event_count, events.size)) }
    if (open) EnhancedAlertDialog(
        visible = true, onDismissRequest = { open = false },
        title = { Text(stringResource(R.string.a2wb_events)) },
        confirmButton = { CloseButton { open = false } },
        text = {
            Column(Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState())) {
                if (events.isEmpty()) Text(stringResource(R.string.a2wb_empty_events))
                events.asReversed().forEach { event ->
                    val detail = when (event) {
                        is A2uiSessionEvent.Action -> buildString {
                            appendLine("${event.name} · ${event.surfaceId}/${event.componentId}")
                            appendLine(event.context)
                            event.dataModel?.let { append(it) }
                        }
                        is A2uiSessionEvent.Error -> "${event.code} ${event.path.orEmpty()}\n${event.detail}"
                        is A2uiSessionEvent.UrlRequested -> stringResource(R.string.a2wb_url_blocked, event.url)
                    }
                    Text(detail.take(4_000), fontFamily = FontFamily.Monospace)
                }
            }
        },
    )
}

@Composable
private fun CloseButton(onClose: () -> Unit) {
    GlassTextButton(onClick = onClose) { Text(stringResource(R.string.a2wb_close)) }
}
