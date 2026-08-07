package com.shifenmiao.ai.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.shifenmiao.ai.component.AgentJsonEditorComponent
import com.shifenmiao.ai.ui.JsonTreeViewer
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.core.R
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedAlertDialog
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedButton
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassButton
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedTextField
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalButton
import com.t8rin.imagetoolbox.core.ui.widget.text.EditorUiDefaults
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFormatPaint
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTextSnippet
import com.t8rin.imagetoolbox.core.resources.icons.line.LineWorkModels

@Composable
fun AgentJsonEditorScreen(
    component: AgentJsonEditorComponent
) {
    val uiState by component.uiState.collectAsState()
    var showDiscardDialog by remember { mutableStateOf(false) }

    BaseScreen(
        title = EditorUiDefaults.resolveScreenTitle(
            editTitle = uiState.editTitle,
            fallbackTitle = stringResource(R.string.code_editor_title)
        ),
        onGoBack = {
            if (component.hasUnsavedChanges()) {
                showDiscardDialog = true
            } else {
                component.onGoBack()
            }
        },
        actions = {
            IconButton(
                onClick = {
                    component.saveEditResult {
                        component.onGoBack()
                    }
                }
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check,
                    contentDescription = stringResource(R.string.done)
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp).imePadding()
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GlassButton(
                    onClick = { if (uiState.isTreeMode) component.toggleTreeMode() },
                ) {
                    Icon(
                        modifier = Modifier.size(18.dp),
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTextSnippet,
                        contentDescription = stringResource(R.string.code_editor_mode_text),
                        tint = if (!uiState.isTreeMode) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.code_editor_mode_text),
                        color = if (!uiState.isTreeMode) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                GlassButton(
                    onClick = { if (!uiState.isTreeMode) component.toggleTreeMode() },
                ) {
                    Icon(
                        modifier = Modifier.size(18.dp),
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineWorkModels,
                        contentDescription = stringResource(R.string.code_editor_mode_tree),
                        tint = if (uiState.isTreeMode) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.code_editor_mode_tree),
                        color = if (uiState.isTreeMode) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                if (!uiState.isTreeMode) {
                    GlassTonalButton(onClick = component::formatJson) {
                        Icon(
                            modifier = Modifier.size(18.dp),
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFormatPaint,
                            contentDescription = stringResource(R.string.code_editor_format),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.code_editor_format),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (uiState.isTreeMode) {
                if (uiState.parseError != null) {
                    Text(
                        text = uiState.parseError
                            ?: stringResource(R.string.code_editor_parse_error),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp),
                    )
                } else {
                    JsonTreeViewer(
                        jsonString = uiState.content,
                        onEditValue = component::updateJsonValue,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            } else {
                GlassOutlinedTextField(
                    value = uiState.content,
                    onValueChange = component::updateContent,
                    modifier = Modifier.fillMaxSize(),
                    textStyle = EditorUiDefaults.contentTextStyle(monospaced = true),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )
            }
        }
    }

    if (showDiscardDialog) {
        EnhancedAlertDialog(
            visible = showDiscardDialog,
            onDismissRequest = { showDiscardDialog = false },
            confirmButton = {
                EnhancedButton(
                    onClick = {
                        showDiscardDialog = false
                        component.onGoBack()
                    }
                ) {
                    Text(stringResource(R.string.discard))
                }
            },
            dismissButton = {
                EnhancedButton(
                    onClick = { showDiscardDialog = false }
                ) {
                    Text(stringResource(R.string.button_cancel))
                }
            },
            title = { Text(stringResource(R.string.unsaved_changes)) },
            text = { Text(stringResource(R.string.unsaved_changes_message)) },
        )
    }
}
