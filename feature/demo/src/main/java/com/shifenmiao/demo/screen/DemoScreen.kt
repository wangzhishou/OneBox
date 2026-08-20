package com.shifenmiao.demo.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.core.R
import com.shifenmiao.demo.screenLogic.DemoComponent
import com.shifenmiao.model.ai.AIGCInfo
import com.t8rin.imagetoolbox.core.domain.model.MimeType
import com.t8rin.imagetoolbox.core.domain.model.plus
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.FileType
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.rememberFilePicker
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalButton
import com.t8rin.imagetoolbox.core.utils.getString
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.shifenmiao.demo.R as DemoR
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTheme
import com.t8rin.imagetoolbox.core.resources.icons.Close
import com.t8rin.imagetoolbox.core.resources.icons.ContentCopy
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCalendar
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCloudStorage
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCalculate
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCasino
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFileOpen
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLink
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMagic
import com.t8rin.imagetoolbox.core.resources.icons.Compass
import com.t8rin.imagetoolbox.core.resources.icons.BrokenImageAlt
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAccountBalance
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDashboardCustomize
import com.t8rin.imagetoolbox.core.resources.icons.line.LineNote
import com.t8rin.imagetoolbox.core.resources.icons.DshWhale
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRobot
import com.t8rin.imagetoolbox.core.resources.icons.line.LineShield
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSpeedTest
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTravelExplore
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAltitude
import com.t8rin.imagetoolbox.core.resources.icons.line.LineBook
import com.t8rin.imagetoolbox.core.resources.icons.line.LineBookkeeping
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCheckCircleOutline
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTeleprompter
import com.t8rin.imagetoolbox.core.resources.icons.line.LineGrid4x4
import com.t8rin.imagetoolbox.core.resources.icons.line.LineImportExport
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSquareFoot

@Composable
fun DemoScreen(
    demoComponent: DemoComponent,
    onGoBack: () -> Unit,
    onNavigate: (Screen) -> Unit = {}
) {
    var showDeeplinkHelp by remember { mutableStateOf(false) }
    var showGlassShowcase by remember { mutableStateOf(false) }
    var showA2uiGallery by remember { mutableStateOf(false) }
    EnhancedModalBottomSheet(
        visible = showDeeplinkHelp,
        dragHandle = {
            CenterAlignedTopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                actions = {
                    IconButton(onClick = { showDeeplinkHelp = false }) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                },
                title = {
                    Text(
                        text = stringResource(R.string.demo_navigation_deeplink_tool_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                }
            )
        },
        onDismiss = { showDeeplinkHelp = false },
        sheetContent = {
            NavigationDeeplinkHelpSheet(
                demoComponent = demoComponent,
                onNavigate = onNavigate
            )
        }
    )

    EnhancedModalBottomSheet(
        visible = showGlassShowcase,
        dragHandle = {
            CenterAlignedTopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                actions = {
                    IconButton(onClick = { showGlassShowcase = false }) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                },
                title = {
                    Text(
                        text = stringResource(DemoR.string.demo_glass_showcase_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                }
            )
        },
        onDismiss = { showGlassShowcase = false },
        sheetContent = {
            GlassComponentShowcaseSheet()
        }
    )

    EnhancedModalBottomSheet(
        visible = showA2uiGallery,
        dragHandle = {
            CenterAlignedTopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                actions = {
                    IconButton(onClick = { showA2uiGallery = false }) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                },
                title = {
                    Text(
                        text = "A2UI 组件画廊",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                }
            )
        },
        onDismiss = { showA2uiGallery = false },
        sheetContent = {
            A2uiComponentGalleryScreen(
                demoComponent = demoComponent,
                onDismiss = { showA2uiGallery = false }
            )
        }
    )

    val filePicker = rememberFilePicker(
        type = FileType.Single,
        mimeType = MimeType.Pdf + MimeType.Png + MimeType.Html,
        onFailure = { demoComponent.onPickFileFailed() },
        onSuccess = { uris -> demoComponent.onPickFile(uris.first()) }
    )

    BaseScreen(
        title = { Text(text = "Demo") },
        isShowDefaultActions = true,
        onGoBack = onGoBack
    ) {
        val demoEntries = demoNavigationEntries()
        val gridState = rememberLazyGridState()
        LazyVerticalGrid(
            state = gridState,
            modifier = Modifier.fillMaxHeight()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(2),
            userScrollEnabled = true
        ) {
            demoNavigationItems(entries = demoEntries, onNavigate = onNavigate)
            item {
                DemoActionButton(
                    title = "A2UI 组件画廊",
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDashboardCustomize,
                    emphasized = true,
                    onClick = { showA2uiGallery = true }
                )
            }
            item {
                DemoActionButton(
                    title = stringResource(DemoR.string.demo_glass_showcase_button_title),
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMagic,
                    emphasized = true,
                    onClick = { showGlassShowcase = true }
                )
            }
            item {
                DemoActionButton(
                    title = stringResource(R.string.demo_navigation_deeplink_tool_title),
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineLink,
                    onClick = { showDeeplinkHelp = true }
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                AigcDemoSection(
                    component = demoComponent,
                    onPickFile = { filePicker.pickFile() },
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                DemoContent(
                    component = demoComponent,
                    onNavigate = onNavigate,
                )
            }
        }
    }
    BackHandler { onGoBack() }
}

@Composable
private fun AigcDemoSection(
    component: DemoComponent,
    onPickFile: () -> Unit,
) {
    val state by component.state.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(DemoR.string.demo_aigc_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = stringResource(DemoR.string.demo_aigc_description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DemoActionButton(
                title = stringResource(DemoR.string.demo_aigc_action_pick),
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFileOpen,
                onClick = onPickFile,
                modifier = Modifier.weight(1f)
            )
            DemoActionButton(
                title = stringResource(DemoR.string.demo_aigc_action_demo),
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMagic,
                onClick = { component.loadDemoAigcInfo() },
                modifier = Modifier.weight(1f)
            )
        }

        AigcSourceSummary(state = state)

        AnimatedVisibility(visible = state.isLoading) {
            Text(
                text = stringResource(DemoR.string.demo_aigc_loading),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AigcSourceSummary(state: DemoComponent.DemoState) {
    val text = when {
        state.sourceKind == DemoComponent.SourceKind.DEMO ->
            stringResource(DemoR.string.demo_aigc_source_demo)
        state.sourceKind == DemoComponent.SourceKind.NONE ->
            stringResource(DemoR.string.demo_aigc_source_none)
        state.selectedFileName != null ->
            stringResource(DemoR.string.demo_aigc_source_file, state.selectedFileName)
        else -> state.selectedUri ?: ""
    }
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
fun DemoContent(
    component: DemoComponent,
    onNavigate: (Screen) -> Unit,
) {
    val state by component.state.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when {
            state.isLoading -> Unit
            state.error != null -> {
                Text(
                    text = state.error.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            state.parsedAigcInfo != null -> {
                AigcInfoPanel(aigc = state.parsedAigcInfo!!)
            }
        }

        state.aigcInfo?.takeIf { it.isNotBlank() }?.let { raw ->
            AigcRawPanel(raw = raw)
        }

        DemoScheduleDatabaseSection(
            state = state,
            onNavigate = onNavigate,
        )
    }
}

@Composable
private fun AigcInfoPanel(aigc: AIGCInfo) {
    val blank = stringResource(DemoR.string.demo_aigc_value_blank)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(DemoR.string.demo_aigc_parsed_title),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            AigcInfoLine(
                label = stringResource(DemoR.string.demo_aigc_label),
                value = resolveAigcLabelText(aigc.label, blank)
            )
            AigcInfoGroup(
                title = stringResource(DemoR.string.demo_aigc_group_producer),
                rows = listOf(
                    AigcFieldValue(
                        label = stringResource(DemoR.string.demo_aigc_field_engine) + " / " +
                            stringResource(DemoR.string.demo_aigc_field_model),
                        value = aigc.contentProducer.ifBlank { blank }
                    ),
                    AigcFieldValue(
                        label = "ProduceID",
                        value = aigc.produceID.ifBlank { blank }
                    )
                )
            )
            AigcInfoGroup(
                title = stringResource(DemoR.string.demo_aigc_group_propagator),
                rows = listOf(
                    AigcFieldValue(
                        label = "Propagator",
                        value = aigc.contentPropagator.ifBlank { blank }
                    ),
                    AigcFieldValue(
                        label = "PropagateID",
                        value = aigc.propagateID.ifBlank { blank }
                    )
                )
            )
            val (engine, model, completion, userHash) = parseAigcIntegrity(aigc.reservedCode1)
            AigcInfoGroup(
                title = stringResource(DemoR.string.demo_aigc_group_integrity),
                rows = listOf(
                    AigcFieldValue(
                        label = stringResource(DemoR.string.demo_aigc_field_engine),
                        value = engine
                    ),
                    AigcFieldValue(
                        label = stringResource(DemoR.string.demo_aigc_field_model),
                        value = model
                    ),
                    AigcFieldValue(
                        label = stringResource(DemoR.string.demo_aigc_field_completion),
                        value = completion
                    ),
                    AigcFieldValue(
                        label = stringResource(DemoR.string.demo_aigc_field_user_hash),
                        value = userHash
                    )
                )
            )
            val (entry, ref, uid) = parseAigcTrace(aigc.reservedCode2)
            AigcInfoGroup(
                title = stringResource(DemoR.string.demo_aigc_group_trace),
                rows = listOf(
                    AigcFieldValue(
                        label = stringResource(DemoR.string.demo_aigc_field_entry),
                        value = entry
                    ),
                    AigcFieldValue(
                        label = stringResource(DemoR.string.demo_aigc_field_ref),
                        value = ref
                    ),
                    AigcFieldValue(
                        label = stringResource(DemoR.string.demo_aigc_field_uid),
                        value = uid
                    )
                )
            )
        }
    }
}

private data class AigcFieldValue(
    val label: String,
    val value: String,
)

@Composable
private fun AigcInfoGroup(
    title: String,
    rows: List<AigcFieldValue>,
) {
    val blank = stringResource(DemoR.string.demo_aigc_value_blank)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.outline,
        )
        rows.forEach { row ->
            AigcInfoLine(label = row.label, value = row.value.ifBlank { blank })
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    }
}

@Composable
private fun AigcInfoLine(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

private fun parseAigcIntegrity(reservedCode1: String): Quadruple<String, String, String, String> {
    return Quadruple(
        first = parseTraceValue(reservedCode1, "engine"),
        second = parseTraceValue(reservedCode1, "model"),
        third = parseTraceValue(reservedCode1, "completion"),
        fourth = parseTraceValue(reservedCode1, "userHash")
    )
}

private fun parseAigcTrace(reservedCode2: String): Triple<String, String, String> {
    return Triple(
        parseTraceValue(reservedCode2, "entry"),
        parseTraceValue(reservedCode2, "ref"),
        parseTraceValue(reservedCode2, "uid")
    )
}

private fun parseTraceValue(input: String, key: String): String {
    return Regex("$key=([^|]*)").find(input)?.groupValues?.getOrNull(1) ?: ""
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
)

@Composable
private fun resolveAigcLabelText(label: String, blank: String): String {
    if (label.isBlank()) return blank
    return when (label) {
        "1" -> stringResource(DemoR.string.demo_aigc_label_1)
        "2" -> stringResource(DemoR.string.demo_aigc_label_2)
        "3" -> stringResource(DemoR.string.demo_aigc_label_3)
        else -> stringResource(DemoR.string.demo_aigc_label_unknown, label)
    }
}

@Composable
private fun AigcRawPanel(raw: String) {
    val clipboardManager = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(DemoR.string.demo_aigc_raw_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                IconButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(raw))
                        copied = true
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ContentCopy,
                        contentDescription = stringResource(DemoR.string.demo_aigc_raw_copy),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Text(
                text = if (copied) stringResource(DemoR.string.demo_aigc_raw_copied) else raw,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Monospace,
            )
        }
    }
}

@Composable
private fun DemoScheduleDatabaseSection(
    state: DemoComponent.DemoState,
    onNavigate: (Screen) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(DemoR.string.demo_schedule_database_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(DemoR.string.demo_schedule_database_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        GlassTonalButton(
            onClick = { onNavigate(Screen.Schedule()) },
            style = GlassStyle.Medium,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCalendar,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(text = stringResource(DemoR.string.demo_schedule_open_screen))
        }

        when {
            state.scheduleLoadError -> {
                Text(
                    text = stringResource(DemoR.string.demo_schedule_database_load_failed),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            state.scheduleEvents.isEmpty() -> {
                Text(
                    text = stringResource(DemoR.string.demo_schedule_database_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            else -> {
                Text(
                    text = stringResource(
                        DemoR.string.demo_schedule_database_count,
                        state.scheduleEvents.size,
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )

                state.scheduleEvents.take(8).forEach { event ->
                    DemoScheduleEventCard(event = event)
                }

                val remainingCount = state.scheduleEvents.size - 8
                if (remainingCount > 0) {
                    Text(
                        text = stringResource(
                            DemoR.string.demo_schedule_database_more,
                            remainingCount,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun DemoScheduleEventCard(
    event: DemoComponent.ScheduleEventPreview,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = event.title.ifBlank { event.id },
                style = MaterialTheme.typography.titleSmall,
            )

            event.description?.takeIf { it.isNotBlank() }?.let { description ->
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Text(
                text = stringResource(DemoR.string.demo_schedule_event_time, formatScheduleRange(event)),
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = stringResource(DemoR.string.demo_schedule_event_provider, event.providerType),
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = stringResource(DemoR.string.demo_schedule_event_sync, event.syncStatus),
                style = MaterialTheme.typography.bodySmall,
            )
            event.linkedTaskId?.let { linkedTaskId ->
                Text(
                    text = stringResource(DemoR.string.demo_schedule_event_linked_task, linkedTaskId),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

private fun formatScheduleRange(event: DemoComponent.ScheduleEventPreview): String {
    val pattern = if (event.isAllDay) {
        "yyyy-MM-dd"
    } else {
        "yyyy-MM-dd HH:mm"
    }
    val formatter = SimpleDateFormat(pattern, Locale.getDefault())
    val start = formatter.format(Date(event.startUtcMillis))
    val end = formatter.format(Date(event.endUtcMillis))
    return if (event.isAllDay) {
        "$start ${if (start == end) "" else "- $end"}".trim()
    } else if (event.startUtcMillis == event.endUtcMillis) {
        start
    } else {
        "$start - $end"
    }
}

private data class DemoNavigationEntry(
    val title: String,
    val icon: ImageVector,
    val destination: () -> Screen
)

private fun demoNavigationEntries(): List<DemoNavigationEntry> = listOf(
    DemoNavigationEntry("远程存储", com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCloudStorage) { Screen.CloudStorage() },
    DemoNavigationEntry("海拔仪", com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAltitude) { Screen.Altitude },
    DemoNavigationEntry("网络测速", com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSpeedTest) { Screen.SpeedTest },
    DemoNavigationEntry("DSH 客户端", com.t8rin.imagetoolbox.core.resources.Icons.Rounded.DshWhale) { Screen.DshClient },
    DemoNavigationEntry("计算转换", com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCalculate) { Screen.UnitConverter() },
    DemoNavigationEntry("计算器 Demo", com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCalculate) {
        Screen.UnitConverter(initialTab = "calculator")
    },
    DemoNavigationEntry("亲戚关系 Demo", com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCalculate) {
        Screen.UnitConverter(initialTab = "relative")
    },
    DemoNavigationEntry("指南针", com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Compass) { Screen.Compass },
    DemoNavigationEntry("万年历", com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCalendar) { Screen.Calendar() },
    DemoNavigationEntry("日程中心", com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCalendar) { Screen.Schedule() },
    DemoNavigationEntry("屏幕坏点检测", com.t8rin.imagetoolbox.core.resources.Icons.Rounded.BrokenImageAlt) { Screen.DeadPixelTest },
    DemoNavigationEntry("记账本", com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineBookkeeping) { Screen.Bookkeeping() },
    DemoNavigationEntry("习惯打卡", com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCheckCircleOutline) { Screen.HabitTracker() },
    DemoNavigationEntry("贷款计算器", com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAccountBalance) { Screen.LoanCalculator },
    DemoNavigationEntry("投骰子", com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCasino) { Screen.DiceRoller },
    DemoNavigationEntry("2048", com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineGrid4x4) { Screen.Game2048 },
    DemoNavigationEntry("扫雷", com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCasino) { Screen.Minesweeper },
    DemoNavigationEntry(
        getString(DemoR.string.demo_blessing_wall_entry_title),
        com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMagic,
    ) { Screen.BlessingWall() },
    DemoNavigationEntry("中国古诗词", com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineBook) { Screen.Poem() },
    DemoNavigationEntry(getString(DemoR.string.demo_xiangqi_entry_title),
        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCasino,
    ) { Screen.XiangqiRouter() },
    DemoNavigationEntry("提词器", com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTeleprompter) { Screen.Teleprompter() },
    DemoNavigationEntry("创建智能体", com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMagic) { Screen.CreateAIAgent() },
    DemoNavigationEntry("创建提示词", com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineNote) { Screen.CreateAIChatPrompt() },
    DemoNavigationEntry("创建代办", com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMagic) { Screen.MarkTodoRouter(Screen.MarkTodoRouter.MarkTodoType.AddTodo()) },
    DemoNavigationEntry("数据同步", com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineImportExport) { Screen.DataSync },
    DemoNavigationEntry("时光里程碑", com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMagic) { Screen.LifeTime },
    DemoNavigationEntry("主题设置", com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTheme) { Screen.ThemeSettings },
    DemoNavigationEntry("撑30秒赚积分", com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCasino) { Screen.Survive30s },
    DemoNavigationEntry("浏览器", com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTravelExplore) { Screen.WebBrowser() },
    DemoNavigationEntry("测量工具", com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSquareFoot) { Screen.MeasurementTools },
    DemoNavigationEntry("密码保险箱", com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineShield) { Screen.PasswordVault() },
    DemoNavigationEntry("A2UI 渲染器", com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDashboardCustomize) { Screen.A2UI }
)

private fun LazyGridScope.demoNavigationItems(
    entries: List<DemoNavigationEntry>,
    onNavigate: (Screen) -> Unit
) {
    entries.forEach { entry ->
        item(key = entry.title) {
            DemoActionButton(
                title = entry.title,
                icon = entry.icon,
                onClick = { onNavigate(entry.destination()) }
            )
        }
    }
}

@Composable
private fun DemoActionButton(
    title: String,
    icon: ImageVector? = null,
    emphasized: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val containerColor = if (emphasized) {
        colorScheme.primaryContainer
    } else {
        colorScheme.surfaceContainerHigh
    }
    val contentColor = if (emphasized) {
        colorScheme.onPrimaryContainer
    } else {
        colorScheme.primary
    }

    GlassTonalButton(
        onClick = onClick,
        style = if (emphasized) GlassStyle.Medium else GlassStyle.Regular,
        borderWidth = if (emphasized) 0.9.dp else 0.75.dp,
        color = containerColor,
        contentColor = contentColor,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
        }
        Text(text = title)
    }
}
