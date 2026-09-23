package com.wanbaohe.gomoku.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.ui.BaseScreen
import com.wanbaohe.boardgame.model.GameCardData
import com.wanbaohe.boardgame.model.GameCardLabels
import com.wanbaohe.boardgame.model.GameCardPlayer
import com.wanbaohe.boardgame.ui.GameCard
import com.wanbaohe.gomoku.R
import com.wanbaohe.gomoku.component.GomokuLibraryComponent
import com.wanbaohe.gomoku.data.GomokuGameSummary
import com.wanbaohe.gomoku.domain.model.GameMode
import com.wanbaohe.gomoku.domain.model.GameStatus
import com.wanbaohe.gomoku.domain.model.PlayerType
import com.wanbaohe.gomoku.presentation.localizedGameResultText

@Composable
fun GomokuLibraryScreen(
    component: GomokuLibraryComponent,
    modifier: Modifier = Modifier,
    showChrome: Boolean = true,
) {
    val libraryTitle = stringResource(R.string.gomoku_library_title)

    val content: @Composable (Modifier) -> Unit = { contentModifier ->
        GomokuLibraryContent(
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
private fun GomokuLibraryContent(
    component: GomokuLibraryComponent,
    modifier: Modifier,
) {
    if (component.games.isEmpty()) {
        GomokuEmptyQuickPanel(
            component = component,
            headline = stringResource(R.string.gomoku_empty_library_title),
            subline = stringResource(R.string.gomoku_empty_library_message),
            modifier = modifier.fillMaxSize(),
        )
        return
    }

    val labels = GameCardLabels(
        renameTitle = stringResource(R.string.gomoku_rename_game),
        renameHint = stringResource(R.string.gomoku_rename_hint),
        deleteConfirmTitle = stringResource(R.string.gomoku_delete_confirm_title),
        deleteConfirmMessage = stringResource(R.string.gomoku_delete_confirm_message),
        confirm = stringResource(R.string.gomoku_confirm),
        cancel = stringResource(R.string.gomoku_cancel),
        renameContentDescription = stringResource(R.string.gomoku_rename_game),
        deleteContentDescription = stringResource(R.string.gomoku_delete_game),
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(component.games, key = { it.id }) { item ->
            GameCard(
                data = item.toCardData(),
                labels = labels,
                // 已结束的局点卡片的本意是"复盘",进行中的才是"继续下"
                onContinue = {
                    if (item.status.isFinished()) {
                        component.openAnalysis(item.id)
                    } else {
                        component.openGame(item.id)
                    }
                },
                onAnalysis = { component.openAnalysis(item.id) },
                onDelete = { component.deleteGame(item.id) },
                onRename = { newTitle -> component.renameGame(item.id, newTitle) },
            )
        }
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun GomokuGameSummary.toCardData(): GameCardData {
    val isLocal = mode == GameMode.LOCAL_PVP
    val modeText = when (mode) {
        GameMode.LOCAL_PVP -> stringResource(R.string.gomoku_mode_local)
        GameMode.ONLINE_PVP -> stringResource(R.string.gomoku_mode_online)
        GameMode.LLM_VS_LLM -> stringResource(R.string.gomoku_mode_ai_vs_ai)
        GameMode.HUMAN_VS_LLM -> stringResource(R.string.gomoku_mode_ai)
    }

    val blackName = when {
        blackPlayerType == PlayerType.LLM -> stringResource(R.string.gomoku_player_ai)
        isLocal -> stringResource(R.string.gomoku_player_local_black)
        else -> stringResource(R.string.gomoku_player_you)
    }
    val whiteName = when {
        whitePlayerType == PlayerType.LLM -> stringResource(R.string.gomoku_player_ai)
        isLocal -> stringResource(R.string.gomoku_player_local_white)
        else -> stringResource(R.string.gomoku_player_you)
    }

    return GameCardData(
        title = title,
        statusText = when (status) {
            GameStatus.NOT_STARTED -> stringResource(R.string.gomoku_library_status_not_started)
            GameStatus.PAUSED -> stringResource(R.string.gomoku_library_status_paused)
            GameStatus.PLAYING -> stringResource(R.string.gomoku_library_status_in_progress)
            else -> stringResource(R.string.gomoku_library_status_game_over)
        },
        modeBadge = modeText,
        updatedAt = updatedAt,
        firstPlayer = GameCardPlayer(
            badge = stringResource(R.string.gomoku_side_black),
            name = blackName,
            badgeColor = MaterialTheme.colorScheme.onSurface,
            badgeBackground = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            badgeBorder = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
        ),
        secondPlayer = GameCardPlayer(
            badge = stringResource(R.string.gomoku_side_white),
            name = whiteName,
            badgeColor = MaterialTheme.colorScheme.onPrimaryContainer,
            badgeBackground = MaterialTheme.colorScheme.primaryContainer,
            badgeBorder = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
        ),
        vsLabel = stringResource(R.string.gomoku_vs_short),
        plyCountText = stringResource(R.string.gomoku_ply_count, plyCount),
        resultText = localizedGameResultText(resultText),
        // 进行中的局卡片点击就是进回放,"查看回放"在这里语义重叠,用「复盘」区分
        actionLabel = stringResource(
            if (status.isFinished()) {
                R.string.gomoku_library_open_analysis
            } else {
                R.string.gomoku_library_review
            },
        ),
    )
}

/** 终局判定:只有这些状态点卡片时才按"复盘"处理。 */
private fun GameStatus.isFinished(): Boolean = when (this) {
    GameStatus.BLACK_WINS, GameStatus.WHITE_WINS, GameStatus.DRAW, GameStatus.RESIGNED -> true
    else -> false
}
