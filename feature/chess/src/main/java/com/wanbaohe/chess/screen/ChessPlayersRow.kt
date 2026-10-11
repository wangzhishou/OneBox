package com.wanbaohe.chess.screen

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.shifenmiao.common.utils.BaseUtils
import com.t8rin.imagetoolbox.core.ui.utils.provider.LocalLoginState
import com.wanbaohe.boardgame.model.PlayerBarData
import com.wanbaohe.boardgame.ui.PlayersBar
import com.wanbaohe.chess.R
import com.wanbaohe.chess.component.ChessGameUiState
import com.wanbaohe.chess.domain.model.GameMode
import com.wanbaohe.chess.domain.model.GameStatus
import com.wanbaohe.chess.domain.model.PlayerType
import com.wanbaohe.chess.domain.model.Side

internal fun ChessGameUiState.bottomSide(): Side = when (mode) {
    GameMode.HUMAN_VS_LLM -> if (whitePlayerType == PlayerType.HUMAN) Side.WHITE else Side.BLACK
    GameMode.ONLINE_PVP -> onlineMySide
    else -> Side.WHITE
}

@Composable
internal fun ChessPlayersRow(
    state: ChessGameUiState,
    onPickAiFor: (Side) -> Unit,
    onShowOrigin: () -> Unit,
    modifier: Modifier = Modifier,
    emphasizeVs: Boolean = state.status == GameStatus.NOT_STARTED && state.startedAt == 0L,
) {
    val login = LocalLoginState.current
    val humanName = BaseUtils.getDisplayName(login.nickname, login.username)
        .ifBlank { stringResource(R.string.chess_player_you) }
    val bottom = state.bottomSide()
    val playable = state.isLoaded && (state.status == GameStatus.PLAYING || state.status == GameStatus.CHECK)

    @Composable
    fun player(side: Side): PlayerBarData {
        val type = if (side == Side.WHITE) state.whitePlayerType else state.blackPlayerType
        val sideLabel = stringResource(if (side == Side.WHITE) R.string.chess_side_white else R.string.chess_side_black)
        val active = playable && state.boardState.sideToMove == side
        val service = if (side == Side.WHITE) state.whiteAiServiceName else state.blackAiServiceName
        val model = if (side == Side.WHITE) state.whiteAiModelName else state.blackAiModelName
        val name = when (type) {
            PlayerType.LLM -> service.ifBlank { stringResource(R.string.chess_player_ai) }
            PlayerType.REMOTE -> state.onlineOpponentName.ifBlank { stringResource(R.string.chess_player_remote) }
            PlayerType.HUMAN -> if (state.mode == GameMode.LOCAL_PVP) sideLabel else humanName
        }
        val detail = when {
            active && type == PlayerType.LLM && state.isAiThinking -> stringResource(R.string.chess_player_thinking)
            active && type == PlayerType.HUMAN -> stringResource(R.string.chess_player_your_turn)
            type == PlayerType.LLM -> model
            type == PlayerType.HUMAN && state.mode != GameMode.LOCAL_PVP -> stringResource(R.string.chess_player_you)
            type == PlayerType.REMOTE -> stringResource(R.string.chess_player_remote)
            else -> ""
        }
        return PlayerBarData(
            name = name,
            subtitle = if (detail.isBlank()) sideLabel else "$sideLabel · $detail",
            avatarUrl = when (type) {
                PlayerType.HUMAN -> if (state.mode == GameMode.LOCAL_PVP) "" else login.avatar.orEmpty()
                PlayerType.REMOTE -> state.onlineOpponentAvatarUrl
                PlayerType.LLM -> ""
            },
            indicatorColor = if (side == Side.BLACK) MaterialTheme.colorScheme.onSurface else Color(0xFF9E9E9E),
            isActiveTurn = active,
            onClick = if (type == PlayerType.LLM && state.isLoaded && !state.isUpdating) ({ onPickAiFor(side) }) else null,
            actionLabel = if (type == PlayerType.LLM) stringResource(R.string.chess_player_change) else "",
            avatarIsLoggedIn = type != PlayerType.HUMAN || state.mode == GameMode.LOCAL_PVP || login.isLogin,
        )
    }

    val origin = state.origin
    PlayersBar(
        top = player(bottom.opposite()),
        bottom = player(bottom),
        vsLabel = stringResource(R.string.chess_vs_short),
        turnLabel = stringResource(R.string.chess_turn_badge),
        modifier = modifier,
        emphasizeVs = emphasizeVs,
        onVsClick = if (origin != null) onShowOrigin else null,
        vsActionLabel = if (origin != null) stringResource(R.string.chess_setup_source_ply, origin.ply)
            else stringResource(R.string.chess_vs_short),
    )
}
