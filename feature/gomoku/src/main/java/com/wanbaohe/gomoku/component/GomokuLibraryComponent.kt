package com.wanbaohe.gomoku.component

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.arkivanov.decompose.ComponentContext
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.wanbaohe.gomoku.application.usecase.CreateGameUseCase
import com.wanbaohe.gomoku.application.usecase.DeleteGameUseCase
import com.wanbaohe.gomoku.application.usecase.GameQueryUseCase
import com.wanbaohe.gomoku.application.usecase.ImportFailureCause
import com.wanbaohe.gomoku.application.usecase.ImportGameUseCase
import com.wanbaohe.gomoku.application.usecase.ImportResult
import com.wanbaohe.gomoku.application.usecase.ManageGameUseCase
import com.wanbaohe.gomoku.application.dto.GamePreparation
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.interfaces.singleton.AppContext
import com.wanbaohe.gomoku.R
import com.wanbaohe.gomoku.data.GomokuGameSummary
import com.wanbaohe.gomoku.domain.model.Side
import com.wanbaohe.gomoku.domain.FenCodec
import com.wanbaohe.gomoku.domain.model.GameSetup
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.launch

class GomokuLibraryComponent @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigate: (Screen) -> Unit,
    @Assisted private val onPrepareGame: (GamePreparation) -> Unit,
    private val createGame: CreateGameUseCase,
    private val gameQuery: GameQueryUseCase,
    private val importGame: ImportGameUseCase,
    private val deleteGameUseCase: DeleteGameUseCase,
    private val manageGame: ManageGameUseCase,
    dispatchersHolder: DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext) {

    /** Offline menu choices prepare a draft; only Start creates a game. */
    fun startAiGame(title: String, aiSide: Side) {
        createAiGame(title, aiSide)
    }

    fun startAiVsAiGame(title: String) {
        createAiVsAiGame(title)
    }


    var games by mutableStateOf<List<GomokuGameSummary>>(emptyList())
        private set

    init {
        componentScope.launch {
            gameQuery.observeAll().collect { list ->
                games = list
            }
        }
    }

    fun createLocalGame(title: String) {
        onPrepareGame(GamePreparation(title = title, setup = GameSetup.local()))
    }

    fun createAiGame(title: String, aiSide: Side) {
        onPrepareGame(GamePreparation(title = title, setup = GameSetup.humanVsAi(aiSide)))
    }

    fun createAiVsAiGame(title: String) {
        onPrepareGame(GamePreparation(title = title, setup = GameSetup.aiVsAi()))
    }

    fun createOnlineGame(
        roomId: String,
        mySide: Side,
        opponentName: String = AppContext.getString(R.string.gomoku_player_remote),
        opponentAvatarUrl: String = "",
        initialFen: String,
    ) {
        componentScope.launch {
            val sideName = if (mySide == Side.BLACK) {
                AppContext.getString(R.string.gomoku_side_black)
            } else {
                AppContext.getString(R.string.gomoku_side_white)
            }
            val title = "$sideName vs $opponentName"
            val gameId = createGame.createOnline(
                title = title,
                mySide = mySide,
                initialFen = initialFen,
                roomId = roomId,
                opponentName = opponentName,
                opponentAvatarUrl = opponentAvatarUrl,
            )
            navigateToGame(gameId)
        }
    }

    fun importFen(title: String, fen: String, defaultTitle: String) {
        val board = runCatching { FenCodec.parse(fen) }.getOrNull()
        if (board == null) ActionUtils.showToast(R.string.gomoku_invalid_fen)
        else onPrepareGame(GamePreparation(
            title = title.ifBlank { defaultTitle }, setup = GameSetup.local(), initialFen = FenCodec.encode(board),
        ))
    }

    fun openSourceGame(gameId: String) {
        componentScope.launch {
            if (gameQuery.getById(gameId) == null) ActionUtils.showToast(R.string.gomoku_game_missing)
            else navigateToGame(gameId)
        }
    }

    /** 导入 JSON 棋谱（含着法）。[defaultTitle] 同 [importFen]。 */
    fun importJson(title: String, json: String, defaultTitle: String) {
        componentScope.launch {
            when (val result = importGame.importJson(title, json, defaultTitle)) {
                is ImportResult.Failure -> ActionUtils.showToast(
                    when (result.cause) {
                        ImportFailureCause.INVALID_FEN -> R.string.gomoku_invalid_fen
                        ImportFailureCause.INVALID_JSON -> R.string.gomoku_invalid_json
                    },
                )
                is ImportResult.Success -> {
                    notifyImportOutcome(result)
                    navigateToGame(result.gameId)
                }
            }
        }
    }

    /**
     * 局部失败也要说清楚：静默丢着法会直接摧毁用户对导入功能的信任。
     */
    private fun notifyImportOutcome(result: ImportResult.Success) {
        when {
            result.hasSkipped -> ActionUtils.showToast(
                AppContext.getQuantityString(
                    R.plurals.gomoku_import_partial,
                    result.importedPlies,
                    result.importedPlies,
                    result.skippedPlies.size,
                ),
            )
            result.importedPlies > 0 -> ActionUtils.showToast(R.string.gomoku_import_success)
            // FEN 导入只有局面、没有着法，不必提示"导入成功"
            else -> Unit
        }
    }

    fun openGame(gameId: String) {
        navigateToGame(gameId)
    }

    fun openAnalysis(gameId: String) {
        onNavigate(Screen.GomokuRouter(Screen.GomokuRouter.Type.Analysis(gameId)))
    }

    fun deleteGame(gameId: String) {
        componentScope.launch { deleteGameUseCase.delete(gameId) }
    }

    fun renameGame(gameId: String, newTitle: String) {
        componentScope.launch { manageGame.rename(gameId, newTitle) }
    }

    private fun navigateToGame(gameId: String) {
        onNavigate(Screen.GomokuRouter(Screen.GomokuRouter.Type.Game(gameId)))
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
            onPrepareGame: (GamePreparation) -> Unit,
        ): GomokuLibraryComponent
    }
}
