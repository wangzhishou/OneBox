package com.wanbaohe.iching.domain

import com.wanbaohe.iching.model.DivinationResult
import com.wanbaohe.iching.model.HexagramInfo
import com.wanbaohe.iching.model.HexagramLine
import javax.inject.Inject
import kotlin.random.Random

class HexagramGenerator @Inject constructor() {

    fun tossLine(random: Random = Random.Default): HexagramLine {
        val value = List(3) { if (random.nextBoolean()) 3 else 2 }.sum()
        return HexagramLine(value)
    }

    fun create(question: String, lines: List<HexagramLine>): DivinationResult {
        require(lines.size == 6) { "A hexagram must contain exactly six lines" }
        val primary = resolve(lines.map(HexagramLine::isYang))
        val changed = if (lines.any(HexagramLine::isChanging)) {
            resolve(lines.map(HexagramLine::changedIsYang))
        } else null
        return DivinationResult(question.trim(), lines, primary, changed)
    }

    internal fun resolve(lines: List<Boolean>): HexagramInfo {
        require(lines.size == 6)
        val lower = trigramCode(lines.subList(0, 3))
        val upper = trigramCode(lines.subList(3, 6))
        val entry = HEXAGRAMS.getValue(upper to lower)
        return HexagramInfo(entry.first, entry.second, TRIGRAM_NAMES.getValue(upper), TRIGRAM_NAMES.getValue(lower))
    }

    private fun trigramCode(lines: List<Boolean>): Int =
        lines.foldIndexed(0) { index, result, yang ->
            result or ((if (yang) 1 else 0) shl (2 - index))
        }

    private companion object {
        // Binary codes follow the mapping table: bottom line is the most significant bit.
        val TRIGRAM_NAMES = mapOf(7 to "乾（天）", 0 to "坤（地）", 2 to "坎（水）", 5 to "离（火）", 4 to "震（雷）", 1 to "艮（山）", 3 to "巽（风）", 6 to "兑（泽）")

        val KING_WEN_KEYS = listOf(
            7 to 7, 0 to 0, 2 to 4, 1 to 2, 2 to 7, 7 to 2, 0 to 2, 2 to 0,
            3 to 7, 7 to 6, 0 to 7, 7 to 0, 7 to 5, 5 to 7, 0 to 1, 4 to 0,
            6 to 4, 1 to 3, 0 to 6, 3 to 0, 5 to 4, 1 to 5, 1 to 0, 0 to 4,
            7 to 4, 1 to 7, 1 to 4, 6 to 3, 2 to 2, 5 to 5, 6 to 1, 4 to 3,
            7 to 1, 4 to 7, 5 to 0, 0 to 5, 3 to 5, 5 to 6, 2 to 1, 4 to 2,
            1 to 6, 3 to 4, 6 to 7, 7 to 3, 6 to 0, 0 to 3, 6 to 2, 2 to 3,
            6 to 5, 5 to 3, 4 to 4, 1 to 1, 3 to 1, 4 to 6, 4 to 5, 5 to 1,
            3 to 3, 6 to 6, 3 to 2, 2 to 6, 3 to 6, 4 to 1, 2 to 5, 5 to 2,
        )

        // King Wen sequence, keyed by upper then lower trigram.
        val HEXAGRAMS = listOf(
            1 to "乾为天", 2 to "坤为地", 3 to "水雷屯", 4 to "山水蒙", 5 to "水天需", 6 to "天水讼", 7 to "地水师", 8 to "水地比",
            9 to "风天小畜", 10 to "天泽履", 11 to "地天泰", 12 to "天地否", 13 to "天火同人", 14 to "火天大有", 15 to "地山谦", 16 to "雷地豫",
            17 to "泽雷随", 18 to "山风蛊", 19 to "地泽临", 20 to "风地观", 21 to "火雷噬嗑", 22 to "山火贲", 23 to "山地剥", 24 to "地雷复",
            25 to "天雷无妄", 26 to "山天大畜", 27 to "山雷颐", 28 to "泽风大过", 29 to "坎为水", 30 to "离为火", 31 to "泽山咸", 32 to "雷风恒",
            33 to "天山遁", 34 to "雷天大壮", 35 to "火地晋", 36 to "地火明夷", 37 to "风火家人", 38 to "火泽睽", 39 to "水山蹇", 40 to "雷水解",
            41 to "山泽损", 42 to "风雷益", 43 to "泽天夬", 44 to "天风姤", 45 to "泽地萃", 46 to "地风升", 47 to "泽水困", 48 to "水风井",
            49 to "泽火革", 50 to "火风鼎", 51 to "震为雷", 52 to "艮为山", 53 to "风山渐", 54 to "雷泽归妹", 55 to "雷火丰", 56 to "火山旅",
            57 to "巽为风", 58 to "兑为泽", 59 to "风水涣", 60 to "水泽节", 61 to "风泽中孚", 62 to "雷山小过", 63 to "水火既济", 64 to "火水未济",
        ).associateBy({ numberAndName ->
            // Canonical upper/lower trigram pairs in King Wen order.
            KING_WEN_KEYS[numberAndName.first - 1]
        }, { it })

    }
}


