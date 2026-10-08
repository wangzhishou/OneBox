package com.wanbaohe.sudoku.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.essenty.lifecycle.doOnStart
import com.arkivanov.essenty.lifecycle.doOnStop
import com.shifenmiao.base.audio.NetworkAudioPlayer
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.wanbaohe.core.ui.review.ReviewPromptHost
import com.wanbaohe.core.ui.review.ReviewPromptTrigger
import com.wanbaohe.sudoku.data.SudokuSettings
import com.wanbaohe.sudoku.data.SudokuSnapshot
import com.wanbaohe.sudoku.data.SudokuStorage
import com.wanbaohe.sudoku.data.SudokuTimerMode
import com.wanbaohe.sudoku.logic.SudokuDifficulty
import com.wanbaohe.sudoku.logic.SudokuEngine
import com.wanbaohe.sudoku.logic.SudokuRecord
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 数独模块的单 Component: tab 横切 + 对局编排 + 设置 + 战绩聚合全在这里。
 *
 * 引擎(SudokuEngine)与统计聚合(SudokuStats)是纯函数, MMKV 读写集中在
 * [SudokuStorage], 这里只做编排: 状态流、计时协程、撤销栈、音效触发、生命周期落盘。
 */
class SudokuComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    dispatchersHolder: DispatchersHolder,
    private val audioPlayer: NetworkAudioPlayer,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _uiState = MutableStateFlow(SudokuUiState())
    val uiState = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var wasTickingBeforeStop = false

    /** 撤销栈(内存): 记录每次落子/橡皮/提示前的格子旧值。 */
    private val undoStack = ArrayDeque<UndoMove>()

    private data class UndoMove(val index: Int, val previousValue: Int)

    companion object {
        /**
         * 音效托管在 R2(bucket onebox-images 的 audio/sudoku/ 路径), 国内海外同地址。
         * 文件没传上去时播放器静默失败, 不影响游戏。
         */
        private const val SOUND_BASE = "https://images.oneboxable.com/audio/sudoku"
        private const val SOUND_PLACE = "$SOUND_BASE/place.ogg"
        private const val SOUND_ERASE = "$SOUND_BASE/erase.ogg"
        private const val SOUND_ERROR = "$SOUND_BASE/error.ogg"
        private const val SOUND_HINT = "$SOUND_BASE/hint.ogg"
        private const val SOUND_UNDO = "$SOUND_BASE/undo.ogg"
        private const val SOUND_WIN = "$SOUND_BASE/win.ogg"
        private const val SOUND_BGM = "$SOUND_BASE/bgm.ogg"

        private val ALL_SOUNDS = listOf(
            SOUND_PLACE, SOUND_ERASE, SOUND_ERROR,
            SOUND_HINT, SOUND_UNDO, SOUND_WIN, SOUND_BGM,
        )
    }

    init {
        val settings = SudokuStorage.loadSettings()
        val snapshot = SudokuStorage.loadSnapshot()
        _uiState.update {
            it.copy(
                settings = settings,
                records = SudokuStorage.loadRecords(),
                hasSavedGame = snapshot != null,
                savedFilledCount = snapshot?.let(::savedUserFilledCount) ?: 0
            )
        }
        // 后台预热音效(下载到本地缓存), 首次触发基本无网络延迟
        componentScope.launch {
            ALL_SOUNDS.forEach { url -> audioPlayer.warmUp(url) }
        }
        if (settings.bgmEnabled) playBackground()

        // 退后台: 停表 + 快照落盘(含已用时); 回前台按退后台前的走表状态恢复
        componentContext.lifecycle.doOnStop {
            wasTickingBeforeStop = timerJob != null
            stopTimer()
            persistSnapshot()
        }
        componentContext.lifecycle.doOnStart {
            if (wasTickingBeforeStop) ensureTimer()
            wasTickingBeforeStop = false
        }
        componentContext.lifecycle.doOnDestroy {
            stopTimer()
            persistSnapshot()
            audioPlayer.stopBackground()
            audioPlayer.stopEffect()
        }
    }

    // ─── tab 与返回 ────────────────────────────────────────────────────────

    fun switchTab(tab: SudokuTab) {
        _uiState.update { it.copy(tab = tab) }
    }

    /** 非游戏 tab 先切回游戏 tab, 已在游戏 tab 才退出模块(对局有快照, 退出不丢)。 */
    fun onTabBack() {
        if (_uiState.value.tab != SudokuTab.GAME) switchTab(SudokuTab.GAME) else onGoBack()
    }

    // ─── 开局 / 继续 / 放弃 ────────────────────────────────────────────────

    /**
     * 开新局。若有未完成旧局(进行中或躺着快照)先记为 abandoned 战绩再开。
     * 出题在 default 线程(回溯 + 挖洞有几十到几百毫秒开销), 不卡主线程。
     */
    fun startNewGame(difficulty: SudokuDifficulty) {
        abandonUnfinishedIfAny()
        componentScope.launch {
            val (puzzle, solution) = withContext(defaultDispatcher) {
                SudokuEngine.generatePuzzle(difficulty)
            }
            undoStack.clear()
            _uiState.update {
                it.copy(
                    phase = SudokuGamePhase.PLAYING,
                    puzzle = puzzle.toList(),
                    solution = solution.toList(),
                    entries = puzzle.toList(),
                    difficulty = difficulty,
                    selectedIndex = null,
                    undoDepth = 0,
                    elapsedSeconds = 0,
                    timerRunning = it.settings.timerMode != SudokuTimerMode.MANUAL,
                    hasSavedGame = false,
                    savedFilledCount = 0,
                    completedBestSeconds = null
                )
            }
            persistSnapshot()
            ensureTimer()
        }
    }

    /** 继续上次没玩完的局(快照已在 init 校验过, 这里再兜一层)。 */
    fun continueSavedGame() {
        val snapshot = SudokuStorage.loadSnapshot()
        if (snapshot == null) {
            _uiState.update { it.copy(hasSavedGame = false, savedFilledCount = 0) }
            return
        }
        val puzzle = SudokuEngine.decodeBoard(snapshot.puzzle) ?: return
        val solution = SudokuEngine.decodeBoard(snapshot.solution) ?: return
        val entries = SudokuEngine.decodeBoard(snapshot.entries) ?: return
        undoStack.clear()
        _uiState.update {
            it.copy(
                phase = SudokuGamePhase.PLAYING,
                puzzle = puzzle.toList(),
                solution = solution.toList(),
                entries = entries.toList(),
                difficulty = snapshot.difficulty,
                selectedIndex = null,
                undoDepth = 0,
                elapsedSeconds = snapshot.elapsedSeconds,
                timerRunning = it.settings.timerMode != SudokuTimerMode.MANUAL,
                completedBestSeconds = null
            )
        }
        ensureTimer()
    }

    /** 对局中点"换个难度": 记放弃战绩, 回难度选择页。 */
    fun onChangeDifficulty() {
        abandonUnfinishedIfAny()
        _uiState.update { it.copy(phase = SudokuGamePhase.HOME) }
    }

    /** 结算页"再来一局": 直接按刚完成的难度开新局。 */
    fun onPlayAgain() {
        startNewGame(_uiState.value.difficulty)
    }

    /** 设置页"难度选择"行: 有进行中的局时点击 = 切回游戏 tab 继续对局。 */
    fun openOngoingGame() {
        when {
            _uiState.value.phase == SudokuGamePhase.PLAYING -> switchTab(SudokuTab.GAME)
            SudokuStorage.loadSnapshot() != null -> {
                continueSavedGame()
                switchTab(SudokuTab.GAME)
            }
        }
    }

    /** 设置页"难度选择"行: 无对局时选定难度 = 存为默认难度并立即开新局。 */
    fun startNewGameFromSettings(difficulty: SudokuDifficulty) {
        updateDefaultDifficulty(difficulty)
        switchTab(SudokuTab.GAME)
        startNewGame(difficulty)
    }

    private fun abandonUnfinishedIfAny() {
        val state = _uiState.value
        val snapshot = SudokuStorage.loadSnapshot()
        when {
            state.phase == SudokuGamePhase.PLAYING ->
                appendRecord(state.difficulty, state.elapsedSeconds, completed = false)

            snapshot != null ->
                appendRecord(snapshot.difficulty, snapshot.elapsedSeconds, completed = false)

            else -> return
        }
        SudokuStorage.clearSnapshot()
        stopTimer()
        _uiState.update { it.copy(hasSavedGame = false, savedFilledCount = 0) }
    }

    // ─── 对局操作 ──────────────────────────────────────────────────────────

    fun onCellSelected(index: Int) {
        if (_uiState.value.phase != SudokuGamePhase.PLAYING) return
        _uiState.update { it.copy(selectedIndex = index) }
    }

    fun onDigitInput(digit: Int) {
        val state = _uiState.value
        if (state.phase != SudokuGamePhase.PLAYING) return
        val index = state.selectedIndex ?: return
        if (state.puzzle.getOrNull(index) != 0) return
        if (state.entries.getOrNull(index) == digit) return

        pushUndo(index, state.entries[index])
        setEntry(index, digit)
        // 自动检查开着且填错 = 错误音; 其余落子一个音(关掉检查时对错不在盘上标红)
        playEffect(
            if (state.settings.autoCheck && digit != state.solution[index]) SOUND_ERROR else SOUND_PLACE
        )
        persistSnapshot()
        checkCompleted()
    }

    fun onErase() {
        val state = _uiState.value
        if (state.phase != SudokuGamePhase.PLAYING) return
        val index = state.selectedIndex ?: return
        if (state.puzzle.getOrNull(index) != 0) return
        if (state.entries.getOrNull(index) == 0) return

        pushUndo(index, state.entries[index])
        setEntry(index, 0)
        playEffect(SOUND_ERASE)
        persistSnapshot()
    }

    /** 提示: 选中格可填填选中格, 否则随机填一个空格。设置里关掉提示则不调进来(UI 同步置灰)。 */
    fun onHint() {
        val state = _uiState.value
        if (!state.settings.hintEnabled || state.phase != SudokuGamePhase.PLAYING) return
        val target = SudokuEngine.findHintCell(
            puzzle = state.puzzle.toIntArray(),
            entries = state.entries.toIntArray(),
            solution = state.solution.toIntArray(),
            selectedIndex = state.selectedIndex
        )
        if (target < 0) return

        pushUndo(target, state.entries[target])
        setEntry(target, state.solution[target])
        _uiState.update { it.copy(selectedIndex = target) }
        playEffect(SOUND_HINT)
        persistSnapshot()
        checkCompleted()
    }

    fun onUndo() {
        if (_uiState.value.phase != SudokuGamePhase.PLAYING) return
        val move = undoStack.removeLastOrNull() ?: return
        setEntry(move.index, move.previousValue)
        _uiState.update {
            it.copy(selectedIndex = move.index, undoDepth = undoStack.size)
        }
        playEffect(SOUND_UNDO)
        persistSnapshot()
    }

    /** 重新开始: 清空用户填写、保留题目, 计时与撤销栈一并归零。 */
    fun onRestart() {
        val state = _uiState.value
        if (state.phase != SudokuGamePhase.PLAYING) return
        undoStack.clear()
        _uiState.update {
            it.copy(
                entries = it.puzzle,
                selectedIndex = null,
                undoDepth = 0,
                elapsedSeconds = 0,
                timerRunning = it.settings.timerMode != SudokuTimerMode.MANUAL
            )
        }
        persistSnapshot()
        ensureTimer()
    }

    // ─── 设置(即时生效并落盘) ─────────────────────────────────────────────

    fun updateDefaultDifficulty(difficulty: SudokuDifficulty) =
        saveSettings(_uiState.value.settings.copy(defaultDifficulty = difficulty))

    fun updateAutoCheck(enabled: Boolean) =
        saveSettings(_uiState.value.settings.copy(autoCheck = enabled))

    fun updateHintEnabled(enabled: Boolean) =
        saveSettings(_uiState.value.settings.copy(hintEnabled = enabled))

    fun updateTimerMode(mode: SudokuTimerMode) {
        saveSettings(_uiState.value.settings.copy(timerMode = mode))
        ensureTimer()
    }

    fun updateSoundEnabled(enabled: Boolean) =
        saveSettings(_uiState.value.settings.copy(soundEnabled = enabled))

    /** BGM 开关立即播/停。 */
    fun updateBgmEnabled(enabled: Boolean) {
        saveSettings(_uiState.value.settings.copy(bgmEnabled = enabled))
        if (enabled) playBackground() else {
            componentScope.launch { runCatching { audioPlayer.stopBackground() } }
        }
    }

    /** 手动计时模式: 点用时卡开始/暂停。 */
    fun onTimeCardClick() {
        val state = _uiState.value
        if (state.settings.timerMode != SudokuTimerMode.MANUAL) return
        if (state.phase != SudokuGamePhase.PLAYING) return
        _uiState.update { it.copy(timerRunning = !it.timerRunning) }
        ensureTimer()
    }

    // ─── 内部 ──────────────────────────────────────────────────────────────

    private fun saveSettings(settings: SudokuSettings) {
        _uiState.update { it.copy(settings = settings) }
        SudokuStorage.saveSettings(settings)
    }

    private fun pushUndo(index: Int, previousValue: Int) {
        undoStack.addLast(UndoMove(index, previousValue))
        _uiState.update { it.copy(undoDepth = undoStack.size) }
    }

    private fun setEntry(index: Int, value: Int) {
        _uiState.update {
            it.copy(entries = it.entries.toMutableList().apply { this[index] = value })
        }
    }

    private fun checkCompleted() {
        val state = _uiState.value
        if (!SudokuEngine.isBoardComplete(state.entries.toIntArray(), state.solution.toIntArray())) return

        stopTimer()
        appendRecord(state.difficulty, state.elapsedSeconds, completed = true)
        SudokuStorage.updateBestSeconds(state.difficulty, state.elapsedSeconds)
        SudokuStorage.clearSnapshot()
        _uiState.update {
            it.copy(
                phase = SudokuGamePhase.COMPLETED,
                timerRunning = false,
                hasSavedGame = false,
                savedFilledCount = 0,
                completedBestSeconds = SudokuStorage.loadBestSeconds(state.difficulty)
            )
        }
        playEffect(SOUND_WIN)
        // 完成一局 = 通关时刻,上报给应用内评分弹层(google 渠道按累计次数/冷却决定是否弹)
        ReviewPromptHost.notifySuccess(ReviewPromptTrigger.GAME_WIN)
    }

    private fun appendRecord(difficulty: SudokuDifficulty, timeSec: Int, completed: Boolean) {
        val record = SudokuRecord(
            timestampMillis = System.currentTimeMillis(),
            difficulty = difficulty,
            timeSec = timeSec,
            completed = completed
        )
        SudokuStorage.appendRecord(record)
        _uiState.update { it.copy(records = it.records.plus(record).takeLast(1000)) }
    }

    /** 只在 PLAYING 且有完整盘面时落盘; 完成后快照已清, 不会再写。 */
    private fun persistSnapshot() {
        val state = _uiState.value
        if (state.phase != SudokuGamePhase.PLAYING) return
        if (state.puzzle.size != SudokuEngine.BOARD_CELLS ||
            state.solution.size != SudokuEngine.BOARD_CELLS ||
            state.entries.size != SudokuEngine.BOARD_CELLS
        ) return
        SudokuStorage.saveSnapshot(
            SudokuSnapshot(
                puzzle = SudokuEngine.encodeBoard(state.puzzle.toIntArray()),
                solution = SudokuEngine.encodeBoard(state.solution.toIntArray()),
                entries = SudokuEngine.encodeBoard(state.entries.toIntArray()),
                difficulty = state.difficulty,
                elapsedSeconds = state.elapsedSeconds
            )
        )
        _uiState.update {
            it.copy(
                hasSavedGame = true,
                savedFilledCount = SudokuEngine.countUserFilled(
                    state.puzzle.toIntArray(), state.entries.toIntArray()
                )
            )
        }
    }

    private fun savedUserFilledCount(snapshot: SudokuSnapshot): Int {
        val puzzle = SudokuEngine.decodeBoard(snapshot.puzzle) ?: return 0
        val entries = SudokuEngine.decodeBoard(snapshot.entries) ?: return 0
        return SudokuEngine.countUserFilled(puzzle, entries)
    }

    /**
     * 走表条件: 对局中, 且(计时模式非手动, 或手动模式下用户已点开始)。
     * 关闭模式只是不显示用时卡, 表照走 —— 战绩里的用时仍然是真实时长。
     */
    private fun isTickingNow(state: SudokuUiState): Boolean {
        if (state.phase != SudokuGamePhase.PLAYING) return false
        return when (state.settings.timerMode) {
            SudokuTimerMode.MANUAL -> state.timerRunning
            SudokuTimerMode.OFF, SudokuTimerMode.AUTO -> true
        }
    }

    private fun ensureTimer() {
        if (isTickingNow(_uiState.value)) {
            if (timerJob == null) {
                timerJob = componentScope.launch {
                    while (true) {
                        delay(1000)
                        _uiState.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
                    }
                }
            }
        } else {
            stopTimer()
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun playEffect(url: String) {
        if (!_uiState.value.settings.soundEnabled) return
        componentScope.launch {
            runCatching { audioPlayer.playEffect(url) }
        }
    }

    private fun playBackground() {
        componentScope.launch {
            runCatching { audioPlayer.playBackground(SOUND_BGM) }
        }
    }

    @AssistedFactory
    interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit
        ): SudokuComponent
    }
}
