package com.wanbaohe.xiangqi.component

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.arkivanov.decompose.ComponentContext
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.wanbaohe.xiangqi.application.usecase.CreateGameUseCase
import com.shifenmiao.interfaces.singleton.AppContext
import com.wanbaohe.xiangqi.R
import com.wanbaohe.xiangqi.application.usecase.DeleteGameUseCase
import com.wanbaohe.xiangqi.application.usecase.GameQueryUseCase
import com.wanbaohe.xiangqi.application.usecase.ImportGameUseCase
import com.wanbaohe.xiangqi.application.usecase.ManageGameUseCase
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
                games = list.map { it.toLegacy() }
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

    fun importFen(title: String, fen: String) {
        componentScope.launch {
            val gameId = importGame.importFen(title, fen)
            navigateToGame(gameId)
        }
    }

    fun importJson(title: String, json: String) {
        componentScope.launch {
            val gameId = importGame.importJson(title, json)
            navigateToGame(gameId)
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

    private fun com.wanbaohe.xiangqi.application.dto.GameSummary.toLegacy() = XiangqiGameSummary(
        id = id,
        title = title,
        mode = mode,
        redPlayerType = redPlayerType,
        blackPlayerType = blackPlayerType,
        status = status,
        resultText = resultText,
        updatedAt = updatedAt,
    )

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
        ): XiangqiLibraryComponent
    }
}
