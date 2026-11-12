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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.ui.BaseScreen
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.line.LineHelp
import com.wanbaohe.core.ui.widget.enhanced.EnhancedAlertDialog
import com.wanbaohe.core.ui.widget.enhanced.EnhancedButton
import com.wanbaohe.core.ui.widget.system.OneBoxDesignSystem
import com.wanbaohe.core.ui.widget.system.OneBoxSectionCard
import com.wanbaohe.mahjong.R
import com.wanbaohe.mahjong.component.MahjongComponent
import com.wanbaohe.mahjong.component.MahjongScreenPhase
import com.wanbaohe.mahjong.component.MahjongUiState
import com.wanbaohe.mahjong.domain.model.FanType
import com.wanbaohe.mahjong.domain.model.GamePhase
import com.wanbaohe.mahjong.domain.model.Tiles

@Composable
fun MahjongScreen(component: MahjongComponent) {
    val state by component.uiState.collectAsState()
    when (state.screen) {
        MahjongScreenPhase.HOME -> MahjongHome(component, state)
        MahjongScreenPhase.GAME -> MahjongGameScreen(component, state)
    }

    if (state.showRules) {
        EnhancedAlertDialog(
            visible = true,
            onDismissRequest = { component.showRules(false) },
            title = { Text(stringResource(R.string.mahjong_rules)) },
            text = {
                Text(
                    text = stringResource(R.string.mahjong_rules_text),
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                )
            },
            confirmButton = {
                EnhancedButton(onClick = { component.showRules(false) }) {
                    Text(stringResource(R.string.mahjong_close))
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        )
    }

    val result = state.game?.result
    if (state.game?.phase == GamePhase.FINISHED && result != null) {
        val title = when {
            result.isDraw -> R.string.mahjong_round_draw
            result.wins.any { it.seat == Tiles.HUMAN_SEAT } -> R.string.mahjong_round_win
            else -> R.string.mahjong_round_loss
        }
        EnhancedAlertDialog(
            visible = true,
            onDismissRequest = component::returnHome,
            title = { Text(stringResource(title)) },
            text = {
                Column(
                    modifier = Modifier
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    result.wins.forEach { win ->
                        Text(
                            text = stringResource(
                                R.string.mahjong_winner_seat,
                                seatLabel(win.seat),
                                win.points,
                            ),
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = win.fans.map { fanLabel(it) }.joinToString(" · "),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    result.scoreChanges.forEachIndexed { seat, change ->
                        Text(
                            text = stringResource(
                                R.string.mahjong_score_change,
                                seatLabel(seat),
                                change,
                            ),
                            color = if (seat == Tiles.HUMAN_SEAT) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            },
            confirmButton = {
                EnhancedButton(onClick = component::startNewGame) {
                    Text(stringResource(R.string.mahjong_play_again))
                }
            },
            dismissButton = {
                EnhancedButton(onClick = component::returnHome) {
                    Text(stringResource(R.string.mahjong_back_home))
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        )
    }
}

@Composable
private fun MahjongHome(component: MahjongComponent, state: MahjongUiState) {
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = OneBoxDesignSystem.screenPadding),
            verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.blockSpacing),
        ) {
            Spacer(Modifier.height(OneBoxDesignSystem.screenTopSpacing))
            Text(
                text = stringResource(R.string.mahjong_subtitle),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.mahjong_offline),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (state.hasSavedGame) {
                OneBoxSectionCard(onClick = component::continueSavedGame) {
                    Column(Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(R.string.mahjong_continue_game),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = stringResource(R.string.mahjong_continue_description),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            OneBoxSectionCard(onClick = component::startNewGame) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.mahjong_new_game),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = stringResource(R.string.mahjong_rules_text),
                            maxLines = 3,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            OneBoxSectionCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = stringResource(R.string.mahjong_stats),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Row(Modifier.fillMaxWidth()) {
                        StatItem(
                            label = stringResource(R.string.mahjong_stats_games),
                            value = state.stats.games,
                            modifier = Modifier.weight(1f),
                        )
                        StatItem(
                            label = stringResource(R.string.mahjong_stats_wins),
                            value = state.stats.wins,
                            modifier = Modifier.weight(1f),
                        )
                        StatItem(
                            label = stringResource(R.string.mahjong_stats_draws),
                            value = state.stats.draws,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (state.stats.games > 0) {
                        Text(
                            text = stringResource(R.string.mahjong_best_score, state.stats.bestScore),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            Spacer(Modifier.height(OneBoxDesignSystem.blockSpacing))
        }
    }
}

@Composable
private fun StatItem(label: String, value: Int, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.Bold,
        )
        Text(text = label, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
internal fun seatLabel(seat: Int): String = stringResource(
    when (seat) {
        Tiles.HUMAN_SEAT -> R.string.mahjong_you
        1 -> R.string.mahjong_right
        2 -> R.string.mahjong_opposite
        else -> R.string.mahjong_left
    }
)

@Composable
private fun fanLabel(fan: FanType): String = stringResource(
    when (fan) {
        FanType.BASE -> R.string.mahjong_fan_base
        FanType.SEVEN_PAIRS -> R.string.mahjong_fan_seven_pairs
        FanType.ALL_PUNGS -> R.string.mahjong_fan_all_pungs
        FanType.ONE_SUIT -> R.string.mahjong_fan_one_suit
        FanType.SELF_DRAW -> R.string.mahjong_fan_self_draw
        FanType.ROB_KONG -> R.string.mahjong_fan_rob_kong
    }
)
