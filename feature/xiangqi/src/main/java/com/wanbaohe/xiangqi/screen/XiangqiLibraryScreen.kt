package com.wanbaohe.xiangqi.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.common.ui.BaseScreen
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedTextField
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSurface
import com.wanbaohe.xiangqi.R
import com.wanbaohe.xiangqi.component.XiangqiLibraryComponent
import com.wanbaohe.xiangqi.data.XiangqiGameSummary
import com.wanbaohe.xiangqi.domain.model.GameMode
import com.wanbaohe.xiangqi.domain.model.GameStatus
import com.wanbaohe.xiangqi.domain.model.PlayerType
import java.text.DateFormat
import java.util.Date
import com.t8rin.imagetoolbox.core.resources.icons.Edit
import com.t8rin.imagetoolbox.core.resources.icons.Delete
import com.t8rin.imagetoolbox.core.resources.icons.line.LineArrowForwardIos

@Composable
fun XiangqiLibraryScreen(
    component: XiangqiLibraryComponent,
    modifier: Modifier = Modifier,
    showChrome: Boolean = true,
) {
    val libraryTitle = stringResource(R.string.xiangqi_library_title)

    val content: @Composable (Modifier) -> Unit = { contentModifier ->
        XiangqiLibraryContent(
            component = component,
            modifier = contentModifier,
        )
    }

    if (showChrome) {
        BaseScreen(
            title = libraryTitle,
            onGoBack = component.onGoBack,
        ) {
            content(Modifier.fillMaxSize())
        }
    } else {
        content(modifier)
    }
}

@Composable
private fun XiangqiLibraryContent(
    component: XiangqiLibraryComponent,
    modifier: Modifier,
) {
    if (component.games.isEmpty()) {
        XiangqiEmptyQuickPanel(
            component = component,
            headline = stringResource(R.string.xiangqi_empty_library_title),
            subline = stringResource(R.string.xiangqi_empty_library_message),
            modifier = modifier.fillMaxSize(),
        )
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(component.games, key = { it.id }) { item ->
            GameCard(
                item = item,
                onContinue = { component.openGame(item.id) },
                onAnalysis = { component.openAnalysis(item.id) },
                onDelete = { component.deleteGame(item.id) },
                onRename = { newTitle -> component.renameGame(item.id, newTitle) },
            )
        }
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun GameCard(
    item: XiangqiGameSummary,
    onContinue: () -> Unit,
    onAnalysis: () -> Unit,
    onDelete: () -> Unit,
    onRename: (String) -> Unit,
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    val isLocal = item.mode == GameMode.LOCAL_PVP
    val modeText = when (item.mode) {
        GameMode.LOCAL_PVP -> stringResource(R.string.xiangqi_mode_local)
        GameMode.ONLINE_PVP -> stringResource(R.string.xiangqi_mode_online)
        GameMode.LLM_VS_LLM -> stringResource(R.string.xiangqi_mode_ai_vs_ai)
        GameMode.HUMAN_VS_LLM -> stringResource(R.string.xiangqi_mode_ai)
    }

    val redName = when {
        item.redPlayerType == PlayerType.LLM -> stringResource(R.string.xiangqi_player_ai)
        isLocal -> stringResource(R.string.xiangqi_player_local_red)
        else -> stringResource(R.string.xiangqi_player_you)
    }
    val blackName = when {
        item.blackPlayerType == PlayerType.LLM -> stringResource(R.string.xiangqi_player_ai)
        isLocal -> stringResource(R.string.xiangqi_player_local_black)
        else -> stringResource(R.string.xiangqi_player_you)
    }

    GlassSurface(
        modifier = Modifier.fillMaxWidth().clickable { onContinue() },
        style = GlassStyle.Medium,
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = modeText.uppercase(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp)
                    )
                }
                Text(
                    text = DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(item.updatedAt)),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.2f), CircleShape)
                            .border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(R.string.xiangqi_side_red), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(redName, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp)
                ) {
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(Brush.horizontalGradient(listOf(Color.Transparent, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)))))
                    Text(stringResource(R.string.xiangqi_vs_short), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 8.dp))
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), Color.Transparent))))
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(R.string.xiangqi_side_black), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(blackName, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = item.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = when (item.status) {
                    GameStatus.NOT_STARTED -> stringResource(R.string.xiangqi_library_status_not_started)
                    GameStatus.PAUSED -> stringResource(R.string.xiangqi_library_status_paused)
                    GameStatus.PLAYING, GameStatus.CHECK -> stringResource(R.string.xiangqi_library_status_in_progress)
                    else -> stringResource(R.string.xiangqi_library_status_game_over)
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)))
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: edit + delete icons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    val subtleTint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                    IconButton(onClick = { showRenameDialog = true }) {
                        Icon(
                            com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Edit,
                            contentDescription = stringResource(R.string.xiangqi_rename_game),
                            tint = subtleTint,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(
                            com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Delete,
                            contentDescription = stringResource(R.string.xiangqi_delete_game),
                            tint = subtleTint,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }

                // Right: analysis link
                Row(
                    modifier = Modifier.clickable { onAnalysis() },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(stringResource(R.string.xiangqi_library_open_analysis), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
                    Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineArrowForwardIos, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.xiangqi_delete_confirm_title)) },
            text = { Text(stringResource(R.string.xiangqi_delete_confirm_message)) },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    showDeleteConfirm = false
                    onDelete()
                }) {
                    Text(stringResource(R.string.xiangqi_confirm))
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.xiangqi_cancel))
                }
            },
        )
    }

    if (showRenameDialog) {
        RenameGameDialog(
            currentTitle = item.title,
            onDismiss = { showRenameDialog = false },
            onConfirm = { newTitle ->
                showRenameDialog = false
                onRename(newTitle)
            },
        )
    }
}

@Composable
private fun RenameGameDialog(
    currentTitle: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var text by remember { mutableStateOf(currentTitle) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.xiangqi_rename_game)) },
        text = {
            GlassOutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.xiangqi_rename_hint)) },
                singleLine = true,
            )
        },
        confirmButton = {
            androidx.compose.material3.TextButton(
                onClick = { onConfirm(text) },
                enabled = text.isNotBlank(),
            ) {
                Text(stringResource(R.string.xiangqi_confirm))
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.xiangqi_cancel))
            }
        },
    )
}
