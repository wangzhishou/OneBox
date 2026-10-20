package com.wanbaohe.core.ui.utils.blessing

import android.os.SystemClock
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wanbaohe.core.ui.R
import com.t8rin.modalsheet.FullscreenPopup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

enum class BlessingEffectType(val durationMs: Long) {
    WoodenFish(950L),
    WealthGod(2_200L),
    Guanyin(2_000L),
    Incense(2_000L),
    Hamsa(2_000L),
    Cupid(2_100L),
    Clover(2_000L),
    Angel(2_000L),
    LuckyCat(2_200L),
    Dreamcatcher(2_100L),
    Wishbone(1_800L),
    Horseshoe(2_000L),
}

@Stable
class BlessingEffectHostState {
    private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
    private var completionJob: Job? = null
    private val _trigger = mutableIntStateOf(0)
    private val _type = mutableStateOf<BlessingEffectType?>(null)
    private val _isActive = mutableStateOf(false)
    private val _woodenFishEvents = mutableStateListOf<Int>()
    private var endsAtUptime = 0L

    val trigger: Int get() = _trigger.intValue
    val type: BlessingEffectType? get() = _type.value
    val isActive: Boolean get() = _isActive.value
    val woodenFishEvents: List<Int> get() = _woodenFishEvents

    @Synchronized
    fun show(type: BlessingEffectType): Boolean {
        if (type == BlessingEffectType.WoodenFish) {
            return showWoodenFish()
        }
        if (_isActive.value && SystemClock.uptimeMillis() < endsAtUptime) return false

        completionJob?.cancel()
        endsAtUptime = SystemClock.uptimeMillis() + type.durationMs
        _type.value = type
        _isActive.value = true
        _trigger.intValue += 1
        completionJob = scope.launch {
            delay(type.durationMs)
            _isActive.value = false
            _type.value = null
            endsAtUptime = 0L
        }
        return true
    }

    private fun showWoodenFish(): Boolean {
        if (_isActive.value && _type.value != BlessingEffectType.WoodenFish) return false

        _type.value = BlessingEffectType.WoodenFish
        _isActive.value = true
        _trigger.intValue += 1
        val eventId = _trigger.intValue
        _woodenFishEvents.add(eventId)
        scope.launch {
            delay(BlessingEffectType.WoodenFish.durationMs)
            _woodenFishEvents.remove(eventId)
            if (_woodenFishEvents.isEmpty() && _type.value == BlessingEffectType.WoodenFish) {
                _isActive.value = false
                _type.value = null
            }
        }
        return true
    }

    fun dismiss() {
        completionJob?.cancel()
        completionJob = null
        _isActive.value = false
        _type.value = null
        _woodenFishEvents.clear()
        endsAtUptime = 0L
    }
}

@Composable
fun BlessingEffectHost(
    hostState: BlessingEffectHostState,
) {
    val isActive = hostState.isActive
    val type = hostState.type
    val trigger = hostState.trigger
    if (!isActive || type == null) return

    if (type == BlessingEffectType.WoodenFish) {
        FullscreenPopup(
            placeAboveAll = true,
            isTouchable = false,
        ) {
            WoodenFishEffect(eventIds = hostState.woodenFishEvents)
        }
        return
    }

    FullscreenPopup(placeAboveAll = true) {
        key(trigger) {
            when (type) {
                BlessingEffectType.WealthGod,
                BlessingEffectType.LuckyCat,
                BlessingEffectType.Horseshoe,
                -> WealthGodEffect()
                BlessingEffectType.Guanyin -> GuanyinEffect()
                BlessingEffectType.Incense -> IncenseEffect()
                BlessingEffectType.Hamsa -> RingGlowEffect(color = MaterialTheme.colorScheme.onPrimaryContainer)
                BlessingEffectType.Angel -> RingGlowEffect(color = MaterialTheme.colorScheme.onTertiaryContainer)
                BlessingEffectType.Cupid -> FloatingShapeEffect(
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    shape = FloatingShape.Heart,
                )
                BlessingEffectType.Clover -> FloatingShapeEffect(
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    shape = FloatingShape.Leaf,
                )
                BlessingEffectType.Dreamcatcher -> FloatingShapeEffect(
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    shape = FloatingShape.Star,
                )
                BlessingEffectType.Wishbone -> SparkleBurstEffect(
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                BlessingEffectType.WoodenFish -> Unit
            }
        }
    }
}

@Composable
private fun WoodenFishEffect(
    eventIds: List<Int>,
) {
    val message = stringResource(R.string.blessing_effect_merit_plus_one)
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }
        eventIds.forEach { eventId ->
            key(eventId) {
                FloatingMeritBubble(
                    message = message,
                    horizontalOffsetPx = widthPx *
                            (((eventId * 37) % 70) / 100f - 0.35f),
                    travelDistancePx = heightPx * (0.35f + (eventId % 4) * 0.05f),
                )
            }
        }
    }
}

@Composable
private fun FloatingMeritBubble(
    message: String,
    horizontalOffsetPx: Float,
    travelDistancePx: Float,
) {
    val progress = remember { Animatable(0f) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 920, easing = LinearEasing),
        )
    }
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                translationX = horizontalOffsetPx
                translationY = travelDistancePx * (0.42f - progress.value)
                alpha = sin(progress.value * PI).toFloat().coerceIn(0f, 1f)
                scaleX = 0.82f + progress.value * 0.18f
                scaleY = scaleX
            },
        color = Color.Transparent,
    ) {
        Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Composable
private fun WealthGodEffect() {
    val particles = remember {
        List(42) {
            IngotParticleSpec(
                sizeDp = Random.nextInt(from = 32, until = 58),
                rotation = Random.nextInt(from = 180, until = 420).toFloat() *
                        if (Random.nextBoolean()) 1f else -1f,
                horizontalVelocity = Random.nextFloat() * 1.5f - 0.75f,
                verticalVelocity = -(0.55f + Random.nextFloat() * 0.45f),
                gravity = 2.2f + Random.nextFloat() * 0.6f,
                delayMs = Random.nextLong(from = 0L, until = 160L),
                durationMs = Random.nextInt(from = 1_450, until = 1_900),
            )
        }
    }
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }
        particles.forEachIndexed { index, particle ->
            BurstingIngotImage(
                index = index,
                particle = particle,
                screenWidthPx = widthPx,
                screenHeightPx = heightPx,
            )
        }
    }
}

private data class IngotParticleSpec(
    val sizeDp: Int,
    val rotation: Float,
    val horizontalVelocity: Float,
    val verticalVelocity: Float,
    val gravity: Float,
    val delayMs: Long,
    val durationMs: Int,
)

@Composable
private fun BurstingIngotImage(
    index: Int,
    particle: IngotParticleSpec,
    screenWidthPx: Float,
    screenHeightPx: Float,
) {
    val progress = remember(index, particle) { Animatable(0f) }
    val density = LocalDensity.current
    val size = particle.sizeDp.dp
    androidx.compose.runtime.LaunchedEffect(index, particle) {
        delay(particle.delayMs)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = particle.durationMs,
                easing = LinearEasing,
            ),
        )
    }
    Image(
        painter = painterResource(R.drawable.blessing_ingot_particle),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .size(size)
            .graphicsLayer {
                val particleSizePx = with(density) { size.toPx() }
                val time = progress.value
                translationX = screenWidthPx / 2f - particleSizePx / 2f +
                        screenWidthPx * particle.horizontalVelocity * time
                translationY = screenHeightPx * 0.46f - particleSizePx / 2f +
                        screenHeightPx * (
                        particle.verticalVelocity * time +
                                0.5f * particle.gravity * time * time
                        )
                rotationZ = particle.rotation * progress.value
                alpha = when {
                    progress.value <= 0f -> 0f
                    progress.value >= 0.94f -> (1f - progress.value) / 0.06f
                    else -> 1f
                }
            },
    )
}

@Composable
private fun GuanyinEffect() {
    val progress = remember { Animatable(0f) }
    val primary = MaterialTheme.colorScheme.onTertiaryContainer
    val secondary = MaterialTheme.colorScheme.onPrimaryContainer
    androidx.compose.runtime.LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(durationMillis = 1_900, easing = LinearEasing))
    }
    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height * 0.55f)
        repeat(4) { ring ->
            val phase = (progress.value * 1.7f - ring * 0.2f).coerceIn(0f, 1f)
            drawCircle(
                color = secondary.copy(alpha = (1f - phase) * 0.2f),
                radius = size.minDimension * (0.08f + phase * 0.42f),
                center = center,
            )
        }
        repeat(22) { index ->
            val phase = (progress.value * 1.5f - index * 0.035f).coerceIn(0f, 1f)
            val x = size.width * (0.12f + ((index * 31) % 76) / 100f)
            val y = size.height * (0.82f - phase * (0.45f + index % 4 * 0.04f))
            rotate(degrees = index * 29f + phase * 80f, pivot = Offset(x, y)) {
                drawOval(
                    color = primary.copy(alpha = sin(phase * PI).toFloat() * 0.55f),
                    topLeft = Offset(x - 8.dp.toPx(), y - 15.dp.toPx()),
                    size = Size(16.dp.toPx(), 30.dp.toPx()),
                )
            }
        }
    }
}

@Composable
private fun RingGlowEffect(color: Color) {
    val progress = remember { Animatable(0f) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(durationMillis = 1_900, easing = LinearEasing))
    }
    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height * 0.48f)
        repeat(5) { ring ->
            val phase = (progress.value * 1.6f - ring * 0.16f).coerceIn(0f, 1f)
            drawCircle(
                color = color.copy(alpha = (1f - phase) * 0.22f),
                radius = size.minDimension * (0.06f + phase * 0.48f),
                center = center,
            )
        }
        repeat(18) { index ->
            val phase = (progress.value * 1.4f - index * 0.04f).coerceIn(0f, 1f)
            val angle = index * 20f
            val radius = size.minDimension * (0.12f + phase * 0.34f)
            val x = center.x + radius * sin(angle * PI / 180.0).toFloat()
            val y = center.y + radius * cos(angle * PI / 180.0).toFloat()
            drawCircle(
                color = color.copy(alpha = sin(phase * PI).toFloat() * 0.55f),
                radius = 3.dp.toPx(),
                center = Offset(x, y),
            )
        }
    }
}

private enum class FloatingShape { Heart, Leaf, Star }

@Composable
private fun FloatingShapeEffect(color: Color, shape: FloatingShape) {
    val particles = remember(shape) {
        List(28) {
            FloatingParticleSpec(
                sizeDp = Random.nextInt(from = 14, until = 28),
                horizontalVelocity = Random.nextFloat() * 1.2f - 0.6f,
                verticalVelocity = -(0.35f + Random.nextFloat() * 0.35f),
                delayMs = Random.nextLong(from = 0L, until = 180L),
                durationMs = Random.nextInt(from = 1_300, until = 1_800),
                rotation = Random.nextInt(from = 0, until = 360).toFloat(),
            )
        }
    }
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }
        particles.forEachIndexed { index, particle ->
            val progress = remember(index, particle) { Animatable(0f) }
            androidx.compose.runtime.LaunchedEffect(index, particle) {
                delay(particle.delayMs)
                progress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(particle.durationMs, easing = LinearEasing),
                )
            }
            val particleSize = particle.sizeDp.dp
            Canvas(
                modifier = Modifier
                    .size(particleSize)
                    .graphicsLayer {
                        val particleSizePx = with(density) { particleSize.toPx() }
                        val time = progress.value
                        translationX = widthPx / 2f - particleSizePx / 2f +
                                widthPx * particle.horizontalVelocity * time
                        // verticalVelocity is negative: particles should rise from mid-screen
                        translationY = heightPx * 0.58f - particleSizePx / 2f +
                                heightPx * particle.verticalVelocity * time
                        rotationZ = particle.rotation * time
                        alpha = sin(time * PI).toFloat().coerceIn(0f, 0.85f)
                    },
            ) {
                val w = size.width
                val h = size.height
                when (shape) {
                    FloatingShape.Heart -> {
                        val path = Path().apply {
                            moveTo(w * 0.5f, h * 0.85f)
                            cubicTo(w * 0.05f, h * 0.45f, w * 0.15f, h * 0.05f, w * 0.5f, h * 0.28f)
                            cubicTo(w * 0.85f, h * 0.05f, w * 0.95f, h * 0.45f, w * 0.5f, h * 0.85f)
                            close()
                        }
                        drawPath(path, color)
                    }
                    FloatingShape.Leaf -> drawOval(
                        color = color.copy(alpha = 0.85f),
                        topLeft = Offset(w * 0.2f, h * 0.05f),
                        size = Size(w * 0.6f, h * 0.9f),
                    )
                    FloatingShape.Star -> {
                        val cx = w / 2f
                        val cy = h / 2f
                        drawCircle(color = color, radius = w * 0.18f, center = Offset(cx, cy))
                        drawCircle(
                            color = color.copy(alpha = 0.55f),
                            radius = w * 0.38f,
                            center = Offset(cx, cy),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx()),
                        )
                    }
                }
            }
        }
    }
}

private data class FloatingParticleSpec(
    val sizeDp: Int,
    val horizontalVelocity: Float,
    val verticalVelocity: Float,
    val delayMs: Long,
    val durationMs: Int,
    val rotation: Float,
)

@Composable
private fun SparkleBurstEffect(color: Color) {
    val progress = remember { Animatable(0f) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(durationMillis = 1_700, easing = LinearEasing))
    }
    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height * 0.5f)
        repeat(24) { index ->
            val phase = (progress.value * 1.3f - index * 0.025f).coerceIn(0f, 1f)
            val angle = (index * 15f + phase * 20f) * PI / 180.0
            val radius = size.minDimension * phase * 0.42f
            val x = center.x + radius * cos(angle).toFloat()
            val y = center.y + radius * sin(angle).toFloat()
            drawCircle(
                color = color.copy(alpha = sin(phase * PI).toFloat() * 0.7f),
                radius = (2.5f + (index % 3) * 1.5f).dp.toPx(),
                center = Offset(x, y),
            )
        }
    }
}

@Composable
private fun IncenseEffect() {
    val progress = remember { Animatable(0f) }
    val smoke = MaterialTheme.colorScheme.onSurfaceVariant
    val ember = MaterialTheme.colorScheme.onTertiaryContainer
    androidx.compose.runtime.LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(durationMillis = 1_900, easing = LinearEasing))
    }
    Canvas(modifier = Modifier.fillMaxSize()) {
        val baseY = size.height * 0.82f
        repeat(3) { column ->
            val baseX = size.width * (0.42f + column * 0.08f)
            drawCircle(
                color = ember.copy(alpha = (1f - progress.value) * 0.8f),
                radius = 4.dp.toPx(),
                center = Offset(baseX, baseY),
            )
            repeat(12) { index ->
                val phase = (progress.value * 1.5f - index * 0.07f).coerceIn(0f, 1f)
                val wave = sin((phase * 4f + column) * PI).toFloat()
                val center = Offset(
                    x = baseX + wave * 18.dp.toPx(),
                    y = baseY - phase * size.height * 0.62f,
                )
                drawCircle(
                    color = smoke.copy(alpha = sin(phase * PI).toFloat() * 0.18f),
                    radius = (7 + index * 0.7f).dp.toPx(),
                    center = center,
                )
            }
        }
        val glow = Path().apply {
            moveTo(size.width * 0.3f, baseY + 12.dp.toPx())
            quadraticTo(
                size.width * 0.5f,
                baseY - 18.dp.toPx(),
                size.width * 0.7f,
                baseY + 12.dp.toPx(),
            )
        }
        translate(top = progress.value * -4.dp.toPx()) {
            drawPath(
                path = glow,
                color = ember.copy(alpha = (1f - progress.value) * 0.16f),
            )
        }
    }
}
