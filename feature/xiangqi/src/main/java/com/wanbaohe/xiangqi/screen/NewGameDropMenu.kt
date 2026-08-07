package com.wanbaohe.xiangqi.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.shifenmiao.base.ui.shapes.BubbleShape
import com.shifenmiao.base.utils.ActionUtils
import com.wanbaohe.xiangqi.R
import com.wanbaohe.xiangqi.component.XiangqiLibraryComponent
import com.wanbaohe.xiangqi.di.XiangqiOnlineEntryPoint
import com.wanbaohe.xiangqi.domain.model.Side
import dagger.hilt.android.EntryPointAccessors
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRobot
import com.t8rin.imagetoolbox.core.resources.icons.line.LineGroup
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePsychology
import com.t8rin.imagetoolbox.core.resources.icons.line.LineScience
import com.t8rin.imagetoolbox.core.resources.icons.line.LineUploadFile
import com.t8rin.imagetoolbox.core.resources.icons.line.LineHistoryEdu

@Composable
fun NewGameDropMenu(
    component: XiangqiLibraryComponent,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    var importFen by remember { mutableStateOf(false) }
    var importJson by remember { mutableStateOf(false) }
    var showOnlineMatch by remember { mutableStateOf(false) }

    val localTitle = stringResource(R.string.xiangqi_mode_local)
    val aiTitle = stringResource(R.string.xiangqi_mode_ai)
    val aiVsAiTitle = stringResource(R.string.xiangqi_mode_ai_vs_ai)
    val libraryTitle = stringResource(R.string.xiangqi_library_title)

    Box(modifier = modifier) {
        val rotation by animateFloatAsState(
            targetValue = if (expanded) 45f else 0f,
            animationSpec = tween(300),
            label = "menu_rotation",
        )
        IconButton(onClick = { expanded = !expanded }) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Add,
                contentDescription = null,
                modifier = Modifier.rotate(rotation),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (expanded) {
            val density = LocalDensity.current
            val bubbleShape = BubbleShape(
                arrowSize = 8.dp,
                arrowDirection = BubbleShape.ArrowDirection.Top,
                arrowAlignment = BubbleShape.ArrowAlignment.End,
                arrowOffset = 20.dp,
                cornerRadius = 8.dp,
            )
            Popup(
                alignment = Alignment.TopEnd,
                offset = IntOffset(
                    x = with(density) { 4.dp.roundToPx() },
                    y = with(density) { 40.dp.roundToPx() },
                ),
                onDismissRequest = { expanded = false },
                properties = PopupProperties(focusable = true),
            ) {
                Surface(
                    shape = bubbleShape,
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shadowElevation = 2.dp,
                    modifier = Modifier.width(220.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(
                            start = 8.dp,
                            end = 8.dp,
                            top = 14.dp,
                            bottom = 8.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        MenuItem(
                            label = stringResource(R.string.xiangqi_new_local_game),
                            icon = {
                                Icon(
                                    com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineHistoryEdu,
                                    null,
                                    Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            },
                            onClick = {
                                expanded = false
                                component.createLocalGame(localTitle)
                            },
                        )
                        MenuItem(
                            label = stringResource(R.string.xiangqi_new_ai_as_red),
                            icon = {
                                Icon(
                                    com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineRobot,
                                    null,
                                    Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            },
                            onClick = {
                                expanded = false
                                ActionUtils.showLogin(source = "xiangqi_ai_red") {
                                    component.createAiGame(aiTitle, Side.RED)
                                }
                            },
                        )
                        MenuItem(
                            label = stringResource(R.string.xiangqi_new_ai_as_black),
                            icon = {
                                Icon(
                                    com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePsychology,
                                    null,
                                    Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            },
                            onClick = {
                                expanded = false
                                ActionUtils.showLogin(source = "xiangqi_ai_black") {
                                    component.createAiGame(aiTitle, Side.BLACK)
                                }
                            },
                        )
                        MenuItem(
                            label = stringResource(R.string.xiangqi_new_ai_vs_ai),
                            icon = {
                                Icon(
                                    com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineScience,
                                    null,
                                    Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            },
                            onClick = {
                                expanded = false
                                ActionUtils.showLogin(source = "xiangqi_ai_vs_ai") {
                                    component.createAiVsAiGame(aiVsAiTitle)
                                }
                            },
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        )

                        MenuItem(
                            label = stringResource(R.string.xiangqi_new_online_game),
                            icon = {
                                Icon(
                                    com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineGroup,
                                    null,
                                    Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            },
                            onClick = {
                                expanded = false
                                ActionUtils.showLogin(source = "xiangqi_online") {
                                    showOnlineMatch = true
                                }
                            },
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        )

                        MenuItem(
                            label = stringResource(R.string.xiangqi_import_fen),
                            icon = {
                                Icon(
                                    com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineUploadFile,
                                    null,
                                    Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            },
                            onClick = {
                                expanded = false
                                ActionUtils.showLogin(source = "xiangqi_import_fen") {
                                    importFen = true
                                }
                            },
                        )
                        MenuItem(
                            label = stringResource(R.string.xiangqi_import_json),
                            icon = {
                                Icon(
                                    com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineUploadFile,
                                    null,
                                    Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            },
                            onClick = {
                                expanded = false
                                ActionUtils.showLogin(source = "xiangqi_import_json") {
                                    importJson = true
                                }
                            },
                        )
                    }
                }
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

@Composable
private fun MenuItem(
    label: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        icon()
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
