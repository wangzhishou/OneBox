package com.wanbaohe.survive30s.engine

import com.wanbaohe.survive30s.component.SurvivalPhase
import kotlin.random.Random

/**
 * 躲避游戏引擎
 *
 * 负责：
 * 1. 管理玩家位置、障碍物生成与移动
 * 2. 碰撞检测（圆形碰撞）
 * 3. 难度曲线（随时间加速 + 增加障碍密度 + 多方向威胁）
 * 4. 60fps 帧更新逻辑
 */
object Survive30sEngine {

    /** 游戏总时长（秒） */
    const val GAME_DURATION = 30f

    /** 玩家半径占画布宽度比例 */
    const val PLAYER_RADIUS_RATIO = 0.035f

    /** 障碍物最小半径占画布宽度比例 */
    private const val OBS_MIN_RADIUS_RATIO = 0.02f

    /** 障碍物最大半径占画布宽度比例 */
    private const val OBS_MAX_RADIUS_RATIO = 0.06f

    /** 初始障碍物生成间隔（帧数） */
    private const val INITIAL_SPAWN_INTERVAL = 28

    /** 最终障碍物生成间隔（帧数） */
    private const val FINAL_SPAWN_INTERVAL = 6

    /** 初始垂直速度占画布高度比例（每秒） */
    private const val INITIAL_VERTICAL_SPEED_RATIO = 0.35f

    /** 最终垂直速度占画布高度比例（每秒） */
    private const val FINAL_VERTICAL_SPEED_RATIO = 0.9f

    /** 初始水平速度占画布宽度比例（每秒） */
    private const val INITIAL_HORIZONTAL_SPEED_RATIO = 0.32f

    /** 最终水平速度占画布宽度比例（每秒） */
    private const val FINAL_HORIZONTAL_SPEED_RATIO = 0.85f

    /** 障碍物水平漂移速度占画布宽度比例（每秒） */
    private const val MAX_DRIFT_SPEED_RATIO = 0.16f

    /** 近身闪避判定倍率 */
    private const val NEAR_MISS_RATIO = 1.35f

    /** 护盾爆发清障范围倍率 */
    private const val SHIELD_BLAST_RATIO = 3.2f

    private var nextObstacleId: Long = 0L

    /**
     * 生成新障碍物
     *
     * 根据游戏阶段决定威胁方向：
     * - Warmup：只有顶部下落
     * - Rush：加入左右横飞
     * - Storm：再加入底部上升，形成 360° 压力
     *
     * @param canvasWidth  画布宽度（px）
     * @param canvasHeight 画布高度（px）
     * @param elapsedSec   已经过的秒数
     * @param phase        当前阶段
     * @return 新障碍物列表
     */
    fun spawnObstacles(
        canvasWidth: Float,
        canvasHeight: Float,
        elapsedSec: Float,
        phase: SurvivalPhase,
    ): List<Obstacle> {
        val difficulty = difficultyFactor(elapsedSec)
        val count = if (difficulty > 0.5f) Random.nextInt(1, 3) else 1

        val verticalSpeed = randomVerticalSpeed(canvasHeight, difficulty)
        val horizontalSpeed = randomHorizontalSpeed(canvasWidth, difficulty)
        val maxDrift = canvasWidth * (MAX_DRIFT_SPEED_RATIO * (0.45f + difficulty))

        return List(count) {
            val radius = canvasWidth * randomInRange(
                OBS_MIN_RADIUS_RATIO, OBS_MAX_RADIUS_RATIO
            )

            val type = when (phase) {
                SurvivalPhase.Warmup -> ObstacleType.TOP_FALL
                SurvivalPhase.Rush -> when (Random.nextInt(3)) {
                    0 -> ObstacleType.LEFT_FLY
                    1 -> ObstacleType.RIGHT_FLY
                    else -> ObstacleType.TOP_FALL
                }
                SurvivalPhase.Storm -> when (Random.nextInt(4)) {
                    0 -> ObstacleType.LEFT_FLY
                    1 -> ObstacleType.RIGHT_FLY
                    2 -> ObstacleType.BOTTOM_RISE
                    else -> ObstacleType.TOP_FALL
                }
            }

            spawnByType(
                type = type,
                canvasWidth = canvasWidth,
                canvasHeight = canvasHeight,
                radius = radius,
                verticalSpeed = verticalSpeed,
                horizontalSpeed = horizontalSpeed,
                maxDrift = maxDrift,
            )
        }
    }

    private fun spawnByType(
        type: ObstacleType,
        canvasWidth: Float,
        canvasHeight: Float,
        radius: Float,
        verticalSpeed: Float,
        horizontalSpeed: Float,
        maxDrift: Float,
    ): Obstacle {
        val colorIndex = Random.nextInt(0, 6)
        return when (type) {
            ObstacleType.TOP_FALL -> Obstacle(
                id = nextObstacleId++,
                x = Random.nextFloat() * (canvasWidth - 2 * radius) + radius,
                y = -radius * 2,
                radius = radius,
                velocityX = randomInRange(-maxDrift, maxDrift),
                velocityY = verticalSpeed,
                type = type,
                colorIndex = colorIndex,
            )

            ObstacleType.BOTTOM_RISE -> Obstacle(
                id = nextObstacleId++,
                x = Random.nextFloat() * (canvasWidth - 2 * radius) + radius,
                y = canvasHeight + radius * 2,
                radius = radius,
                velocityX = randomInRange(-maxDrift, maxDrift),
                velocityY = -verticalSpeed,
                type = type,
                colorIndex = colorIndex,
            )

            ObstacleType.LEFT_FLY -> Obstacle(
                id = nextObstacleId++,
                x = -radius * 2,
                y = Random.nextFloat() * (canvasHeight - 2 * radius) + radius,
                radius = radius,
                velocityX = horizontalSpeed,
                velocityY = randomInRange(-maxDrift * 0.6f, maxDrift * 0.6f),
                type = type,
                colorIndex = colorIndex,
            )

            ObstacleType.RIGHT_FLY -> Obstacle(
                id = nextObstacleId++,
                x = canvasWidth + radius * 2,
                y = Random.nextFloat() * (canvasHeight - 2 * radius) + radius,
                radius = radius,
                velocityX = -horizontalSpeed,
                velocityY = randomInRange(-maxDrift * 0.6f, maxDrift * 0.6f),
                type = type,
                colorIndex = colorIndex,
            )
        }
    }

    private fun randomVerticalSpeed(canvasHeight: Float, difficulty: Float): Float {
        return canvasHeight * randomInRange(
            INITIAL_VERTICAL_SPEED_RATIO,
            INITIAL_VERTICAL_SPEED_RATIO + difficulty * (FINAL_VERTICAL_SPEED_RATIO - INITIAL_VERTICAL_SPEED_RATIO)
        )
    }

    private fun randomHorizontalSpeed(canvasWidth: Float, difficulty: Float): Float {
        return canvasWidth * randomInRange(
            INITIAL_HORIZONTAL_SPEED_RATIO,
            INITIAL_HORIZONTAL_SPEED_RATIO + difficulty * (FINAL_HORIZONTAL_SPEED_RATIO - INITIAL_HORIZONTAL_SPEED_RATIO)
        )
    }

    /**
     * 更新所有障碍物位置
     *
     * @param obstacles 当前障碍物列表
     * @param deltaTime 帧间隔（秒）
     * @param canvasWidth 画布宽度
     * @param canvasHeight 画布高度
     * @return 移动后的障碍物列表（已移除超出画布的）
     */
    fun updateObstacles(
        obstacles: List<Obstacle>,
        deltaTime: Float,
        canvasWidth: Float,
        canvasHeight: Float,
    ): List<Obstacle> = buildList(obstacles.size) {
        obstacles.forEach { obs ->
            val newX = obs.x + obs.velocityX * deltaTime
            val newY = obs.y + obs.velocityY * deltaTime

            // 根据运动方向判断是否已经离开画布（从对面出去）；
            // 刚生成在屏幕外时不要移除，否则第一帧就会被删掉。
            val leftScreen = when (obs.type) {
                ObstacleType.TOP_FALL -> newY - obs.radius > canvasHeight
                ObstacleType.BOTTOM_RISE -> newY + obs.radius < 0f
                ObstacleType.LEFT_FLY -> newX - obs.radius > canvasWidth
                ObstacleType.RIGHT_FLY -> newX + obs.radius < 0f
            }
            if (leftScreen) return@forEach

            val clampedX = newX.coerceIn(obs.radius, canvasWidth - obs.radius)
            val clampedY = newY.coerceIn(obs.radius, canvasHeight - obs.radius)

            // 仅对同方向轴线做反弹：上下弹只反弹左右边界，左右弹只反弹上下边界
            val bounceX = clampedX != newX && obs.type.isVertical
            val bounceY = clampedY != newY && obs.type.isHorizontal

            add(
                obs.copy(
                    x = clampedX,
                    y = clampedY,
                    velocityX = if (bounceX) -obs.velocityX else obs.velocityX,
                    velocityY = if (bounceY) -obs.velocityY else obs.velocityY,
                )
            )
        }
    }

    /**
     * 计算当前应使用的生成间隔（帧数）
     */
    fun spawnInterval(elapsedSec: Float): Int {
        val d = difficultyFactor(elapsedSec)
        return (INITIAL_SPAWN_INTERVAL - d * (INITIAL_SPAWN_INTERVAL - FINAL_SPAWN_INTERVAL)).toInt()
            .coerceIn(FINAL_SPAWN_INTERVAL, INITIAL_SPAWN_INTERVAL)
    }

    /**
     * 碰撞检测：玩家（圆形）与障碍物（圆形）
     *
     * 判定距离使用半径和的 0.95 倍，让视觉本体与判定基本一致，避免“明明没碰到却死了”的不公平感。
     */
    fun checkCollision(player: Player, obstacle: Obstacle): Boolean {
        val dx = player.x - obstacle.x
        val dy = player.y - obstacle.y
        val touchDistance = (player.radius + obstacle.radius) * 0.95f
        return dx * dx + dy * dy < touchDistance * touchDistance
    }

    fun markNearMisses(
        player: Player,
        obstacles: List<Obstacle>,
    ): Pair<List<Obstacle>, Int> {
        var gained = 0
        val nearMissed = obstacles.map { obstacle ->
            if (obstacle.didNearMiss) return@map obstacle

            val dx = player.x - obstacle.x
            val dy = player.y - obstacle.y
            val nearMissDistance = (player.radius + obstacle.radius) * NEAR_MISS_RATIO
            if (dx * dx + dy * dy < nearMissDistance * nearMissDistance) {
                gained += 1
                obstacle.copy(didNearMiss = true)
            } else {
                obstacle
            }
        }

        return nearMissed to gained
    }

    fun dangerLevel(player: Player, obstacles: List<Obstacle>): Float {
        if (obstacles.isEmpty()) return 0f

        val nearestGap = obstacles.minOf { obstacle ->
            val dx = player.x - obstacle.x
            val dy = player.y - obstacle.y
            val distanceSq = dx * dx + dy * dy
            val safeRadius = player.radius + obstacle.radius
            distanceSq - safeRadius * safeRadius
        }

        return (1f - nearestGap / (player.radius * player.radius * 10f)).coerceIn(0f, 1f)
    }

    fun clearNearbyObstacles(player: Player, obstacles: List<Obstacle>): List<Obstacle> {
        val blastDistance = player.radius * SHIELD_BLAST_RATIO
        val blastDistanceSq = blastDistance * blastDistance
        return obstacles.filter { obstacle ->
            val dx = player.x - obstacle.x
            val dy = player.y - obstacle.y
            dx * dx + dy * dy > blastDistanceSq
        }
    }

    /**
     * 计算难度因子 [0, 1]
     * - 0 = 刚开始
     * - 1 = 最后一秒
     */
    private fun difficultyFactor(elapsedSec: Float): Float =
        (elapsedSec / GAME_DURATION).coerceIn(0f, 1f)

    private fun randomInRange(min: Float, max: Float): Float =
        min + Random.nextFloat() * (max - min)
}

/** 障碍物运动方向 */
enum class ObstacleType {
    TOP_FALL,
    BOTTOM_RISE,
    LEFT_FLY,
    RIGHT_FLY;

    val isVertical: Boolean get() = this == TOP_FALL || this == BOTTOM_RISE
    val isHorizontal: Boolean get() = this == LEFT_FLY || this == RIGHT_FLY
}

/** 玩家数据 */
data class Player(
    val x: Float,
    val y: Float,
    val radius: Float,
    /** 拖拽偏移量，用于平滑跟随手指 */
    val offsetX: Float = 0f,
)

/** 障碍物数据 */
data class Obstacle(
    val id: Long,
    val x: Float,
    val y: Float,
    val radius: Float,
    /** 每秒水平移动像素（正向右） */
    val velocityX: Float = 0f,
    /** 每秒垂直移动像素（正向下） */
    val velocityY: Float = 0f,
    /** 运动方向类型 */
    val type: ObstacleType = ObstacleType.TOP_FALL,
    /** 颜色索引，用于渲染差异化 */
    val colorIndex: Int = 0,
    /** 是否已被记为一次近身闪避 */
    val didNearMiss: Boolean = false,
)
