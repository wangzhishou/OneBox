/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.wanbaohe.mahjong.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.ui.BaseScreen
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.line.LineHelp
import com.wanbaohe.core.ui.widget.enhanced.EnhancedButton
import com.wanbaohe.core.ui.widget.glass.glassThin
import com.wanbaohe.core.ui.widget.system.OneBoxDesignSystem
import com.wanbaohe.mahjong.R
import com.wanbaohe.mahjong.component.MahjongComponent
import com.wanbaohe.mahjong.component.MahjongUiState
import com.wanbaohe.mahjong.domain.model.ClaimType
import com.wanbaohe.mahjong.domain.model.GamePhase
import com.wanbaohe.mahjong.domain.model.MahjongAction
import com.wanbaohe.mahjong.domain.model.Meld
import com.wanbaohe.mahjong.domain.model.PlayerState
import com.wanbaohe.mahjong.domain.model.Tiles

@Composable
fun MahjongGameScreen(component: MahjongComponent, state: MahjongUiState) {
    val game = state.game ?: return
    BaseScreen(
        title = stringResource(R.string.mahjong_title),
        onGoBack = component::leaveModule,
        actions = {
            IconButton(onClick = { component.showRules(true) }) {
                Icon(
                    imageVector = Icons.Outlined.LineHelp,
                    contentDescription = stringResource(R.string.mahjong_rules),
                )
            }
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
        ) {
            GameStatus(state)
            OpponentSeat(
                player = game.players[2],
                seat = 2,
                isCurrent = game.currentSeat == 2,
                isDealer = game.dealerSeat == 2,
                backWidth = 8.dp,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                OpponentSeat(
                    player = game.players[3],
                    seat = 3,
                    isCurrent = game.currentSeat == 3,
                    isDealer = game.dealerSeat == 3,
                    backWidth = 5.dp,
                    modifier = Modifier.weight(0.24f),
                )
                DiscardsPanel(
                    players = game.players,
                    modifier = Modifier.weight(0.52f),
                )
                OpponentSeat(
                    player = game.players[1],
                    seat = 1,
                    isCurrent = game.currentSeat == 1,
                    isDealer = game.dealerSeat == 1,
                    backWidth = 5.dp,
                    modifier = Modifier.weight(0.24f),
                )
            }
            HumanArea(component, state)
            ActionArea(component, state)
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun GameStatus(state: MahjongUiState) {
    val game = state.game ?: return
    val hasClaim = state.humanActions.any { it is MahjongAction.Respond }
    val hasTurn = state.humanActions.any { it !is MahjongAction.Respond }
    val status = when {
        state.isAiThinking -> stringResource(R.string.mahjong_ai_thinking)
        hasClaim -> stringResource(R.string.mahjong_your_claim)
        hasTurn -> stringResource(R.string.mahjong_your_turn)
        else -> stringResource(R.string.mahjong_waiting)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.mahjong_wall, game.wall.size),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = status,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OpponentSeat(
    player: PlayerState,
    seat: Int,
    isCurrent: Boolean,
    isDealer: Boolean,
    backWidth: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        PlayerLabel(seat, player.score, isCurrent, isDealer)
        FlowRow(
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.Center,
        ) {
            repeat(player.hand.size) { MahjongTileBack(width = backWidth) }
        }
        FlowRow(
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            player.melds.forEach { meld ->
                MeldTiles(meld = meld, hideFace = meld.concealed, tileWidth = 14.dp)
            }
        }
    }
}

@Composable
private fun PlayerLabel(seat: Int, score: Int, isCurrent: Boolean, isDealer: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = seatLabel(seat),
            style = MaterialTheme.typography.labelLarge,
            color = if (isCurrent) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
        )
        if (isDealer) {
            Text(
                text = " · " + stringResource(R.string.mahjong_dealer),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        Text(
            text = " · " + stringResource(R.string.mahjong_score, score),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DiscardsPanel(players: List<PlayerState>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .glassThin(shape = RoundedCornerShape(OneBoxDesignSystem.largeRadius))
            .padding(6.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = stringResource(R.string.mahjong_discards),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
        )
        players.forEachIndexed { seat, player ->
            Text(
                text = seatLabel(seat),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(1.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                player.discards.forEach { tileId ->
                    MahjongTile(tileId = tileId, width = 14.dp)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HumanArea(component: MahjongComponent, state: MahjongUiState) {
    val game = state.game ?: return
    val player = game.players[Tiles.HUMAN_SEAT]
    PlayerLabel(
        seat = Tiles.HUMAN_SEAT,
        score = player.score,
        isCurrent = game.currentSeat == Tiles.HUMAN_SEAT,
        isDealer = game.dealerSeat == Tiles.HUMAN_SEAT,
    )
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        player.melds.forEach { meld -> MeldTiles(meld = meld, hideFace = false, tileWidth = 18.dp) }
        if (player.melds.isEmpty()) {
            Text(
                text = stringResource(R.string.mahjong_no_melds),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    Spacer(Modifier.height(10.dp))
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val spacing = 1.dp
        val tileWidth = ((maxWidth - spacing * 13) / 14).coerceIn(18.dp, 34.dp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.Bottom,
        ) {
            player.hand.forEach { tileId ->
                MahjongTile(
                    tileId = tileId,
                    width = tileWidth,
                    selected = state.selectedTileId == tileId,
                    onClick = { component.onTileSelected(tileId) },
                )
            }
        }
    }
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun MeldTiles(meld: Meld, hideFace: Boolean, tileWidth: androidx.compose.ui.unit.Dp) {
    Row(
        modifier = Modifier.padding(horizontal = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        meld.tileIds.forEach { tileId ->
            if (hideFace) MahjongTileBack(tileWidth) else MahjongTile(tileId, tileWidth)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ActionArea(component: MahjongComponent, state: MahjongUiState) {
    if (state.game?.phase == GamePhase.FINISHED) return
    val actions = state.humanActions
    val specialActions = actions.filterNot { it is MahjongAction.Discard }
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        specialActions.forEach { action ->
            EnhancedButton(onClick = { component.performHumanAction(action) }) {
                Text(actionLabel(action))
            }
        }
        if (actions.any { it is MahjongAction.Discard }) {
            EnhancedButton(
                onClick = component::discardSelected,
                enabled = state.selectedTileId != null,
            ) {
                Text(stringResource(R.string.mahjong_discard))
            }
        }
    }
}

@Composable
private fun actionLabel(action: MahjongAction): String = when (action) {
    is MahjongAction.SelfHu -> stringResource(R.string.mahjong_hu)
    is MahjongAction.ConcealedKong, is MahjongAction.AddedKong ->
        stringResource(R.string.mahjong_kong)
    is MahjongAction.Respond -> when (action.choice.type) {
        ClaimType.HU -> stringResource(R.string.mahjong_hu)
        ClaimType.PONG -> stringResource(R.string.mahjong_pong)
        ClaimType.KONG -> stringResource(R.string.mahjong_kong)
        ClaimType.CHI -> stringResource(
            R.string.mahjong_chi_sequence,
            tileSequenceText(action.choice.chiStart),
        )
        ClaimType.PASS -> stringResource(R.string.mahjong_pass)
    }
    is MahjongAction.Discard -> stringResource(R.string.mahjong_discard)
}
