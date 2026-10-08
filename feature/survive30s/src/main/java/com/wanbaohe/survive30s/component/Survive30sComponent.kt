package com.wanbaohe.survive30s.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.shifenmiao.base.audio.NetworkAudioPlayer
import com.tencent.mmkv.MMKV
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.wanbaohe.core.ui.review.ReviewPromptHost
import com.wanbaohe.core.ui.review.ReviewPromptTrigger
import com.shifenmiao.common.utils.BaseUtils
import com.shifenmiao.storage.RemoteConfigStorage
import com.wanbaohe.survive30s.engine.Player
import com.wanbaohe.survive30s.engine.Survive30sEngine
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.exp

/**
 * 躲避30秒游戏业务逻辑组件
 *
 * 职责：
 * 1. 维护 [Survive30sUiState] 并通过 [uiState] 暴露给 Compose
 * 2. 驱动游戏主循环（60fps 定时器）
 * 3. 处理触摸拖动 → 更新玩家位置
 * 4. 碰撞检测 & 游戏结束判定
 * 5. 持久化最佳记录到 MMKV
 */
class Survive30sComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    dispatchersHolder: DispatchersHolder,
    private val audioPlayer: NetworkAudioPlayer,
) : BaseComponent(dispatchersHolder, componentContext) {

    companion object {
        private const val FRAME_INTERVAL_MS = 16L
        private const val PLAYER_FOLLOW_RESPONSE = 60f
        private const val INVINCIBLE_AFTER_SHIELD_SEC = 1.1f
        private const val NEAR_MISS_PER_SHIELD = 3
        private const val MAX_SHIELDS = 2

        /**
         * 单帧最大可信耗时（秒）。超过视为进程被冻结/切后台，
         * 丢弃该帧增量——否则一次 30 秒的大帧会直接判胜。
         */
        private const val MAX_FRAME_DELTA_SEC = 0.25f

        /**
         * 游戏音效托管在 R2(bucket onebox-images 的 audio/survive30s/ 路径),
         * 国内海外同地址。合成脚本与源文件见 onebox-doc/audio/survive30s/。
         */
        private const val SOUND_BASE = "https://images.oneboxable.com/audio/survive30s"
        private const val SOUND_BGM = "$SOUND_BASE/bgm.ogg"
        private const val SOUND_GAME_OVER = "$SOUND_BASE/game_over.ogg"
        private const val SOUND_SHIELD_BLOCK = "$SOUND_BASE/shield_block.ogg"
        private const val SOUND_NEAR_MISS = "$SOUND_BASE/near_miss.ogg"
        private const val SOUND_SHIELD_GAIN = "$SOUND_BASE/shield_gain.ogg"
        private const val SOUND_PHASE_CHANGE = "$SOUND_BASE/phase_change.ogg"
        private const val SOUND_WIN = "$SOUND_BASE/win.ogg"
        private val ALL_SOUNDS = listOf(
            SOUND_BGM, SOUND_GAME_OVER, SOUND_SHIELD_BLOCK, SOUND_NEAR_MISS,
            SOUND_SHIELD_GAIN, SOUND_PHASE_CHANGE, SOUND_WIN,
        )

        /** 擦身音效最小间隔(纳秒) = 150ms */
        private const val NEAR_MISS_SOUND_MIN_GAP_NS = 150_000_000L
    }

    // ─── MMKV 持久化（必须在 _uiState 之前声明，因为 _uiState 初始化时需要访问 mkv） ──

    private val mkv by lazy { MMKV.mmkvWithID("survive30s") }

    private val _uiState = MutableStateFlow(Survive30sUiState(bestTime = loadBestTime()))
    val uiState = _uiState.asStateFlow()

    /** 游戏主循环 Job */
    private var gameLoopJob: Job? = null

    /**
     * 暂停请求标记（与 [Survive30sUiState.gameState] 解耦）。
     *
     * 退后台时 lifecycle observer 跟主循环在同一线程上，会有竞态：
     * 主循环先把 state 翻成 GAME_OVER / WIN，observer 紧接着才跑，
     * 这时 [pauseGame] 看到 state 不是 PLAYING 就直接 return，结果 UI 显示的是结算页而不是 PAUSED 覆盖层。
     *
     * 用一个独立的 atomic flag 在 [pauseGame] / [onGameOver] / [onWin] 入口处优先检查，
     * 把"应该暂停"的状态判断跟"游戏结果"的状态判断解耦——任何时候只要调用过 [pauseGame]，
     * 这一轮就不该被判定为死亡或胜利。
     */
    @Volatile
    private var pauseRequested: Boolean = false

    /** 障碍物生成帧计数器 */
    private var spawnCounter = 0

    /** 上次擦身音效触发时刻(纳秒), 用于节流 */
    private var lastNearMissSoundNs = 0L

    /** 玩家目标位置，由触摸输入直接更新，在主循环中平滑跟随 */
    private var targetPlayerX = Float.NaN
    private var targetPlayerY = Float.NaN

    init {
        // 后台预热音效(下载到本地缓存), 用户点"开始挑战"时基本已就绪, 首次播放无网络延迟
        componentScope.launch {
            ALL_SOUNDS.map { url -> async { audioPlayer.warmUp(url) } }.awaitAll()
        }
        componentContext.lifecycle.doOnDestroy {
            gameLoopJob?.cancel()
            audioPlayer.stopBackground()
            audioPlayer.stopEffect()
        }
    }

    // ─── 公开交互接口 ─────────────────────────────────────────────────────────

    /**
     * 同步画布尺寸（首次布局、屏幕旋转、分屏都会回调）
     *
     * 画布铺满整屏，所以这里同时充当"初始化玩家位置"与"边界变化后把玩家夹回可视区"两个职责。
     */
    fun updateCanvasSize(width: Float, height: Float) {
        if (width <= 0f || height <= 0f) return
        val state = _uiState.value
        if (state.canvasWidth == width && state.canvasHeight == height) return

        val playerRadius = width * Survive30sEngine.PLAYER_RADIUS_RATIO
        val isFirstLayout = state.canvasWidth <= 0f || state.player.radius <= 0f
        val x = if (isFirstLayout) width / 2f else clampX(state.player.x, width, playerRadius)
        val y = if (isFirstLayout) height * 0.85f else clampY(state.player.y, height, playerRadius)
        targetPlayerX = x
        targetPlayerY = y
        _uiState.update {
            it.copy(
                canvasWidth = width,
                canvasHeight = height,
                player = it.player.copy(x = x, y = y, radius = playerRadius),
            )
        }
    }

    /** 开始新游戏 */
    fun startGame() {
        spawnCounter = 0
        val w = _uiState.value.canvasWidth
        val h = _uiState.value.canvasHeight
        val playerRadius = w * Survive30sEngine.PLAYER_RADIUS_RATIO
        val initialX = w / 2f
        val initialY = h * 0.85f
        targetPlayerX = initialX
        targetPlayerY = initialY
        _uiState.update {
            it.copy(
                gameState = GameState.PLAYING,
                elapsedSec = 0f,
                obstacles = emptyList(),
                shieldCount = 0,
                nearMissCount = 0,
                nearMissCharge = 0,
                dangerLevel = 0f,
                invincibleSec = 0f,
                phase = SurvivalPhase.Warmup,
                player = Player(
                    x = initialX,
                    y = initialY,
                    radius = playerRadius
                )
            )
        }
        startGameLoop()
        playSound(SOUND_BGM, isBackground = true)
    }

    /**
     * 暂停游戏（退后台时由 UI 层生命周期回调触发）。
     *
     * 进度（剩余时间 / 障碍物 / 护盾 / 充能）全部保留，主循环立即停止，
     * 避免后台计时继续累积导致"回来就判胜"。
     *
     * 注意：必须先设 [pauseRequested] 再检查 state——避免与主循环在
     * "碰撞检测→onGameOver→state=GAME_OVER→break" 的执行序列里 race，
     * 那样 observer 来的时候 state 已经是 GAME_OVER，pauseGame 早退，UI 看到的是结算页。
     */
    fun pauseGame() {
        pauseRequested = true
        if (_uiState.value.gameState != GameState.PLAYING) return
        gameLoopJob?.cancel()
        gameLoopJob = null
        audioPlayer.stopBackground()
        _uiState.update { it.copy(gameState = GameState.PAUSED) }
    }

    /** 从暂停中继续：清除请求标记，重开主循环，计时基准重置，不会吞掉后台时长 */
    fun resumeGame() {
        pauseRequested = false
        if (_uiState.value.gameState != GameState.PAUSED) return
        _uiState.update { it.copy(gameState = GameState.PLAYING) }
        startGameLoop()
        playSound(SOUND_BGM, isBackground = true)
    }

    /** 拖动更新玩家位置（支持上下左右全方向移动）
     *
     * @param x 目标 X 坐标
     * @param y 目标 Y 坐标，null 表示仅水平移动
     */
    fun movePlayerTo(x: Float, y: Float? = null) {
        val state = _uiState.value
        if (state.gameState != GameState.PLAYING) return
        if (state.canvasWidth <= 0f || state.canvasHeight <= 0f) return
        // 拖动范围 = 整块画布（已铺满全屏），只在边缘处收住，避免小球跑出屏幕
        targetPlayerX = clampX(x, state.canvasWidth, state.player.radius)
        targetPlayerY = clampY(y ?: state.player.y, state.canvasHeight, state.player.radius)
    }

    private fun clampX(x: Float, canvasWidth: Float, radius: Float): Float =
        x.coerceIn(radius, (canvasWidth - radius).coerceAtLeast(radius))

    private fun clampY(y: Float, canvasHeight: Float, radius: Float): Float =
        y.coerceIn(radius, (canvasHeight - radius).coerceAtLeast(radius))

    // ─── 游戏主循环 ────────────────────────────────────────────────────────────

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = componentScope.launch {
            var lastTime = System.nanoTime()
            while (true) {
                // 暂停请求先于 state 检查，避开与 onGameOver 的 race
                if (pauseRequested) break
                delay(FRAME_INTERVAL_MS)
                val now = System.nanoTime()
                val deltaSec = (now - lastTime) / 1_000_000_000f
                lastTime = now

                val state = _uiState.value
                if (state.gameState != GameState.PLAYING) break

                // 异常大帧（进程被冻结 / 极端卡顿）：丢弃增量，防止一次跳变直接判胜
                if (deltaSec > MAX_FRAME_DELTA_SEC) continue

                val newElapsed = state.elapsedSec + deltaSec
                val updatedPlayer = smoothPlayer(state, deltaSec)

                // 检查是否存活30秒
                if (newElapsed >= Survive30sEngine.GAME_DURATION) {
                    onWin(newElapsed)
                    break
                }

                // 更新障碍物位置
                var obstacles = Survive30sEngine.updateObstacles(
                    obstacles = state.obstacles,
                    deltaTime = deltaSec,
                    canvasWidth = state.canvasWidth,
                    canvasHeight = state.canvasHeight,
                )

                // 生成新障碍物
                spawnCounter++
                val interval = Survive30sEngine.spawnInterval(newElapsed)
                if (spawnCounter >= interval) {
                    spawnCounter = 0
                    obstacles = obstacles + Survive30sEngine.spawnObstacles(
                        canvasWidth = state.canvasWidth,
                        canvasHeight = state.canvasHeight,
                        elapsedSec = newElapsed,
                        phase = state.phase,
                    )
                }

                val (nearMissedObstacles, nearMissDelta) = Survive30sEngine.markNearMisses(
                    player = updatedPlayer,
                    obstacles = obstacles,
                )
                obstacles = nearMissedObstacles
                // 节流: 擦身音效最快 150ms 一次, 钻障碍群时不会机枪连发
                if (nearMissDelta > 0 && now - lastNearMissSoundNs > NEAR_MISS_SOUND_MIN_GAP_NS) {
                    lastNearMissSoundNs = now
                    playSound(SOUND_NEAR_MISS)
                }

                val totalCharge = state.nearMissCharge + nearMissDelta
                val earnedShield = totalCharge / NEAR_MISS_PER_SHIELD
                val shieldCount = (state.shieldCount + earnedShield).coerceAtMost(MAX_SHIELDS)
                if (shieldCount > state.shieldCount) playSound(SOUND_SHIELD_GAIN)
                val newCharge = if (shieldCount >= MAX_SHIELDS) {
                    0
                } else {
                    totalCharge % NEAR_MISS_PER_SHIELD
                }

                val newPhase = phaseFor(newElapsed)
                if (newPhase != state.phase) playSound(SOUND_PHASE_CHANGE)

                var invincibleSec = (state.invincibleSec - deltaSec).coerceAtLeast(0f)
                val dangerLevel = Survive30sEngine.dangerLevel(updatedPlayer, obstacles)

                // 碰撞检测
                val hit = obstacles.any { obs ->
                    Survive30sEngine.checkCollision(updatedPlayer, obs)
                }

                if (hit && invincibleSec <= 0f && shieldCount <= 0) {
                    onGameOver(newElapsed)
                    break
                }

                val resolvedObstacles = if (hit && invincibleSec <= 0f && shieldCount > 0) {
                    invincibleSec = INVINCIBLE_AFTER_SHIELD_SEC
                    playSound(SOUND_SHIELD_BLOCK)
                    Survive30sEngine.clearNearbyObstacles(updatedPlayer, obstacles)
                } else {
                    obstacles
                }

                _uiState.update {
                    it.copy(
                        elapsedSec = newElapsed,
                        player = updatedPlayer,
                        obstacles = resolvedObstacles,
                        shieldCount = if (hit && invincibleSec > 0f && shieldCount > 0) {
                            shieldCount - 1
                        } else shieldCount,
                        nearMissCount = it.nearMissCount + nearMissDelta,
                        nearMissCharge = newCharge,
                        dangerLevel = dangerLevel,
                        invincibleSec = invincibleSec,
                        phase = newPhase,
                    )
                }
            }
        }
    }

    private fun smoothPlayer(
        state: Survive30sUiState,
        deltaSec: Float,
    ): Player {
        val player = state.player
        if (player.radius <= 0f) return player

        val desiredX = targetPlayerX.takeIf(Float::isFinite) ?: player.x
        val desiredY = targetPlayerY.takeIf(Float::isFinite) ?: player.y
        val followFactor = 1f - exp(-PLAYER_FOLLOW_RESPONSE * deltaSec)

        return player.copy(
            x = lerp(player.x, desiredX, followFactor),
            y = lerp(player.y, desiredY, followFactor),
        )
    }

    private fun phaseFor(elapsedSec: Float): SurvivalPhase = when {
        elapsedSec < 10f -> SurvivalPhase.Warmup
        elapsedSec < 20f -> SurvivalPhase.Rush
        else -> SurvivalPhase.Storm
    }

    private fun lerp(start: Float, end: Float, fraction: Float): Float {
        return start + (end - start) * fraction.coerceIn(0f, 1f)
    }

    private fun onGameOver(elapsed: Float) {
        // 暂停请求已下达：这一帧不应该算"死亡"。状态留给 lifecycle observer 翻成 PAUSED。
        if (pauseRequested) return
        gameLoopJob?.cancel()
        audioPlayer.stopBackground()
        playSound(SOUND_GAME_OVER)
        val best = if (elapsed > _uiState.value.bestTime) {
            saveBestTime(elapsed)
            elapsed
        } else {
            _uiState.value.bestTime
        }
        _uiState.update {
            it.copy(
                gameState = GameState.GAME_OVER,
                elapsedSec = elapsed,
                bestTime = best,
                dangerLevel = 1f,
                invincibleSec = 0f,
            )
        }
    }

    private fun onWin(elapsed: Float) {
        // 同样的 race 保护：暂停请求优先于胜利判定
        if (pauseRequested) return
        gameLoopJob?.cancel()
        audioPlayer.stopBackground()
        playSound(SOUND_WIN)
        val best = if (elapsed > _uiState.value.bestTime) {
            saveBestTime(elapsed)
            elapsed
        } else {
            _uiState.value.bestTime
        }
        _uiState.update {
            it.copy(
                gameState = GameState.WIN,
                elapsedSec = Survive30sEngine.GAME_DURATION,
                bestTime = best,
                dangerLevel = 0f,
                invincibleSec = 0f,
                phase = SurvivalPhase.Storm,
            )
        }
        val winPoints = RemoteConfigStorage.getRemoteConfig().survive30sWinPoints ?: 300
        BaseUtils.rewardPoints(
            points = winPoints,
            desc = "\u901a\u5173\u8eb2\u907f30\u79d2\u6e38\u620f\u5956\u52b1",
            source = "survive_30s_game",
            bizId = "",
            showToast = true
        )
        // 通关时刻上报给应用内评分弹层:奖励 Toast 消失后才会弹(见 InAppReviewPrompt 的等待逻辑)
        ReviewPromptHost.notifySuccess(ReviewPromptTrigger.GAME_WIN)
    }

    // ─── 音效 ────────────────────────────────────────────────────────────────

    /**
     * 播放音效。短音效用 [NetworkAudioPlayer.playEffect]（同 URL 已缓存，秒开）；
     * BGM 用 [NetworkAudioPlayer.playBackground]（循环，同 URL 播放中不重启）。
     * 播放失败静默吞掉（网络问题不该影响游戏本身）。
     */
    private fun playSound(url: String, isBackground: Boolean = false) {
        componentScope.launch {
            runCatching {
                if (isBackground) audioPlayer.playBackground(url) else audioPlayer.playEffect(url)
            }
        }
    }

    // ─── MMKV 辅助方法 ─────────────────────────────────────────────────────────

    private fun loadBestTime(): Float = mkv.decodeFloat("best_time", 0f)

    private fun saveBestTime(time: Float) = mkv.encode("best_time", time)

    @AssistedFactory
    interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit
        ): Survive30sComponent
    }
}
