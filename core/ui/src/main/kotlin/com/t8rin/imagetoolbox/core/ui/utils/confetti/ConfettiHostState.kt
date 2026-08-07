/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2026 T8RIN (Malik Mukhametzyanov)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.t8rin.imagetoolbox.core.ui.utils.confetti

import android.os.SystemClock
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.t8rin.imagetoolbox.core.settings.domain.model.ColorHarmonizer
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.widget.other.ToastData
import com.t8rin.modalsheet.FullscreenPopup
import com.t8rin.imagetoolbox.core.ui.widget.other.ToastDuration
import com.t8rin.imagetoolbox.core.ui.widget.other.ToastVisuals
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party

enum class ConfettiIntensity(
    val durationMs: Long,
    val particleMultiplier: Float
) {
    Subtle(2200L, 0.6f),
    Normal(3500L, 1.2f),
    Mega(5500L, 1.8f);
}

@Stable
@Immutable
class ConfettiHostState {

    private val internalScope = CoroutineScope(
        Dispatchers.Main.immediate + SupervisorJob()
    )

    private var autoDismissJob: Job? = null

    @Volatile
    private var lastTriggerUptime: Long = 0L

    private val _trigger = mutableIntStateOf(0)
    private val _intensity = mutableStateOf(ConfettiIntensity.Normal)
    private val _endsAtUptime = mutableLongStateOf(0L)

    val trigger: Int get() = _trigger.intValue
    val intensity: ConfettiIntensity get() = _intensity.value
    val endsAtUptime: Long get() = _endsAtUptime.longValue

    private val activeVisualData = ActiveConfettiData(this)

    val currentToastData: ToastData?
        get() = if (isActive) activeVisualData else null

    private val isActive: Boolean
        get() = _trigger.intValue > 0 &&
                SystemClock.uptimeMillis() < _endsAtUptime.longValue

    fun showConfetti(
        intensity: ConfettiIntensity = ConfettiIntensity.Normal,
        cooldownMs: Long = 180L
    ) {
        val now = SystemClock.uptimeMillis()
        if (cooldownMs > 0L &&
            intensity != ConfettiIntensity.Mega &&
            now - lastTriggerUptime < cooldownMs
        ) return
        lastTriggerUptime = now

        autoDismissJob?.cancel()
        autoDismissJob = internalScope.launch {
            _intensity.value = intensity
            _endsAtUptime.longValue = now + intensity.durationMs
            _trigger.intValue = _trigger.intValue + 1
            delay(intensity.durationMs)
        }
    }

    fun dismiss() {
        autoDismissJob?.cancel()
        autoDismissJob = null
        _trigger.intValue = 0
        _endsAtUptime.longValue = 0L
    }

    fun release() {
        dismiss()
        internalScope.cancel()
    }

    private class ActiveConfettiData(
        private val host: ConfettiHostState
    ) : ToastData {
        override val visuals: ToastVisuals = object : ToastVisuals {
            override val message: String = ""
            override val icon: ImageVector? = null
            override val duration: ToastDuration = ToastDuration(0L)
        }
        override fun dismiss() = host.dismiss()
    }
}

@Composable
fun ConfettiHost(
    hostState: ConfettiHostState,
    particles: @Composable (harmonizer: Color, intensity: ConfettiIntensity) -> List<Party>
) {
    val settingsState = LocalSettingsState.current
    val colorScheme = MaterialTheme.colorScheme
    val colorHarmonizer = settingsState.confettiColorHarmonizer
    val harmonizationLevel = settingsState.confettiHarmonizationLevel

    val harmonizationColor = when (colorHarmonizer) {
        is ColorHarmonizer.Custom -> Color(colorHarmonizer.color)
        ColorHarmonizer.Primary -> colorScheme.primary
        ColorHarmonizer.Secondary -> colorScheme.secondary
        ColorHarmonizer.Tertiary -> colorScheme.tertiary
    }

    val trigger = hostState.trigger
    val intensity = hostState.intensity

    val confettiHarmonized = remember(harmonizationColor, harmonizationLevel) {
        harmonizationColor.copy(harmonizationLevel)
    }

    val activeParties = particles(confettiHarmonized, intensity)

    // ConfettiHostState is a process-wide singleton shared by every activity, and its
    // trigger is never reset after firing. Only react to triggers fired after this host
    // entered composition, otherwise a stale trigger replays confetti (plus flash and
    // haptics) in each newly opened screen, e.g. the media/file picker.
    val initialTrigger = remember { hostState.trigger }
    val visible = trigger > 0 && trigger != initialTrigger

    val scale = remember { Animatable(0.92f) }
    val flashAlpha = remember { Animatable(0f) }
    val haptic = LocalHapticFeedback.current
    val hapticsEnabled = settingsState.hapticsStrength > 0

    LaunchedEffect(trigger, hapticsEnabled) {
        if (visible && hapticsEnabled) {
            haptic.performHapticFeedback(
                when (intensity) {
                    ConfettiIntensity.Mega -> HapticFeedbackType.LongPress
                    ConfettiIntensity.Normal -> HapticFeedbackType.Confirm
                    ConfettiIntensity.Subtle -> HapticFeedbackType.TextHandleMove
                }
            )
        }
    }

    LaunchedEffect(trigger) {
        if (visible) {
            scale.snapTo(if (intensity == ConfettiIntensity.Mega) 0.6f else 0.72f)
            scale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 420, easing = LinearEasing)
            )
            flashAlpha.snapTo(
                when (intensity) {
                    ConfettiIntensity.Mega -> 0.7f
                    ConfettiIntensity.Normal -> 0.45f
                    ConfettiIntensity.Subtle -> 0.22f
                }
            )
            flashAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 560, easing = LinearEasing)
            )
        }
    }

    FullscreenPopup(
        placeAboveAll = true
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    this.alpha = if (visible) 1f else 0f
                    scaleX = scale.value
                    scaleY = scale.value
                }
        ) {
            if (visible) {
                key(trigger) {
                    KonfettiView(
                        modifier = Modifier.fillMaxSize(),
                        parties = activeParties
                    )
                }
                ConfettiFlashOverlay(
                    color = confettiHarmonized,
                    intensity = flashAlpha.value
                )
            }
        }
    }
}

@Composable
private fun ConfettiFlashOverlay(
    color: Color,
    intensity: Float
) {
    if (intensity <= 0.01f) return
    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height * 0.5f)
        val radius = maxOf(size.width, size.height) * 0.85f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    color.copy(alpha = intensity),
                    color.copy(alpha = intensity * 0.35f),
                    Color.Transparent
                ),
                center = center,
                radius = radius
            ),
            radius = radius,
            center = center,
            blendMode = BlendMode.Plus
        )
    }
}

@Composable
fun ConfettiHost() {
    val settingsState = LocalSettingsState.current

    AnimatedVisibility(
        visible = settingsState.isConfettiEnabled,
        enter = fadeIn(animationSpec = tween(180)),
        exit = fadeOut(animationSpec = tween(180))
    ) {
        ConfettiHost(
            hostState = AppToastHost.confettiState,
            particles = { harmonizer, intensity ->
                val particlesType by remember(settingsState.confettiType) {
                    derivedStateOf {
                        Particles.Type.entries.first {
                            it.ordinal == settingsState.confettiType
                        }
                    }
                }

                remember(harmonizer, particlesType, intensity) {
                    Particles(harmonizer = harmonizer).build(
                        type = particlesType,
                        intensity = intensity
                    )
                }
            }
        )
    }

    if (!settingsState.isConfettiEnabled) {
        SideEffect {
            AppToastHost.confettiState.dismiss()
        }
    }
}
