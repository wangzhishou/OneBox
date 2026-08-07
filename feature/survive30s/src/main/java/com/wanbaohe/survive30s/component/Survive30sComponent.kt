package com.wanbaohe.survive30s.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.tencent.mmkv.MMKV
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.shifenmiao.common.utils.BaseUtils
import com.shifenmiao.storage.RemoteConfigStorage
import com.wanbaohe.survive30s.engine.Player
import com.wanbaohe.survive30s.engine.Survive30sEngine
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Job
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
) : BaseComponent(dispatchersHolder, componentContext) {

    companion object {
        private const val FRAME_INTERVAL_MS = 16L
        private const val PLAYER_FOLLOW_RESPONSE = 60f
        private const val INVINCIBLE_AFTER_SHIELD_SEC = 1.1f
        private const val NEAR_MISS_PER_SHIELD = 3
        private const val MAX_SHIELDS = 2
    }

    // ─── MMKV 持久化（必须在 _uiState 之前声明，因为 _uiState 初始化时需要访问 mkv） ──

    private val mkv by lazy { MMKV.mmkvWithID("survive30s") }

    private val _uiState = MutableStateFlow(Survive30sUiState(bestTime = loadBestTime()))
    val uiState = _uiState.asStateFlow()

    /** 游戏主循环 Job */
    private var gameLoopJob: Job? = null

    /** 障碍物生成帧计数器 */
    private var spawnCounter = 0

    /** 玩家目标位置，由触摸输入直接更新，在主循环中平滑跟随 */
    private var targetPlayerX = Float.NaN
    private var targetPlayerY = Float.NaN

    init {
        componentContext.lifecycle.doOnDestroy {
            gameLoopJob?.cancel()
        }
    }

    // ─── 公开交互接口 ─────────────────────────────────────────────────────────

    /** 设定画布尺寸并初始化玩家位置 */
    fun initCanvas(width: Float, height: Float) {
        if (_uiState.value.canvasWidth > 0f) return
        val playerRadius = width * Survive30sEngine.PLAYER_RADIUS_RATIO
        val initialX = width / 2f
        val initialY = height * 0.85f
        targetPlayerX = initialX
        targetPlayerY = initialY
        _uiState.update {
            it.copy(
                canvasWidth = width,
                canvasHeight = height,
                player = Player(
                    x = initialX,
                    y = initialY,
                    radius = playerRadius
                )
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
    }

    /** 拖动更新玩家位置（支持上下左右全方向移动）
     *
     * @param x 目标 X 坐标
     * @param y 目标 Y 坐标，null 表示仅水平移动
     */
    fun movePlayerTo(x: Float, y: Float? = null) {
        val state = _uiState.value
        if (state.gameState != GameState.PLAYING) return
        targetPlayerX = x.coerceIn(state.player.radius, state.canvasWidth - state.player.radius)
        // 放开垂直移动范围：允许在全画布上下移动，仅约束在画布边缘内
        targetPlayerY = (y ?: state.player.y).coerceIn(
            state.player.radius,
            state.canvasHeight - state.player.radius
        )
    }

    // ─── 游戏主循环 ────────────────────────────────────────────────────────────

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = componentScope.launch {
            var lastTime = System.nanoTime()
            while (true) {
                delay(FRAME_INTERVAL_MS)
                val now = System.nanoTime()
                val deltaSec = (now - lastTime) / 1_000_000_000f
                lastTime = now

                val state = _uiState.value
                if (state.gameState != GameState.PLAYING) break

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

                val totalCharge = state.nearMissCharge + nearMissDelta
                val earnedShield = totalCharge / NEAR_MISS_PER_SHIELD
                val shieldCount = (state.shieldCount + earnedShield).coerceAtMost(MAX_SHIELDS)
                val newCharge = if (shieldCount >= MAX_SHIELDS) {
                    0
                } else {
                    totalCharge % NEAR_MISS_PER_SHIELD
                }

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
                        phase = phaseFor(newElapsed),
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
        gameLoopJob?.cancel()
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
        gameLoopJob?.cancel()
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
