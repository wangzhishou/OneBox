package com.wanbaohe.gomoku.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalButton
import com.wanbaohe.gomoku.R
import com.wanbaohe.gomoku.application.dto.engineSlotFor
import com.wanbaohe.gomoku.application.port.outbound.EngineSlot
import com.wanbaohe.gomoku.domain.SetupPositionValidator
import com.wanbaohe.gomoku.domain.model.Side
import com.wanbaohe.gomoku.router.screenLogic.GomokuRouterComponent
import com.wanbaohe.gomoku.ui.GomokuOpponentPicker
import com.wanbaohe.gomoku.ui.board.GomokuBoard

@Composable
fun GomokuPlayHomeScreen(component: GomokuRouterComponent, modifier: Modifier = Modifier) {
    val defaults by component.gomokuAiConfig.collectAsState()
    val working by component.currentAIEngine.collectAsState()
    val duelA by component.duelEngineA.collectAsState()
    val duelB by component.duelEngineB.collectAsState()
    val state = remember(component.preparation, defaults, working, duelA, duelB,
        component.isRestoringRecentGame, component.isStartingPreparation) { component.preparationUiState() }
    var pickingSide by remember { mutableStateOf<Side?>(null) }
    var showOrigin by remember { mutableStateOf(false) }
    val setup = component.preparationSetupDraft
    if (setup != null) {
        GomokuSetupScreen(
            setup, component::updatePreparationSetup, component::cancelPreparationSetup, component::finishPreparationSetup,
            stringResource(R.string.gomoku_setup_apply), modifier,
            bottomSide = state.humanOrBottomSide(),
        )
        return
    }
    val issue = remember(state.boardState) { SetupPositionValidator.validate(state.boardState) }
    Column(
        modifier = modifier.fillMaxSize().padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        GomokuPlayersRow(state, { pickingSide = it }, Modifier.fillMaxWidth().padding(horizontal = 12.dp))
        Box(Modifier.fillMaxWidth().weight(1f).padding(horizontal = 8.dp), contentAlignment = Alignment.Center) {
            GomokuBoard(state.boardState, null, emptySet(), { _, _ -> }, bottomSide = state.humanOrBottomSide())
            if (component.isRestoringRecentGame) CircularProgressIndicator()
        }
        if (issue != null) {
            Text(setupIssueText(issue), maxLines = 2, overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 12.dp))
        }
        if (state.origin != null) {
            TextButton(onClick = { showOrigin = true }) {
                Text(stringResource(R.string.gomoku_setup_source), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(
                onClick = component::beginPreparationSetup,
                enabled = state.isLoaded && !state.isUpdating, modifier = Modifier.weight(1f),
            ) { Text(stringResource(R.string.gomoku_setup_action), maxLines = 1, overflow = TextOverflow.Ellipsis) }
            GlassTonalButton(
                onClick = component::startPreparation,
                enabled = state.isLoaded && !state.isUpdating && issue == null,
                modifier = Modifier.weight(1.5f),
            ) {
                Text(stringResource(if (state.isUpdating) R.string.gomoku_starting else R.string.gomoku_start_play),
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            NewGameDropMenu(component.libraryComponent, Modifier.weight(1f),
                label = stringResource(R.string.gomoku_more_modes), openAbove = true)
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
        GomokuOpponentPicker(component.preparationAiConfig(side), engine, engines, models,
            { component.switchPreparationAi(side, it) },
            { selected, model -> component.switchPreparationModel(side, selected, model) },
            { pickingSide = null })
    }
    state.origin?.let { origin ->
        if (showOrigin) GomokuOriginDialog(origin,
            { showOrigin = false; component.libraryComponent.openSourceGame(origin.gameId) }, { showOrigin = false })
    }
}
