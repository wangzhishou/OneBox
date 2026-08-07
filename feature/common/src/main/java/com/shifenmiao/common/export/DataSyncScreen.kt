package com.shifenmiao.common.export

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.R
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.domain.model.MimeType
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.FileType
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.rememberFileCreator
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.rememberFilePicker
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedTextField
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalButton
import com.t8rin.imagetoolbox.core.ui.widget.navigation.BottomNavItem
import com.t8rin.imagetoolbox.core.ui.widget.navigation.BottomNavigationBar
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.t8rin.imagetoolbox.core.resources.icons.CheckCircle
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDownload
import com.t8rin.imagetoolbox.core.resources.icons.line.LineContentPaste
import com.t8rin.imagetoolbox.core.resources.icons.line.LineUpload

@Composable
fun DataSyncScreen(
    component: DataSyncComponent
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val isExporting by component.isExporting.collectAsState()
    val isImporting by component.isImporting.collectAsState()
    
    val exportableItems by component.exportableItems.collectAsState()
    val parsedImportItems by component.parsedImportItems.collectAsState()

    var currentTab by remember { mutableStateOf("export") }
    
    var selectedExportIds by remember { mutableStateOf(setOf<Int>()) }
    var selectedImportIndices by remember { mutableStateOf(setOf<Int>()) }

    var jsonInput by remember { mutableStateOf("") }

    val exportSuccessText = stringResource(R.string.data_sync_export_success)
    val importSuccessText = stringResource(R.string.data_sync_import_success)

    val fileCreator = rememberFileCreator(
        mimeType = MimeType.Json,
        onSuccess = { uri: Uri ->
            component.exportData(
                selectedIds = selectedExportIds,
                onSuccess = { jsonString ->
                    scope.launch {
                        try {
                            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                                outputStream.write(jsonString.toByteArray())
                            }
                            AppToastHost.showToast(exportSuccessText)
                            selectedExportIds = emptySet()
                        } catch (e: Exception) {
                            AppToastHost.showToast(context.getString(R.string.data_sync_export_failed, e.message))
                        }
                    }
                },
                onError = { error ->
                    scope.launch { AppToastHost.showToast(error) }
                }
            )
        }
    )

    val filePicker = rememberFilePicker(
        type = FileType.Single,
        mimeType = MimeType.ImportText,
        onSuccess = { uris ->
            val uri = uris.firstOrNull() ?: return@rememberFilePicker
            scope.launch {
                try {
                    val stringBuilder = StringBuilder()
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        BufferedReader(InputStreamReader(inputStream)).use { reader ->
                            var line: String?
                            while (reader.readLine().also { line = it } != null) {
                                stringBuilder.append(line)
                            }
                        }
                    }
                    val jsonContent = stringBuilder.toString()
                    component.parseImportData(
                        jsonString = jsonContent,
                        onError = { error ->
                            scope.launch { AppToastHost.showToast(error) }
                        }
                    )
                } catch (e: Exception) {
                    AppToastHost.showToast(context.getString(R.string.data_sync_read_file_failed, e.message))
                }
            }
        }
    )

    BaseScreen(
        title = stringResource(com.t8rin.imagetoolbox.core.resources.R.string.data_sync),
        onGoBack = component.onGoBack
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                if (currentTab == "export") {
                    ExportContent(
                        exportableItems = exportableItems,
                        selectedExportIds = selectedExportIds,
                        onSelectionChanged = { selectedExportIds = it },
                        onExportClick = { fileCreator.make("wanbaohe_export.json") },
                        isExporting = isExporting
                    )
                } else {
                    ImportContent(
                        parsedImportItems = parsedImportItems,
                        selectedImportIndices = selectedImportIndices,
                        onSelectionChanged = { selectedImportIndices = it },
                        jsonInput = jsonInput,
                        onJsonInputChange = { jsonInput = it },
                        onParseJson = {
                            if (jsonInput.isNotBlank()) {
                                component.parseImportData(
                                    jsonString = jsonInput,
                                    onError = { error ->
                                        scope.launch { AppToastHost.showToast(error) }
                                    }
                                )
                            }
                        },
                        onPickFile = { filePicker.pickFile() },
                        onClearParsed = { 
                            component.clearParsedImportData()
                            selectedImportIndices = emptySet()
                            jsonInput = ""
                        },
                        onImportClick = {
                            val itemsToImport = selectedImportIndices.map { parsedImportItems[it] }
                            component.importData(
                                selectedItems = itemsToImport,
                                onSuccess = {
                                    scope.launch { 
                                        AppToastHost.showToast(importSuccessText) 
                                        component.clearParsedImportData()
                                        selectedImportIndices = emptySet()
                                        jsonInput = ""
                                    }
                                },
                                onError = { error ->
                                    scope.launch { AppToastHost.showToast(error) }
                                }
                            )
                        },
                        isImporting = isImporting
                    )
                }
            }

            BottomNavigationBar(
                items = listOf(
                    BottomNavItem(
                        id = "export",
                        label = stringResource(R.string.data_sync_tab_export),
                        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDownload
                    ),
                    BottomNavItem(
                        id = "import",
                        label = stringResource(R.string.data_sync_tab_import),
                        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineUpload
                    )
                ),
                selectedItemId = currentTab,
                onItemClick = { currentTab = it.id },
                showBar = true
            )
        }
    }
}

@Composable
private fun ExportContent(
    exportableItems: List<com.shifenmiao.database.item.entity.ItemWithRelation>,
    selectedExportIds: Set<Int>,
    onSelectionChanged: (Set<Int>) -> Unit,
    onExportClick: () -> Unit,
    isExporting: Boolean
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.data_sync_select_export_data, selectedExportIds.size, exportableItems.size),
                style = MaterialTheme.typography.titleMedium
            )
            TextButton(
                onClick = {
                    if (selectedExportIds.size == exportableItems.size) {
                        onSelectionChanged(emptySet())
                    } else {
                        onSelectionChanged(exportableItems.map { it.item.id }.toSet())
                    }
                }
            ) {
                Text(stringResource(if (selectedExportIds.size == exportableItems.size) R.string.data_sync_deselect_all else R.string.data_sync_select_all))
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(exportableItems) { itemWithRel ->
                val item = itemWithRel.item
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val newSet = selectedExportIds.toMutableSet()
                            if (newSet.contains(item.id)) {
                                newSet.remove(item.id)
                            } else {
                                newSet.add(item.id)
                            }
                            onSelectionChanged(newSet)
                        }
                    ) {
                        Icon(
                            imageVector = if (selectedExportIds.contains(item.id)) com.t8rin.imagetoolbox.core.resources.Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (selectedExportIds.contains(item.id)) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = item.title ?: stringResource(R.string.data_sync_unknown_title), style = MaterialTheme.typography.bodyLarge)
                        if (!item.description.isNullOrBlank()) {
                            Text(text = item.description ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        GlassTonalButton(
            onClick = onExportClick,
            enabled = !isExporting && selectedExportIds.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = AppTheme.colors.filledTonalButtonColors()
        ) {
            Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDownload, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
            Text(stringResource(if (isExporting) R.string.data_sync_exporting else R.string.data_sync_export_selected))
        }
    }
}

@Composable
private fun ImportContent(
    parsedImportItems: List<ExportItemModel>,
    selectedImportIndices: Set<Int>,
    onSelectionChanged: (Set<Int>) -> Unit,
    jsonInput: String,
    onJsonInputChange: (String) -> Unit,
    onParseJson: () -> Unit,
    onPickFile: () -> Unit,
    onClearParsed: () -> Unit,
    onImportClick: () -> Unit,
    isImporting: Boolean
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (parsedImportItems.isEmpty()) {
            // 解析前
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                GlassTonalButton(
                    onClick = onPickFile,
                    modifier = Modifier.fillMaxWidth(),
                    colors = AppTheme.colors.filledTonalButtonColors()
                ) {
                    Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineUpload, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    Text(stringResource(R.string.data_sync_import_from_file))
                }

                Text(
                    text = stringResource(R.string.data_sync_or_paste_json),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                GlassOutlinedTextField(
                    value = jsonInput,
                    onValueChange = onJsonInputChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 150.dp),
                    placeholder = { Text(stringResource(R.string.data_sync_paste_json_hint)) },
                    textStyle = MaterialTheme.typography.bodyMedium
                )

                GlassTonalButton(
                    onClick = onParseJson,
                    enabled = jsonInput.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = AppTheme.colors.filledTonalButtonColors()
                ) {
                    Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineContentPaste, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    Text(stringResource(R.string.data_sync_parse_data))
                }
            }
        } else {
            // 解析后，选择要导入的项
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.data_sync_select_import_data, selectedImportIndices.size, parsedImportItems.size),
                    style = MaterialTheme.typography.titleMedium
                )
                Row {
                    TextButton(onClick = onClearParsed) {
                        Text(stringResource(R.string.data_sync_reselect))
                    }
                    TextButton(
                        onClick = {
                            if (selectedImportIndices.size == parsedImportItems.size) {
                                onSelectionChanged(emptySet())
                            } else {
                                onSelectionChanged(parsedImportItems.indices.toSet())
                            }
                        }
                    ) {
                        Text(stringResource(if (selectedImportIndices.size == parsedImportItems.size) R.string.data_sync_deselect_all else R.string.data_sync_select_all))
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(parsedImportItems.size) { index ->
                    val item = parsedImportItems[index]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                val newSet = selectedImportIndices.toMutableSet()
                                if (newSet.contains(index)) {
                                    newSet.remove(index)
                                } else {
                                    newSet.add(index)
                                }
                                onSelectionChanged(newSet)
                            }
                        ) {
                            Icon(
                                imageVector = if (selectedImportIndices.contains(index)) com.t8rin.imagetoolbox.core.resources.Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (selectedImportIndices.contains(index)) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = item.title ?: item.name ?: stringResource(R.string.data_sync_unknown_title), style = MaterialTheme.typography.bodyLarge)
                            if (!item.description.isNullOrBlank()) {
                                Text(text = item.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            GlassTonalButton(
                onClick = onImportClick,
                enabled = !isImporting && selectedImportIndices.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = AppTheme.colors.filledTonalButtonColors()
            ) {
                Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text(stringResource(if (isImporting) R.string.data_sync_importing else R.string.data_sync_confirm_import))
            }
        }
    }
}
