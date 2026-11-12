/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
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

package com.wanbaohe.markuplayers.data.render

import androidx.compose.ui.graphics.BlendMode
import com.wanbaohe.markuplayers.domain.model.LayerBlendMode
import com.wanbaohe.markuplayers.presentation.render.toComposeBlendMode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class LayerPixelMathTest {

    @Test
    fun strengthInterpolatesAlphaRatherThanDrawingOverTheOriginal() {
        val red = 0xFFFF0000.toInt()
        assertEquals(0x80FF0000.toInt(), interpolateEffectPixel(red, 0, 0.5f))
        assertEquals(red, interpolateEffectPixel(red, 0, 0f))
        assertEquals(0, interpolateEffectPixel(red, 0, 1f))
    }

    @Test
    fun strengthInterpolatesPremultipliedColors() {
        assertEquals(
            0xC0AA0055.toInt(),
            interpolateEffectPixel(0x800000FF.toInt(), 0xFFFF0000.toInt(), 0.5f)
        )
    }

    @Test
    fun maskOnlyMultipliesContentAlphaAndPreservesStraightRgb() {
        val pixel = 0x80ABCDEE.toInt()
        assertEquals(pixel, applyMaskPixel(pixel, 255))
        assertEquals(0x40ABCDEE, applyMaskPixel(pixel, 128))
        assertEquals(0, applyMaskPixel(pixel, 0))
    }

    @Test
    fun multiplyRetainsBackdropAlphaAndDoesNotModulateRgba() {
        assertEquals(
            0xFF804040.toInt(),
            blendLayerPixel(0xFF808080.toInt(), 0x80FF0000.toInt(), LayerBlendMode.Multiply)
        )
        assertEquals(
            0x80FF0000.toInt(),
            blendLayerPixel(0, 0x80FF0000.toInt(), LayerBlendMode.Multiply)
        )
    }

    @Test
    fun transparentSourceNeverErasesTheBackdropInAnyBlendMode() {
        LayerBlendMode.entries.forEach {
            assertEquals(0x806080A0.toInt(), blendLayerPixel(0x806080A0.toInt(), 0, it))
        }
    }

    @Test
    fun blendModesMatchTheirSeparableColorDefinitions() {
        val destination = 0xFF808080.toInt()
        val source = 0xFFC0C0C0.toInt()
        val expected = mapOf(
            LayerBlendMode.Normal to 192,
            LayerBlendMode.Multiply to 96,
            LayerBlendMode.Screen to 224,
            LayerBlendMode.Overlay to 192,
            LayerBlendMode.Darken to 128,
            LayerBlendMode.Lighten to 192
        )
        expected.forEach { (mode, value) ->
            assertEquals(
                0xFF000000.toInt() or (value shl 16) or (value shl 8) or value,
                blendLayerPixel(destination, source, mode)
            )
        }
    }

    @Test
    fun composeMappingsUseTheSameColorBlendModes() {
        assertEquals(BlendMode.SrcOver, LayerBlendMode.Normal.toComposeBlendMode())
        assertEquals(BlendMode.Multiply, LayerBlendMode.Multiply.toComposeBlendMode())
        assertEquals(BlendMode.Screen, LayerBlendMode.Screen.toComposeBlendMode())
        assertEquals(BlendMode.Overlay, LayerBlendMode.Overlay.toComposeBlendMode())
        assertEquals(BlendMode.Darken, LayerBlendMode.Darken.toComposeBlendMode())
        assertEquals(BlendMode.Lighten, LayerBlendMode.Lighten.toComposeBlendMode())
    }

    @Test
    fun featherPreservesFullAndEmptySelectionsIncludingTheirEdges() = runBlocking {
        val full = ByteArray(25) { 255.toByte() }
        val empty = ByteArray(25)
        featherLayerMaskAlpha(full, 5, 5, 1)
        featherLayerMaskAlpha(empty, 5, 5, 1)
        assertTrue(full.all { (it.toInt() and 255) == 255 })
        assertTrue(empty.all { it == 0.toByte() })
    }

    @Test
    fun featherProducesBoundedSymmetricPixelCoverage() = runBlocking {
        val alpha = ByteArray(25).apply { this[12] = 255.toByte() }
        featherLayerMaskAlpha(alpha, 5, 5, 1)
        assertTrue((alpha[12].toInt() and 255) in 1..254)
        assertTrue((alpha[11].toInt() and 255) > 0)
        assertEquals(alpha[11], alpha[13])
        assertEquals(alpha[7], alpha[17])
        assertEquals(alpha[11], alpha[7])
    }

    @Test
    fun featherRejectsUnboundedRadiiAndPreservesCancellation() {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { featherLayerMaskAlpha(ByteArray(25), 5, 5, 2) }
        }
        assertThrows(CancellationException::class.java) {
            runBlocking {
                val cancelled = Job().apply { cancel() }
                withContext(cancelled) { featherLayerMaskAlpha(ByteArray(25), 5, 5, 1) }
            }
        }
    }
}
