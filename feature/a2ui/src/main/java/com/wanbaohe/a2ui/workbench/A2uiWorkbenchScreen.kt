package com.wanbaohe.a2ui.workbench

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.ui.BaseScreen
import com.wanbaohe.core.ui.widget.glass.GlassButton
import com.wanbaohe.core.ui.widget.glass.GlassFilterChip
import com.wanbaohe.core.ui.widget.glass.GlassOutlinedTextField
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.core.ui.widget.glass.GlassSurface
import com.wanbaohe.core.ui.widget.glass.GlassTextButton
import kotlinx.coroutines.launch

@Composable
fun A2uiWorkbenchScreen(component: A2uiWorkbenchComponent) {
    val state by component.state.collectAsState()
    var sourceTab by rememberSaveable { mutableStateOf(false) }
    var showHistory by rememberSaveable { mutableStateOf(false) }
    var showSamples by rememberSaveable { mutableStateOf(false) }
    var showSave by rememberSaveable { mutableStateOf(false) }
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    var exportSource by rememberSaveable { mutableStateOf<String?>(null) }
    val importDocument = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) component.importSource(uri)
    }
    val exportDocument = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
        if (uri != null) exportSource?.let { component.exportSource(uri, it) }
        exportSource = null
    }
    BaseScreen(
        title = stringResource(R.string.a2wb_title),
        onGoBack = component::goBack,
        isShowDefaultActions = false,
        modifier = Modifier.imePadding(),
    ) {
        Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            WorkbenchControls(state, component, { sourceTab = false }, { showSamples = true }, { showHistory = true })
            state.error?.let { Text(stringResource(it), color = MaterialTheme.colorScheme.error) }
            if (state.viewingSnapshot) {
                GlassTextButton(onClick = component::returnToDraft) { Text(stringResource(R.string.a2wb_return_draft)) }
            }
            BoxWithConstraints(Modifier.fillMaxWidth().weight(1f)) {
                val wide = maxWidth >= 720.dp
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!wide) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GlassFilterChip(!sourceTab, { sourceTab = false }, { Text(stringResource(R.string.a2wb_preview)) })
                        GlassFilterChip(sourceTab, { sourceTab = true }, { Text(stringResource(R.string.a2wb_source)) })
                    }
                    Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (wide || sourceTab) {
                            WorkbenchEditor(
                                state = state,
                                onEdit = component::edit,
                                onFormat = component::format,
                                onCopy = {
                                    scope.launch {
                                        clipboard.setClipEntry(ClipEntry(android.content.ClipData.newPlainText("A2UI", state.source)))
                                    }
                                },
                                onSave = { showSave = true },
                                onImport = { importDocument.launch(arrayOf("application/json", "text/*", "application/octet-stream", "application/x-ndjson")) },
                                onExport = {
                                    exportSource = state.source
                                    exportDocument.launch("a2ui.txt")
                                },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        if (wide || !sourceTab) WorkbenchPreview(state, Modifier.weight(1f))
                    }
                }
            }
        }
    }
    if (showSamples) WorkbenchSamplesDialog(
        onChoose = {
            component.loadSample(it)
            showSamples = false
            sourceTab = false
        },
        onDismiss = { showSamples = false },
    )
    if (showHistory) WorkbenchHistoryDialog(
        state.snapshots, component,
        onOpen = {
            component.openSnapshot(it.source)
            showHistory = false
            sourceTab = false
        },
        onDismiss = { showHistory = false },
    )
    if (showSave) WorkbenchTitleDialog(
        initial = "",
        confirmLabel = R.string.a2wb_save,
        onDismiss = { showSave = false },
        onConfirm = { component.save(it); showSave = false },
    )
}

@Composable
private fun WorkbenchControls(
    state: WorkbenchState,
    component: A2uiWorkbenchComponent,
    onPreview: () -> Unit,
    onSamples: () -> Unit,
    onHistory: () -> Unit,
) {
    WorkbenchPanel {
        Text(
            stringResource(R.string.a2wb_versions),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            val canRun = state.ready && state.source.isNotBlank()
            GlassButton(onClick = { component.runAll(); onPreview() }, enabled = canRun) {
                Text(stringResource(R.string.a2wb_run))
            }
            GlassTextButton(onClick = { component.step(); onPreview() }, enabled = canRun) {
                Text(stringResource(R.string.a2wb_step))
            }
            GlassTextButton(onClick = component::reset, enabled = state.session != null) {
                Text(stringResource(R.string.a2wb_reset))
            }
            GlassTextButton(onClick = onSamples, enabled = state.ready) { Text(stringResource(R.string.a2wb_samples)) }
            GlassTextButton(onClick = onHistory, enabled = state.ready) { Text(stringResource(R.string.a2wb_history)) }
        }
    }
}

@Composable
private fun WorkbenchEditor(
    state: WorkbenchState,
    onEdit: (String) -> Unit,
    onFormat: () -> Unit,
    onCopy: () -> Unit,
    onSave: () -> Unit,
    onImport: () -> Unit,
    onExport: () -> Unit,
    modifier: Modifier,
) {
    WorkbenchPanel(modifier) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            val hasSource = state.ready && state.source.isNotBlank()
            GlassTextButton(onClick = onFormat, enabled = hasSource) { Text(stringResource(R.string.a2wb_format)) }
            GlassTextButton(onClick = onCopy, enabled = hasSource) { Text(stringResource(R.string.a2wb_copy)) }
            GlassTextButton(onClick = onSave, enabled = hasSource) { Text(stringResource(R.string.a2wb_save)) }
            GlassTextButton(onClick = onImport, enabled = state.ready) { Text(stringResource(R.string.a2wb_import)) }
            GlassTextButton(onClick = onExport, enabled = hasSource) { Text(stringResource(R.string.a2wb_export)) }
        }
        GlassOutlinedTextField(
            value = state.source,
            onValueChange = onEdit,
            enabled = state.ready,
            label = { Text(stringResource(R.string.a2wb_source_label)) },
            textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            modifier = Modifier.fillMaxWidth().weight(1f),
        )
    }
}

@Composable
internal fun WorkbenchPanel(
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    GlassSurface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        style = GlassStyle.Regular,
    ) {
        Column(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            content = content,
        )
    }
}

@Composable
private fun WorkbenchPreview(state: WorkbenchState, modifier: Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(stringResource(R.string.a2wb_progress, state.sent, state.total), Modifier.padding(4.dp))
        Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())) {
            val session = state.session
            if (session == null) {
                Text(stringResource(R.string.a2wb_no_surface), Modifier.padding(8.dp))
            } else {
                val ids by session.surfaceIds.collectAsState()
                var selected by rememberSaveable { mutableStateOf<String?>(null) }
                if (ids.size > 1) FlowRow {
                    ids.forEach { id ->
                        GlassFilterChip(id == (selected ?: ids.first()), { selected = id }, { Text(id) })
                    }
                }
                val id = selected?.takeIf { it in ids } ?: ids.firstOrNull()
                if (id == null) {
                    Text(stringResource(R.string.a2wb_no_surface), Modifier.padding(8.dp))
                } else {
                    key(session, id) {
                        session.Surface(id, Modifier.fillMaxWidth().heightIn(max = 1000.dp))
                    }
                }
            }
        }
        state.session?.let { session -> key(session) { WorkbenchEvents(session) } }
    }
}
