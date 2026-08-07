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

import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.ui.theme.blend
import com.t8rin.imagetoolbox.core.utils.appContext
import nl.dionsegijn.konfetti.core.Angle
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.Spread
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.core.models.Shape
import nl.dionsegijn.konfetti.xml.image.DrawableImage
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt
import kotlin.random.Random


private val Color1 = Color(0xfffce18a)
private val Color2 = Color(0xFF009688)
private val Color3 = Color(0xfff4306d)
private val Color4 = Color(0xffb48def)
private val Color5 = Color(0xFF95FF82)
private val Color6 = Color(0xFF82ECFF)
private val Color7 = Color(0xFFFF9800)
private val Color8 = Color(0xFF0E008A)

private val defaultColors = listOf(
    Color1, Color2, Color3, Color4, Color5, Color6, Color7, Color8
)

private val defaultShapes by lazy {
    listOf(Shape.Square, Shape.Circle, Shape.Rectangle(0.2f))
}

private val colorsByPrimary = mutableMapOf<Color, List<Int>>()

private fun List<Color>.mapToPrimary(primary: Color): List<Int> = colorsByPrimary.getOrPut(primary) {
    map { it.blend(primary.copy(1f), primary.alpha).toArgb() }
}

private data class CacheKey(
    val color: Color,
    val type: Particles.Type,
    val intensity: ConfettiIntensity
)

private val confettiCache = mutableMapOf<CacheKey, List<Party>>()


@Stable
class Particles(
    private val harmonizer: Color
) {
    fun build(
        type: Type,
        intensity: ConfettiIntensity = ConfettiIntensity.Normal
    ): List<Party> {
        val key = CacheKey(harmonizer, type, intensity)
        return confettiCache.getOrPut(key) {
            val base = when (type) {
                Type.Default -> default(harmonizer, intensity)
                Type.Festive -> festiveBottom(harmonizer, intensity)
                Type.Explode -> explode(harmonizer, intensity)
                Type.Rain -> rain(harmonizer, intensity)
                Type.Side -> side(harmonizer, intensity)
                Type.Corners -> festiveCorners(harmonizer, intensity)
                Type.Toolbox -> toolbox(harmonizer, intensity)
                Type.MegaBurst -> megaBurst(harmonizer, intensity)
            }
            base
        }
    }

    companion object {

        private fun festive(
            primary: Color,
            xPos: Double = 0.5,
            yPos: Double = 1.0,
            angle: Int = Angle.TOP,
            duration: Long = 500,
            delay: Int = 0,
            spread: Int = 45,
            intensityMultiplier: Float = 1f
        ): List<Party> {
            val colors = defaultColors.mapToPrimary(primary)
            val base = Party(
                speed = 30f,
                maxSpeed = 50f,
                damping = 0.9f,
                angle = angle,
                spread = spread,
                shapes = defaultShapes,
                delay = delay,
                timeToLive = 3000L,
                colors = colors,
                emitter = Emitter(duration = duration, TimeUnit.MILLISECONDS)
                    .max((30 * intensityMultiplier).roundToInt()),
                position = Position.Relative(xPos, yPos)
            )
            return listOf(
                base,
                base.copy(
                    speed = 55f,
                    maxSpeed = 65f,
                    spread = (spread * 0.22f).roundToInt(),
                    emitter = Emitter(duration = duration, TimeUnit.MILLISECONDS)
                        .max((10 * intensityMultiplier).roundToInt()),
                ),
                base.copy(
                    speed = 50f,
                    maxSpeed = 60f,
                    spread = (spread * 2.67f).roundToInt(),
                    emitter = Emitter(duration = duration, TimeUnit.MILLISECONDS)
                        .max((40 * intensityMultiplier).roundToInt()),
                ),
                base.copy(
                    speed = 65f,
                    maxSpeed = 80f,
                    spread = (spread * 0.22f).roundToInt(),
                    emitter = Emitter(duration = duration, TimeUnit.MILLISECONDS)
                        .max((10 * intensityMultiplier).roundToInt()),
                )
            )
        }

        fun default(
            primary: Color,
            intensity: ConfettiIntensity = ConfettiIntensity.Normal
        ): List<Party> {
            val mul = intensity.particleMultiplier
            val rainPerSec = (110 * mul).roundToInt().coerceAtLeast(40)
            val cannonPerSec = (200 * mul).roundToInt().coerceAtLeast(60)
            val burstPerSec = (260 * mul).roundToInt().coerceAtLeast(80)
            val colors = defaultColors.mapToPrimary(primary)
            return listOf(
                Party(
                    speed = 0f,
                    maxSpeed = 38f,
                    damping = 0.9f,
                    spread = 360,
                    shapes = defaultShapes,
                    timeToLive = 2800,
                    colors = colors,
                    emitter = Emitter(duration = 320, TimeUnit.MILLISECONDS)
                        .perSecond(burstPerSec),
                    position = Position.Relative(0.5, 0.45)
                ),
                Party(
                    speed = 4f,
                    maxSpeed = 18f,
                    damping = 0.9f,
                    angle = Angle.BOTTOM,
                    spread = Spread.ROUND,
                    colors = colors,
                    shapes = defaultShapes,
                    emitter = Emitter(duration = 1500, TimeUnit.MILLISECONDS)
                        .perSecond(rainPerSec),
                    position = Position.Relative(0.0, 0.0).between(Position.Relative(1.0, 0.0))
                ),
                Party(
                    speed = 28f,
                    maxSpeed = 55f,
                    damping = 0.88f,
                    angle = Angle.RIGHT - 50,
                    spread = 45,
                    colors = colors,
                    shapes = defaultShapes,
                    emitter = Emitter(duration = 380, TimeUnit.MILLISECONDS)
                        .perSecond(cannonPerSec),
                    position = Position.Relative(0.0, 1.0)
                ),
                Party(
                    speed = 28f,
                    maxSpeed = 55f,
                    damping = 0.88f,
                    angle = Angle.RIGHT - 130,
                    spread = 45,
                    colors = colors,
                    shapes = defaultShapes,
                    emitter = Emitter(duration = 380, TimeUnit.MILLISECONDS)
                        .perSecond(cannonPerSec),
                    position = Position.Relative(1.0, 1.0)
                ),
                Party(
                    speed = 22f,
                    maxSpeed = 48f,
                    damping = 0.88f,
                    angle = Angle.TOP,
                    spread = 50,
                    colors = colors,
                    shapes = defaultShapes,
                    emitter = Emitter(duration = 260, TimeUnit.MILLISECONDS)
                        .perSecond((cannonPerSec * 0.6f).roundToInt()),
                    position = Position.Relative(0.5, 1.0)
                )
            )
        }

        fun festiveBottom(
            primary: Color,
            intensity: ConfettiIntensity = ConfettiIntensity.Normal
        ): List<Party> = festive(primary, 0.2, intensityMultiplier = intensity.particleMultiplier)
            .plus(festive(primary, 0.8, intensityMultiplier = intensity.particleMultiplier))

        fun explode(
            primary: Color,
            intensity: ConfettiIntensity = ConfettiIntensity.Normal,
            shape: Shape? = null,
            initialDelay: Int = 0
        ): List<Party> {
            val mul = intensity.particleMultiplier
            val maxPerEmitter = (100 * mul).roundToInt().coerceAtLeast(20)
            val colors = defaultColors.mapToPrimary(primary)
            val (x1, y1) = Random.nextDouble(0.0, 0.3) to Random.nextDouble(0.0, 0.5)
            val (x2, y2) = Random.nextDouble(0.0, 0.3) to Random.nextDouble(0.5, 1.0)
            val (x3, y3) = Random.nextDouble(0.3, 0.7) to Random.nextDouble(0.0, 1.0)
            val (x4, y4) = Random.nextDouble(0.7, 1.0) to Random.nextDouble(0.0, 0.5)
            val (x5, y5) = Random.nextDouble(0.7, 1.0) to Random.nextDouble(0.5, 1.0)
            val base = Party(
                speed = 0f,
                maxSpeed = 30f,
                damping = 0.9f,
                spread = 360,
                shapes = shape?.let { listOf(it) } ?: defaultShapes,
                timeToLive = 3000,
                colors = colors,
                emitter = Emitter(duration = 200, TimeUnit.MILLISECONDS).max(maxPerEmitter)
            )
            return listOf(
                base.copy(position = Position.Relative(x1, y1), delay = initialDelay),
                base.copy(position = Position.Relative(x2, y2), delay = initialDelay + 200),
                base.copy(position = Position.Relative(x3, y3), delay = initialDelay + 400),
                base.copy(position = Position.Relative(x4, y4), delay = initialDelay + 600),
                base.copy(position = Position.Relative(x5, y5), delay = initialDelay + 800)
            )
        }

        fun rain(
            primary: Color,
            intensity: ConfettiIntensity = ConfettiIntensity.Normal
        ): List<Party> {
            val perSecond = (100 * intensity.particleMultiplier).roundToInt().coerceAtLeast(1)
            val colors = defaultColors.mapToPrimary(primary)
            val base = Party(
                speed = 10f,
                maxSpeed = 30f,
                damping = 0.9f,
                shapes = defaultShapes,
                colors = colors,
                emitter = Emitter(duration = 2, TimeUnit.SECONDS).perSecond(perSecond),
            )
            return listOf(
                base.copy(
                    angle = 45,
                    position = Position.Relative(0.0, 0.0),
                    spread = 90,
                ),
                base.copy(
                    angle = 90,
                    position = Position.Relative(0.5, 0.0),
                    spread = 360,
                ),
                base.copy(
                    angle = 135,
                    position = Position.Relative(1.0, 0.0),
                    spread = 90,
                )
            )
        }

        fun side(
            primary: Color,
            intensity: ConfettiIntensity = ConfettiIntensity.Normal
        ): List<Party> {
            val mul = intensity.particleMultiplier
            return listOf(
                festive(primary, 0.0, 0.0, Angle.RIGHT, 1000, intensityMultiplier = mul),
                festive(primary, 1.0, 0.33, Angle.LEFT, 1000, 150, intensityMultiplier = mul),
                festive(primary, 0.0, 0.66, Angle.RIGHT, 1000, 300, intensityMultiplier = mul),
                festive(primary, 1.0, 1.0, Angle.LEFT, 1000, 450, intensityMultiplier = mul),
            ).flatten()
        }

        fun festiveCorners(
            primary: Color,
            intensity: ConfettiIntensity = ConfettiIntensity.Normal
        ): List<Party> {
            val mul = intensity.particleMultiplier
            return listOf(
                festive(primary, 0.0, 0.0, 45, 1000, 0, 25, mul),
                festive(primary, 1.0, 0.0, 135, 1000, 150, 25, mul),
                festive(primary, 0.0, 1.0, -45, 1000, 300, 25, mul),
                festive(primary, 1.0, 1.0, 225, 1000, 450, 25, mul),
            ).flatten()
        }

        fun toolbox(
            primary: Color,
            intensity: ConfettiIntensity = ConfettiIntensity.Normal
        ): List<Party> = Shape.DrawableShape(
            AppCompatResources.getDrawable(
                appContext,
                R.drawable.ic_launcher_monochrome_24
            )!!.let {
                DrawableImage(
                    drawable = it,
                    width = it.intrinsicWidth,
                    height = it.intrinsicHeight
                )
            }
        ).let { shape ->
            val delay = 400
            explode(primary, intensity, shape) + explode(primary, intensity, shape, delay)
        }

        fun megaBurst(
            primary: Color,
            intensity: ConfettiIntensity = ConfettiIntensity.Normal
        ): List<Party> {
            val mul = (intensity.particleMultiplier * 1.4f).coerceAtLeast(1f)
            val colors = defaultColors.mapToPrimary(primary)
            val perSecond = (160 * mul).roundToInt().coerceAtLeast(40)
            return listOf(
                Party(
                    speed = 0f,
                    maxSpeed = 40f,
                    damping = 0.9f,
                    spread = 360,
                    shapes = defaultShapes,
                    timeToLive = 3500,
                    colors = colors,
                    emitter = Emitter(duration = 700, TimeUnit.MILLISECONDS)
                        .perSecond(perSecond),
                    position = Position.Relative(0.5, 0.45)
                ),
                Party(
                    speed = 20f,
                    maxSpeed = 60f,
                    damping = 0.88f,
                    angle = Angle.BOTTOM,
                    spread = Spread.ROUND,
                    shapes = defaultShapes,
                    colors = colors,
                    emitter = Emitter(duration = 600, TimeUnit.MILLISECONDS)
                        .perSecond((perSecond * 0.6f).roundToInt()),
                    position = Position.Relative(0.0, 1.0).between(Position.Relative(1.0, 1.0))
                ),
                Party(
                    speed = 20f,
                    maxSpeed = 60f,
                    damping = 0.88f,
                    angle = Angle.TOP,
                    spread = Spread.ROUND,
                    shapes = defaultShapes,
                    colors = colors,
                    emitter = Emitter(duration = 600, TimeUnit.MILLISECONDS)
                        .perSecond((perSecond * 0.4f).roundToInt()),
                    position = Position.Relative(0.0, 0.0).between(Position.Relative(1.0, 0.0))
                )
            )
        }

    }

    enum class Type {
        Default, Festive, Explode, Rain, Side, Corners, Toolbox, MegaBurst
    }
}
