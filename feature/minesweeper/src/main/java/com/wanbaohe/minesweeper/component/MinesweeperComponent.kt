package com.wanbaohe.minesweeper.component

import android.graphics.Bitmap
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.shifenmiao.base.audio.NetworkAudioPlayer
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.image.ImageShareProvider
import com.t8rin.imagetoolbox.core.domain.image.model.ImageFormat
import com.t8rin.imagetoolbox.core.domain.image.model.ImageInfo
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.wanbaohe.core.ui.review.ReviewPromptHost
import com.wanbaohe.core.ui.review.ReviewPromptTrigger
import com.wanbaohe.minesweeper.logic.BOARD_COLS
import com.wanbaohe.minesweeper.logic.BOARD_ROWS_DEFAULT
import com.wanbaohe.minesweeper.logic.Cell
import com.wanbaohe.minesweeper.logic.GameState
import com.wanbaohe.minesweeper.logic.MINE_DENSITY
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

class MinesweeperComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    dispatchersHolder: DispatchersHolder,
    private val audioPlayer: NetworkAudioPlayer,
    private val shareProvider: ImageShareProvider<Bitmap>,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _uiState = MutableStateFlow(MinesweeperUiState())
    val uiState = _uiState.asStateFlow()

    private var timerJob: Job? = null

    /** 当前局的行数。由界面按屏幕比例算好后送来, 见 [applyBoardRows]。 */
    private var rowCount = BOARD_ROWS_DEFAULT

    companion object {
        /**
         * 音效托管在 R2(bucket onebox-images 的 audio/minesweeper/ 路径),
         * 国内海外同地址。合成脚本与源文件见 onebox-doc/audio/minesweeper/。
         */
        private const val SOUND_BASE = "https://images.oneboxable.com/audio/minesweeper"
        private const val SOUND_BGM = "$SOUND_BASE/bgm.ogg"
        private const val SOUND_REVEAL = "$SOUND_BASE/reveal.ogg"
        private const val SOUND_CASCADE = "$SOUND_BASE/cascade.ogg"
        private const val SOUND_FLAG = "$SOUND_BASE/flag.ogg"
        private const val SOUND_UNFLAG = "$SOUND_BASE/unflag.ogg"
        private const val SOUND_EXPLODE = "$SOUND_BASE/explode.ogg"
        private const val SOUND_WIN = "$SOUND_BASE/win.ogg"

        private val ALL_SOUNDS = listOf(
            SOUND_BGM, SOUND_REVEAL, SOUND_CASCADE, SOUND_FLAG,
            SOUND_UNFLAG, SOUND_EXPLODE, SOUND_WIN,
        )

        /** 一次点开超过这么多格就换成"连开一片"的音效 */
        private const val CASCADE_THRESHOLD = 3
    }

    init {
        resetGame()
        // 后台预热音效(下载到本地缓存), 首次触发时基本已就绪, 不会有网络延迟
        componentScope.launch {
            ALL_SOUNDS.forEach { url -> audioPlayer.warmUp(url) }
        }
        componentContext.lifecycle.doOnDestroy {
            timerJob?.cancel()
            audioPlayer.stopBackground()
            audioPlayer.stopEffect()
        }
    }

    /**
     * 界面把"当前屏幕能塞几行"算好后传进来。
     * 只在还没开局([GameState.INITIAL])时立刻重开 —— 玩到一半转屏不应该把进度清掉,
     * 那一局维持原样, 下一局才会用上新行数。
     */
    fun applyBoardRows(rows: Int) {
        if (rows <= 0 || rows == rowCount) return
        rowCount = rows
        if (_uiState.value.gameState == GameState.INITIAL) {
            resetGame()
        }
    }

    fun resetGame() {
        timerJob?.cancel()
        timerJob = null
        val board = List(rowCount) { r ->
            List(BOARD_COLS) { c ->
                Cell(row = r, col = c)
            }
        }
        _uiState.update {
            it.copy(
                board = board,
                gameState = GameState.INITIAL,
                timer = 0,
                minesLeft = totalMines(rowCount)
            )
        }
        // 开新局时 BGM 重来一轮(同 URL 循环播放期间不会重启, 先停再起)
        if (_uiState.value.soundEnabled) {
            audioPlayer.stopBackground()
            playSound(SOUND_BGM, isBackground = true)
        }
    }

    /** 切换"挖雷 / 插旗"模式。插旗模式下单击即插旗, 不必再考验长按手感。 */
    fun toggleFlagMode() {
        _uiState.update { it.copy(flagMode = !it.flagMode) }
    }

    /** 总音量开关: 关掉时同时停掉 BGM。 */
    fun toggleSound() {
        val enabled = !_uiState.value.soundEnabled
        _uiState.update { it.copy(soundEnabled = enabled) }
        if (enabled) {
            playSound(SOUND_BGM, isBackground = true)
        } else {
            audioPlayer.stopBackground()
            audioPlayer.stopEffect()
        }
    }

    /** 把棋盘截图发出去(系统分享面板)。棋盘以外的内容不进图。 */
    fun shareBitmap(bitmap: Bitmap, onComplete: () -> Unit = {}) {
        componentScope.launch {
            runCatching {
                shareProvider.shareImage(
                    imageInfo = ImageInfo(
                        width = bitmap.width,
                        height = bitmap.height,
                        imageFormat = ImageFormat.Png.Lossless
                    ),
                    image = bitmap,
                    onComplete = onComplete
                )
            }
        }
    }

    /** 该行数对应的雷数。密度固定, 所以行数随机型变化时难度不跟着漂。 */
    private fun totalMines(rows: Int): Int =
        (rows * BOARD_COLS * MINE_DENSITY).toInt().coerceAtLeast(1)

    private fun placeMinesAndCalculateNeighbors(firstClickR: Int, firstClickC: Int) {
        val rows = _uiState.value.board.size
        val cols = BOARD_COLS
        val mines = totalMines(rows)

        val board = _uiState.value.board.map { it.toMutableList() }.toMutableList()
        var minesPlaced = 0

        // 首点及其周围一圈留空, 保证第一步永远点得开
        while (minesPlaced < mines) {
            val r = Random.nextInt(rows)
            val c = Random.nextInt(cols)
            if (Math.abs(r - firstClickR) <= 1 && Math.abs(c - firstClickC) <= 1) continue
            if (!board[r][c].isMine) {
                board[r][c] = board[r][c].copy(isMine = true)
                minesPlaced++
            }
        }

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (board[r][c].isMine) continue
                var count = 0
                for (dr in -1..1) {
                    for (dc in -1..1) {
                        val nr = r + dr
                        val nc = c + dc
                        if (nr in 0 until rows && nc in 0 until cols && board[nr][nc].isMine) {
                            count++
                        }
                    }
                }
                board[r][c] = board[r][c].copy(neighborMines = count)
            }
        }

        _uiState.update { it.copy(board = board) }
    }

    /**
     * 单击一格。
     *
     * 交互约定(见 MinesweeperScreen 的说明):
     * - 未翻开的格子: 挖雷模式 = 翻开, 插旗模式 = 插旗/取消旗
     * - 已翻开的数字: 周围旗数够了就"和弦"展开一圈
     */
    fun onCellClicked(r: Int, c: Int) {
        val state = _uiState.value
        if (state.gameState == GameState.WON || state.gameState == GameState.LOST) return

        val board = state.board.map { it.toMutableList() }.toMutableList()
        val cell = board[r][c]

        if (cell.isRevealed) {
            if (cell.neighborMines > 0 && countNeighborFlags(board, r, c) == cell.neighborMines) {
                chordReveal(board, r, c)
            }
            return
        }

        // 插旗模式下单击就是插旗, 不再考验长按的手感
        if (state.flagMode) {
            toggleFlag(board, r, c)
            return
        }
        // 挖雷模式点旗子不响应, 免得手一抖把标记挖了
        if (cell.isFlagged) return

        revealFrom(board, r, c)
    }

    /** 长按一格: 无论当前什么模式, 一律插旗/取消旗。 */
    fun onCellLongClicked(r: Int, c: Int) {
        val state = _uiState.value
        if (state.gameState == GameState.WON || state.gameState == GameState.LOST) return

        val board = state.board.map { it.toMutableList() }.toMutableList()
        if (board[r][c].isRevealed) return
        toggleFlag(board, r, c)
    }

    /** 从 (r,c) 开始翻开。首点才布雷, 之后计算胜负并出声。 */
    private fun revealFrom(board: MutableList<MutableList<Cell>>, r: Int, c: Int) {
        if (_uiState.value.gameState == GameState.INITIAL) {
            placeMinesAndCalculateNeighbors(r, c)
            _uiState.update { it.copy(gameState = GameState.PLAYING) }
            startTimer()
            // 布雷后 board 内容变了, 重新取一份再继续
            val fresh = _uiState.value.board.map { it.toMutableList() }.toMutableList()
            board.clear()
            board.addAll(fresh)
        }

        if (board[r][c].isMine) {
            revealAllMines(board)
            _uiState.update { it.copy(board = board, gameState = GameState.LOST) }
            timerJob?.cancel()
            audioPlayer.stopBackground()
            playSound(SOUND_EXPLODE)
            return
        }

        val revealed = revealEmptyCells(board, r, c)
        if (revealed > CASCADE_THRESHOLD) playSound(SOUND_CASCADE) else playSound(SOUND_REVEAL)

        if (checkWin(board)) {
            finishAsWon(board)
            return
        }
        _uiState.update { it.copy(board = board, gameState = GameState.PLAYING) }
    }

    /**
     * 和弦展开: 数字周围的旗数已够, 把剩余未标记的邻格一次全翻开。
     * 旗插错了就会踩雷, 这是标准扫雷规则, 不是 bug。
     */
    private fun chordReveal(board: MutableList<MutableList<Cell>>, r: Int, c: Int) {
        var revealed = 0
        var hitMine = false
        for (dr in -1..1) {
            for (dc in -1..1) {
                if (dr == 0 && dc == 0) continue
                val nr = r + dr
                val nc = c + dc
                if (nr !in board.indices || nc !in board[0].indices) continue
                val neighbor = board[nr][nc]
                if (neighbor.isRevealed || neighbor.isFlagged) continue
                if (neighbor.isMine) {
                    hitMine = true
                    continue
                }
                revealed += revealEmptyCells(board, nr, nc)
            }
        }

        if (hitMine) {
            revealAllMines(board)
            _uiState.update { it.copy(board = board, gameState = GameState.LOST) }
            timerJob?.cancel()
            audioPlayer.stopBackground()
            playSound(SOUND_EXPLODE)
            return
        }

        if (revealed > 0) {
            if (revealed > CASCADE_THRESHOLD) playSound(SOUND_CASCADE) else playSound(SOUND_REVEAL)
        }
        if (checkWin(board)) {
            finishAsWon(board)
            return
        }
        _uiState.update { it.copy(board = board) }
    }

    /** 清盘:停表停背景音、翻到胜利态、出声,并上报一次「通关」成功时刻(应用内评分弹层用) */
    private fun finishAsWon(board: MutableList<MutableList<Cell>>) {
        timerJob?.cancel()
        audioPlayer.stopBackground()
        _uiState.update { it.copy(board = board, gameState = GameState.WON) }
        playSound(SOUND_WIN)
        ReviewPromptHost.notifySuccess(ReviewPromptTrigger.GAME_WIN)
    }

    private fun toggleFlag(board: MutableList<MutableList<Cell>>, r: Int, c: Int) {
        val cell = board[r][c]
        val nextFlagged = !cell.isFlagged
        board[r][c] = cell.copy(isFlagged = nextFlagged)
        val minesLeft = _uiState.value.minesLeft + if (nextFlagged) -1 else 1
        _uiState.update { it.copy(board = board, minesLeft = minesLeft) }
        playSound(if (nextFlagged) SOUND_FLAG else SOUND_UNFLAG)
    }

    private fun countNeighborFlags(board: List<List<Cell>>, r: Int, c: Int): Int {
        var count = 0
        for (dr in -1..1) {
            for (dc in -1..1) {
                if (dr == 0 && dc == 0) continue
                val nr = r + dr
                val nc = c + dc
                if (nr in board.indices && nc in board[0].indices && board[nr][nc].isFlagged) {
                    count++
                }
            }
        }
        return count
    }

    /** 翻开 (r,c), 空白则递归扩散。返回本次新翻开的格子数。 */
    private fun revealEmptyCells(board: MutableList<MutableList<Cell>>, r: Int, c: Int): Int {
        if (r !in 0 until board.size || c !in 0 until board[0].size) return 0
        if (board[r][c].isRevealed || board[r][c].isFlagged || board[r][c].isMine) return 0

        board[r][c] = board[r][c].copy(isRevealed = true)
        var count = 1

        if (board[r][c].neighborMines == 0) {
            for (dr in -1..1) {
                for (dc in -1..1) {
                    if (dr != 0 || dc != 0) count += revealEmptyCells(board, r + dr, c + dc)
                }
            }
        }
        return count
    }

    private fun revealAllMines(board: MutableList<MutableList<Cell>>) {
        for (r in board.indices) {
            for (c in board[r].indices) {
                if (board[r][c].isMine) {
                    board[r][c] = board[r][c].copy(isRevealed = true)
                }
            }
        }
    }

    private fun checkWin(board: List<List<Cell>>): Boolean {
        for (r in board.indices) {
            for (c in board[r].indices) {
                val cell = board[r][c]
                if (!cell.isMine && !cell.isRevealed) return false
            }
        }
        return true
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = componentScope.launch {
            while (true) {
                delay(1000)
                _uiState.update { it.copy(timer = it.timer + 1) }
            }
        }
    }

    /**
     * 播放音效。短音效用 [NetworkAudioPlayer.playEffect](同 URL 已缓存, 秒开);
     * BGM 用 [NetworkAudioPlayer.playBackground](循环, 同 URL 播放中不重启)。
     * 播放失败静默吞掉, 网络问题不该影响游戏本身。
     */
    private fun playSound(url: String, isBackground: Boolean = false) {
        if (!_uiState.value.soundEnabled) return
        componentScope.launch {
            runCatching {
                if (isBackground) audioPlayer.playBackground(url) else audioPlayer.playEffect(url)
            }
        }
    }

    @AssistedFactory
    interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit
        ): MinesweeperComponent
    }
}
