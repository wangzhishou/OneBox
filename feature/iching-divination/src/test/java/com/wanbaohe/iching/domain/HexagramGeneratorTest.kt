package com.wanbaohe.iching.domain

import com.wanbaohe.iching.model.HexagramLine
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.Test

class HexagramGeneratorTest {
    private val generator = HexagramGenerator()

    @Test
    fun `all yang resolves to hexagram one`() {
        val result = generator.create("test", List(6) { HexagramLine(7) })
        assertEquals(1, result.primary.number)
        assertEquals("乾为天", result.primary.name)
        assertNull(result.changed)
    }

    @Test
    fun `all yin resolves to hexagram two`() {
        val result = generator.create("test", List(6) { HexagramLine(8) })
        assertEquals(2, result.primary.number)
        assertEquals("坤为地", result.primary.name)
    }

    @Test
    fun `wind over heaven resolves to small taming`() {
        val result = generator.create("test", listOf(7, 7, 7, 8, 7, 7).map(::HexagramLine))
        assertEquals(9, result.primary.number)
        assertEquals("风天小畜", result.primary.name)
        assertEquals("巽（风）", result.primary.upperTrigram)
        assertEquals("乾（天）", result.primary.lowerTrigram)
    }

    @Test
    fun `water over thunder resolves to difficulty at beginning`() {
        val result = generator.create("test", listOf(7, 8, 8, 8, 7, 8).map(::HexagramLine))
        assertEquals(3, result.primary.number)
        assertEquals("水雷屯", result.primary.name)
    }

    @Test
    fun `old yin and old yang produce changed hexagram`() {
        val result = generator.create(" test ", List(6) { HexagramLine(9) })
        assertEquals("test", result.question)
        assertEquals((1..6).toList(), result.changingLineNumbers)
        assertNotNull(result.changed)
        assertEquals(2, result.changed.number)
    }

    @Test
    fun `mapping contains every King Wen hexagram exactly once`() {
        val resolved = buildSet {
            for (bits in 0 until 64) {
                val lines = List(6) { index -> bits and (1 shl index) != 0 }
                add(generator.resolve(lines).number)
            }
        }
        assertEquals((1..64).toSet(), resolved)
    }

    @Test
    fun `coin toss only creates valid line values`() {
        val random = Random(42)
        val values = List(1_000) { generator.tossLine(random).value }
        assertTrue(values.all { it in 6..9 })
        assertEquals(setOf(6, 7, 8, 9), values.toSet())
    }
}


