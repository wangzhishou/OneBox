package com.wanbaohe.xiangqi.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.utils.ActionUtils
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSurface
import com.wanbaohe.xiangqi.R
import com.wanbaohe.xiangqi.component.XiangqiLibraryComponent
import com.wanbaohe.xiangqi.di.XiangqiOnlineEntryPoint
import com.wanbaohe.xiangqi.domain.model.Side
import dagger.hilt.android.EntryPointAccessors
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRobot
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCasino
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePublic
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAvatarDefault
import com.t8rin.imagetoolbox.core.resources.icons.line.LineGroup
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMemory
import com.t8rin.imagetoolbox.core.resources.icons.line.LineUploadFile

/**
 * 在 Play / Analyze / History 三个 tab 上,如果没有对局数据,
 * 展示 6 个快捷创建入口,代替"还没有对局"的空文案。
 */
@Composable
fun XiangqiEmptyQuickPanel(
    component: XiangqiLibraryComponent,
    headline: String,
    subline: String,
    modifier: Modifier = Modifier,
) {
    val localTitle = stringResource(R.string.xiangqi_mode_local)
    val aiTitle = stringResource(R.string.xiangqi_mode_ai)
    val aiVsAiTitle = stringResource(R.string.xiangqi_mode_ai_vs_ai)
    val libraryTitle = stringResource(R.string.xiangqi_library_title)

    var importFen by remember { mutableStateOf(false) }
    var importJson by remember { mutableStateOf(false) }
    var showOnlineMatch by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = headline,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = subline,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        val actions = listOf(
            QuickAction(
                label = stringResource(R.string.xiangqi_new_local_game),
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineGroup,
                onClick = { component.createLocalGame(localTitle) },
            ),
            QuickAction(
                label = stringResource(R.string.xiangqi_new_ai_as_black),
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAvatarDefault,
                onClick = {
                    ActionUtils.showLogin(source = "xiangqi_ai_black") {
                        component.createAiGame(aiTitle, Side.BLACK)
                    }
                },
            ),
            QuickAction(
                label = stringResource(R.string.xiangqi_new_ai_as_red),
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineRobot,
                onClick = {
                    ActionUtils.showLogin(source = "xiangqi_ai_red") {
                        component.createAiGame(aiTitle, Side.RED)
                    }
                },
            ),
            QuickAction(
                label = stringResource(R.string.xiangqi_new_ai_vs_ai),
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCasino,
                onClick = {
                    ActionUtils.showLogin(source = "xiangqi_ai_vs_ai") {
                        component.createAiVsAiGame(aiVsAiTitle)
                    }
                },
            ),
            QuickAction(
                label = stringResource(R.string.xiangqi_new_online_game),
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePublic,
                onClick = {
                    ActionUtils.showLogin(source = "xiangqi_online_empty") {
                        showOnlineMatch = true
                    }
                },
            ),
            QuickAction(
                label = stringResource(R.string.xiangqi_import_fen),
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineUploadFile,
                onClick = {
                    ActionUtils.showLogin(source = "xiangqi_import_fen") {
                        importFen = true
                    }
                },
            ),
            QuickAction(
                label = stringResource(R.string.xiangqi_import_json),
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMemory,
                onClick = {
                    ActionUtils.showLogin(source = "xiangqi_import_json") {
                        importJson = true
                    }
                },
            ),
        )

        // 3 列 x 2 行
        actions.chunked(3).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                row.forEach { action ->
                    QuickActionCard(
                        action = action,
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f),
                    )
                }
                repeat(3 - row.size) { Box(modifier = Modifier.weight(1f)) }
            }
        }
    }

    if (importFen) {
        XiangqiImportDialog(
            title = stringResource(R.string.xiangqi_import_dialog_title),
            hint = stringResource(R.string.xiangqi_import_hint_fen),
            onDismiss = { importFen = false },
            onConfirm = { text ->
                component.importFen(libraryTitle, text)
                importFen = false
            },
        )
    }
    if (importJson) {
        XiangqiImportDialog(
            title = stringResource(R.string.xiangqi_import_dialog_title),
            hint = stringResource(R.string.xiangqi_import_hint_json),
            onDismiss = { importJson = false },
            onConfirm = { text ->
                component.importJson(libraryTitle, text)
                importJson = false
            },
        )
    }
    if (showOnlineMatch) {
        val context = LocalContext.current
        val entryPoint = remember {
            EntryPointAccessors.fromApplication(
                context.applicationContext,
                XiangqiOnlineEntryPoint::class.java,
            )
        }
        val onlinePlay = remember { entryPoint.onlinePlayUseCase() }
        OnlineMatchScreen(
            onlinePlay = onlinePlay,
            onDismiss = { showOnlineMatch = false },
            onMatchReady = { roomId, mySide, opponentName, opponentAvatarUrl, initialFen ->
                showOnlineMatch = false
                component.createOnlineGame(roomId, mySide, opponentName, opponentAvatarUrl, initialFen)
            },
        )
    }
}

private data class QuickAction(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
)

@Composable
private fun QuickActionCard(
    action: QuickAction,
    modifier: Modifier = Modifier,
) {
    GlassSurface(
        onClick = action.onClick,
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(20.dp),
        style = GlassStyle.Medium,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = action.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = action.label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
