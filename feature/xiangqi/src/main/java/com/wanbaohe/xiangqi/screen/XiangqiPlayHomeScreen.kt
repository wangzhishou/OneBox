package com.wanbaohe.xiangqi.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wanbaohe.boardgame.ui.BoardGamePillButton
import com.wanbaohe.boardgame.R as BoardGameR
import com.wanbaohe.xiangqi.R
import com.wanbaohe.xiangqi.application.dto.engineSlotFor
import com.wanbaohe.xiangqi.domain.SetupPositionValidator
import com.wanbaohe.xiangqi.domain.model.Side
import com.wanbaohe.xiangqi.router.screenLogic.XiangqiRouterComponent
import com.wanbaohe.xiangqi.ui.XiangqiOpponentPicker
import com.wanbaohe.xiangqi.ui.board.XiangqiBoard
import com.wanbaohe.xiangqi.ui.board.xiangqiBoardAspectRatio

@Composable
fun XiangqiPlayHomeScreen(component: XiangqiRouterComponent, modifier: Modifier = Modifier) {
    val defaults by component.xiangqiAiConfig.collectAsState()
    val workingModel by component.currentAIEngine.collectAsState()
    val duelA by component.duelEngineA.collectAsState()
    val duelB by component.duelEngineB.collectAsState()
    val localEngine by component.localEngineInstallState.collectAsState()
    val state = remember(component.preparation, defaults, workingModel, duelA, duelB, component.isRestoringRecentGame, component.isStartingPreparation) {
        component.preparationUiState()
    }
    var pickingSide by remember { mutableStateOf<Side?>(null) }
    var showOrigin by remember { mutableStateOf(false) }
    val setup = component.preparationSetupDraft

    if (setup != null) {
        XiangqiSetupScreen(
            draft = setup,
            onDraftChange = component::updatePreparationSetup,
            onCancel = component::cancelPreparationSetup,
            onConfirm = component::finishPreparationSetup,
            modifier = modifier,
        )
        return
    }

    Column(
        modifier = modifier.fillMaxSize().padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        XiangqiPlayersRow(
            state = state,
            onPickAiFor = { pickingSide = it },
            onShowOrigin = { showOrigin = true },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
        )
        BoxWithConstraints(Modifier.fillMaxWidth().weight(1f).padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
            XiangqiBoard(
                state.boardState, null, emptySet(), { _, _ -> },
                boardAspectRatio = xiangqiBoardAspectRatio(maxWidth.value, maxHeight.value),
            )
            if (component.isRestoringRecentGame) CircularProgressIndicator()
        }
        val issue = remember(state.boardState) { SetupPositionValidator.validate(state.boardState) }
        if (issue != null) {
            Text(
                setupIssueText(issue),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 12.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BoardGamePillButton(
                label = stringResource(R.string.xiangqi_setup_action),
                icon = Icons.Outlined.GridOn,
                onClick = component::beginPreparationSetup,
                enabled = state.isLoaded && !state.isUpdating,
                modifier = Modifier.weight(1f),
            )
            BoardGamePillButton(
                label = stringResource(if (state.isUpdating) R.string.xiangqi_setup_starting else R.string.xiangqi_start_play),
                icon = Icons.Filled.PlayArrow,
                onClick = component::startPreparation,
                enabled = state.isLoaded && !state.isUpdating && issue == null,
                modifier = Modifier.weight(1.6f),
                primary = true,
            )
            NewGameDropMenu(
                component = component.libraryComponent,
                modifier = Modifier.weight(1f),
                openAbove = true,
                trigger = { openMenu ->
                    BoardGamePillButton(
                        label = stringResource(BoardGameR.string.boardgame_more),
                        icon = Icons.Outlined.MoreHoriz,
                        onClick = openMenu,
                        enabled = state.isLoaded && !state.isUpdating,
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
            )
        }
    }

    pickingSide?.let { side ->
        val engines by component.allAiEngines.collectAsState()
        val models by component.modelsByProvider.collectAsState()
        val engine = when (component.preparation.setup.mode.engineSlotFor(side)) {
            com.wanbaohe.xiangqi.application.port.outbound.EngineSlot.FAST -> workingModel
            com.wanbaohe.xiangqi.application.port.outbound.EngineSlot.DUEL_A -> duelA
            com.wanbaohe.xiangqi.application.port.outbound.EngineSlot.DUEL_B -> duelB
        }
        XiangqiOpponentPicker(
            config = component.preparationAiConfig(side),
            workingEngine = engine,
            allEngines = engines,
            modelsByProvider = models,
            localEnginePackaged = component.isLocalEnginePackaged,
            localEngineState = localEngine,
            onSourceSelected = { component.switchPreparationAi(side, it) },
            onModelSelected = { selectedEngine, model -> component.switchPreparationModel(side, selectedEngine, model) },
            onDismiss = { pickingSide = null },
            onDownloadLocalEngine = component::downloadLocalEngine,
            onCancelLocalEngineDownload = component::cancelLocalEngineDownload,
        )
    }
    val origin = state.origin
    if (showOrigin && origin != null) {
        XiangqiOriginDialog(
            origin = origin,
            onContinueSource = { showOrigin = false; component.libraryComponent.openSourceGame(origin.gameId) },
            onDismiss = { showOrigin = false },
        )
    }
}
