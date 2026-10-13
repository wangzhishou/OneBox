package com.wanbaohe.chess.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import com.wanbaohe.chess.R
import com.wanbaohe.chess.application.dto.engineSlotFor
import com.wanbaohe.chess.application.port.outbound.EngineSlot
import com.wanbaohe.chess.domain.SetupPositionValidator
import com.wanbaohe.chess.domain.model.Side
import com.wanbaohe.chess.router.screenLogic.ChessRouterComponent
import com.wanbaohe.chess.ui.ChessOpponentPicker
import com.wanbaohe.chess.ui.board.ChessBoard

@Composable
fun ChessPlayHomeScreen(component: ChessRouterComponent, modifier: Modifier = Modifier) {
    val defaults by component.chessAiConfig.collectAsState()
    val working by component.currentAIEngine.collectAsState()
    val duelA by component.duelEngineA.collectAsState()
    val duelB by component.duelEngineB.collectAsState()
    val state = remember(component.preparation, defaults, working, duelA, duelB,
        component.isRestoringRecentGame, component.isStartingPreparation) { component.preparationUiState() }
    var pickingSide by remember { mutableStateOf<Side?>(null) }
    var showOrigin by remember { mutableStateOf(false) }
    val draft = component.preparationSetupDraft
    if (draft != null) {
        ChessSetupScreen(
            draft = draft,
            onDraftChange = component::updatePreparationSetup,
            onCancel = component::cancelPreparationSetup,
            onConfirm = component::finishPreparationSetup,
            modifier = modifier,
            bottomSide = state.bottomSide(),
        )
        return
    }
    Column(
        modifier = modifier.fillMaxSize().padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ChessPlayersRow(
            state, { pickingSide = it }, { showOrigin = true },
            Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            emphasizeVs = true,
        )
        Box(Modifier.fillMaxWidth().weight(1f).padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
            Box(contentAlignment = Alignment.Center) {
                ChessBoard(state.boardState, null, emptySet(), { _, _ -> }, bottomSide = state.bottomSide())
                if (component.isRestoringRecentGame) CircularProgressIndicator()
            }
        }
        val issue = remember(state.boardState) { SetupPositionValidator.validate(state.boardState) }
        if (issue != null) Text(
            setupIssueText(issue), color = MaterialTheme.colorScheme.onErrorContainer,
            style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(horizontal = 12.dp),
            maxLines = 2, overflow = TextOverflow.Ellipsis,
        )
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BoardGamePillButton(
                label = stringResource(R.string.chess_setup_action),
                icon = Icons.Outlined.GridOn,
                onClick = component::beginPreparationSetup,
                enabled = state.isLoaded && !state.isUpdating,
                modifier = Modifier.weight(1f),
            )
            BoardGamePillButton(
                label = stringResource(if (state.isUpdating) R.string.chess_setup_starting else R.string.chess_start_action),
                icon = Icons.Filled.PlayArrow,
                onClick = component::startPreparation, enabled = state.isLoaded && !state.isUpdating && issue == null,
                modifier = Modifier.weight(1.6f),
                primary = true,
            )
            NewGameDropMenu(
                component.libraryComponent,
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
            EngineSlot.FAST -> working
            EngineSlot.DUEL_A -> duelA
            EngineSlot.DUEL_B -> duelB
        }
        ChessOpponentPicker(
            config = component.preparationAiConfig(side),
            workingEngine = engine,
            allEngines = engines,
            modelsByProvider = models,
            onSourceSelected = { component.switchPreparationAi(side, it) },
            onModelSelected = { provider, model -> component.switchPreparationModel(side, provider, model) },
            onDismiss = { pickingSide = null },
        )
    }
    state.origin?.let { origin ->
        if (showOrigin) ChessOriginDialog(
            origin, { showOrigin = false; component.libraryComponent.openSourceGame(origin.gameId) }, { showOrigin = false },
        )
    }
}
