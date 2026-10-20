package com.wanbaohe.teleprompter.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wanbaohe.base.ui.empty.EmptyStateGuide
import com.wanbaohe.base.ui.empty.EmptyStateGuideAction
import com.shifenmiao.common.ui.BaseScreen
import com.wanbaohe.teleprompter.R
import com.wanbaohe.teleprompter.component.TeleprompterComponent
import com.wanbaohe.teleprompter.ui.ScriptCard

import com.wanbaohe.core.ui.widget.enhanced.EnhancedAlertDialog
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTeleprompter
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAutoFix

/**
 * 文稿列表页 (Script Library)
 */
@Composable
fun TeleprompterListScreen(
    component: TeleprompterComponent,
) {
    val state by component.listState.collectAsState()
    var deleteTargetId by remember { mutableStateOf<String?>(null) }

    BaseScreen(
        title = stringResource(R.string.teleprompter_title),
        onGoBack = component.onGoBack,
        actions = {
            IconButton(
                onClick = { component.onNewScript() },
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Add,
                    contentDescription = stringResource(R.string.teleprompter_new_script),
                )
            }
        }
    ) {
        if (state.isLoading) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                CircularProgressIndicator()
            }
        } else if (state.scripts.isEmpty()) {
            EmptyStateGuide(
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTeleprompter,
                title = stringResource(R.string.teleprompter_empty_title),
                description = stringResource(R.string.teleprompter_empty_description),
                actions = listOf(
                    EmptyStateGuideAction(
                        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Add,
                        title = stringResource(R.string.teleprompter_new_script),
                        description = stringResource(R.string.teleprompter_empty_manual_desc),
                        onClick = { component.onNewScript() },
                    ),
                    EmptyStateGuideAction(
                        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAutoFix,
                        title = stringResource(R.string.teleprompter_empty_go_ai),
                        description = stringResource(R.string.teleprompter_empty_ai_desc),
                        onClick = component::navigateToAiAssist,
                        emphasized = true,
                    ),
                ),
                footerHint = stringResource(R.string.teleprompter_empty),
            )
        } else {
            // 统计头
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = stringResource(R.string.teleprompter_recent_scripts),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = pluralStringResource(
                        R.plurals.teleprompter_scripts_total,
                        state.scripts.size,
                        state.scripts.size,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                items(
                    items = state.scripts,
                    key = { it.id }
                ) { script ->
                    ScriptCard(
                        script = script,
                        onClick = { component.onEditScript(script) },
                        onEdit = { component.onEditScript(script) },
                        onPlay = { component.onPlayScript(script) },
                        onDelete = { deleteTargetId = script.id },
                    )
                }
                item { Spacer(Modifier.height(80.dp)) } // FAB 留白
            }
        }
    }

    // 删除确认对话框
    EnhancedAlertDialog(
        visible = deleteTargetId != null,
        onDismissRequest = { deleteTargetId = null },
        title = { Text(stringResource(R.string.teleprompter_delete)) },
        text = { Text(stringResource(R.string.teleprompter_delete_confirm)) },
        confirmButton = {
            TextButton(onClick = {
                deleteTargetId?.let { id ->
                    component.onDeleteScript(id)
                }
                deleteTargetId = null
            }) {
                Text(stringResource(R.string.teleprompter_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = { deleteTargetId = null }) {
                Text(stringResource(R.string.teleprompter_cancel))
            }
        }
    )
}
