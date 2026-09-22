package com.wanbaohe.xiangqi.component

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.arkivanov.decompose.ComponentContext
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.wanbaohe.xiangqi.application.usecase.CreateGameUseCase
import com.wanbaohe.xiangqi.application.usecase.DeleteGameUseCase
import com.wanbaohe.xiangqi.application.usecase.GameQueryUseCase
import com.wanbaohe.xiangqi.application.usecase.ImportFailureCause
import com.wanbaohe.xiangqi.application.usecase.ImportGameUseCase
import com.wanbaohe.xiangqi.application.usecase.ImportResult
import com.wanbaohe.xiangqi.application.usecase.ManageGameUseCase
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.interfaces.singleton.AppContext
import com.wanbaohe.xiangqi.R
import com.wanbaohe.xiangqi.data.XiangqiGameSummary
import com.wanbaohe.xiangqi.domain.model.Side
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.launch

class XiangqiLibraryComponent @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigate: (Screen) -> Unit,
    private val createGame: CreateGameUseCase,
    private val gameQuery: GameQueryUseCase,
    private val importGame: ImportGameUseCase,
    private val deleteGameUseCase: DeleteGameUseCase,
    private val manageGame: ManageGameUseCase,
    dispatchersHolder: DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext) {

    var games by mutableStateOf<List<XiangqiGameSummary>>(emptyList())
        private set

    init {
        componentScope.launch {
            gameQuery.observeAll().collect { list ->
                games = list
            }
        }
    }

    fun createLocalGame(title: String) {
        componentScope.launch {
            val gameId = createGame.createLocal(title)
            navigateToGame(gameId)
        }
    }

    fun createAiGame(title: String, aiSide: Side) {
        componentScope.launch {
            val gameId = createGame.createHumanVsAi(title, aiSide)
            navigateToGame(gameId)
        }
    }

    fun createAiVsAiGame(title: String) {
        componentScope.launch {
            val gameId = createGame.createAiVsAi(title)
            navigateToGame(gameId)
        }
    }

    fun createOnlineGame(
        roomId: String,
        mySide: Side,
        opponentName: String = AppContext.getString(R.string.xiangqi_player_remote),
        opponentAvatarUrl: String = "",
        initialFen: String,
    ) {
        componentScope.launch {
            val sideName = if (mySide == Side.RED) AppContext.getString(R.string.xiangqi_side_red) else AppContext.getString(R.string.xiangqi_side_black)
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
        componentScope.launch {
            when (val result = importGame.importFen(title, fen, defaultTitle)) {
                is ImportResult.Failure -> ActionUtils.showToast(
                    when (result.cause) {
                        ImportFailureCause.INVALID_FEN -> R.string.xiangqi_invalid_fen
                        ImportFailureCause.INVALID_JSON -> R.string.xiangqi_invalid_json
                    },
                )
                is ImportResult.Success -> {
                    notifyImportOutcome(result)
                    navigateToGame(result.gameId)
                }
            }
        }
    }

    /** 导入 JSON 棋谱（含着法）。[defaultTitle] 同 [importFen]。 */
    fun importJson(title: String, json: String, defaultTitle: String) {
        componentScope.launch {
            when (val result = importGame.importJson(title, json, defaultTitle)) {
                is ImportResult.Failure -> ActionUtils.showToast(
                    when (result.cause) {
                        ImportFailureCause.INVALID_FEN -> R.string.xiangqi_invalid_fen
                        ImportFailureCause.INVALID_JSON -> R.string.xiangqi_invalid_json
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
                    R.plurals.xiangqi_import_partial,
                    result.importedPlies,
                    result.importedPlies,
                    result.skippedPlies.size,
                ),
            )
            result.importedPlies > 0 -> ActionUtils.showToast(R.string.xiangqi_import_success)
            // FEN 导入只有局面、没有着法，不必提示"导入成功"
            else -> Unit
        }
    }

    fun openGame(gameId: String) {
        navigateToGame(gameId)
    }

    fun openAnalysis(gameId: String) {
        onNavigate(Screen.XiangqiRouter(Screen.XiangqiRouter.Type.Analysis(gameId)))
    }

    fun deleteGame(gameId: String) {
        componentScope.launch { deleteGameUseCase.delete(gameId) }
    }

    fun renameGame(gameId: String, newTitle: String) {
        componentScope.launch { manageGame.rename(gameId, newTitle) }
    }

    private fun navigateToGame(gameId: String) {
        onNavigate(Screen.XiangqiRouter(Screen.XiangqiRouter.Type.Game(gameId)))
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
        ): XiangqiLibraryComponent
    }
}
