package com.wanbaohe.calendar.data

/**
 * 八字计算工具
 *
 * 提供四柱八字（年柱、月柱、日柱、时柱）以及五行分布、大运等计算。
 * 简化版实现，覆盖核心逻辑。
 */
object BaZiCalculator {

    private val TIAN_GAN = arrayOf("甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸")
    private val DI_ZHI = arrayOf("子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥")

    /** 天干五行 */
    private val GAN_WU_XING = mapOf(
        "甲" to "木", "乙" to "木",
        "丙" to "火", "丁" to "火",
        "戊" to "土", "己" to "土",
        "庚" to "金", "辛" to "金",
        "壬" to "水", "癸" to "水"
    )

    /** 地支五行 */
    private val ZHI_WU_XING = mapOf(
        "子" to "水", "丑" to "土", "寅" to "木", "卯" to "木",
        "辰" to "土", "巳" to "火", "午" to "火", "未" to "土",
        "申" to "金", "酉" to "金", "戌" to "土", "亥" to "水"
    )

    /** 十神名称（根据日干与其他天干的关系） */
    private val SHI_SHEN_NAMES = arrayOf(
        "比肩", "劫财", "食神", "伤官", "偏财",
        "正财", "七杀", "正官", "偏印", "正印"
    )

    /**
     * 计算完整八字数据
     * @param hour 出生时辰 (0-23)
     */
    fun calculateBaZi(
        solarYear: Int,
        solarMonth: Int,
        solarDay: Int,
        hour: Int = 12
    ): BaZiData {
        val yearPillar = getYearPillar(solarYear, solarMonth)
        val monthPillar = getMonthPillar(solarYear, solarMonth)
        val dayPillar = getDayPillar(solarYear, solarMonth, solarDay)
        val hourPillar = getHourPillar(dayPillar.tianGan, hour)

        val dayMaster = dayPillar.tianGan
        val dayMasterElement = GAN_WU_XING[dayMaster] ?: "水"

        // 五行分布
        val wuXingDist = calculateWuXingDistribution(yearPillar, monthPillar, dayPillar, hourPillar)

        // 身强身弱简化判断
        val selfElement = dayMasterElement
        val selfRatio = (wuXingDist[selfElement] ?: 0f)
        val supportElement = getGeneratingElement(selfElement)
        val supportRatio = (wuXingDist[supportElement] ?: 0f)
        val strength = if (selfRatio + supportRatio >= 40f) "身强" else "身弱"

        // 喜用神
        val favorable = if (strength == "身强") {
            "喜${getControlledElement(selfElement)}${getControllingElement(selfElement)}"
        } else {
            "喜${selfElement}${supportElement}"
        }

        // 十神
        val yearShiShen = getShiShen(dayMaster, yearPillar.tianGan)
        val monthShiShen = getShiShen(dayMaster, monthPillar.tianGan)
        val hourShiShen = getShiShen(dayMaster, hourPillar.tianGan)

        return BaZiData(
            yearPillar = yearPillar.copy(shiShen = yearShiShen, pillarName = "年柱"),
            monthPillar = monthPillar.copy(shiShen = monthShiShen, pillarName = "月柱"),
            dayPillar = dayPillar.copy(shiShen = "日主", pillarName = "日柱"),
            hourPillar = hourPillar.copy(shiShen = hourShiShen, pillarName = "时柱"),
            dayMaster = dayMaster,
            wuXingDistribution = wuXingDist,
            strength = strength,
            favorableElements = favorable
        )
    }

    /** 年柱：立春为界 */
    private fun getYearPillar(year: Int, month: Int): BaZiPillar {
        // 简化：2月4日前用上一年
        val adjustedYear = if (month <= 1 || (month == 2 && true)) year - 1 else year
        val y = if (month < 2) year - 1 else year
        val ganIdx = (y - 4) % 10
        val zhiIdx = (y - 4) % 12
        return BaZiPillar(
            tianGan = TIAN_GAN[(ganIdx + 10) % 10],
            diZhi = DI_ZHI[(zhiIdx + 12) % 12]
        )
    }

    /** 月柱 */
    private fun getMonthPillar(year: Int, month: Int): BaZiPillar {
        val yearGanIdx = (year - 4) % 10
        // 正月干 = (年干序号 × 2 + 2) % 10，正月支固定为寅
        val monthOffset = month - 1 // 0=正月
        val ganIdx = (yearGanIdx * 2 + 2 + monthOffset) % 10
        val zhiIdx = (month + 1) % 12
        return BaZiPillar(
            tianGan = TIAN_GAN[(ganIdx + 10) % 10],
            diZhi = DI_ZHI[(zhiIdx + 12) % 12]
        )
    }

    /** 日柱 */
    private fun getDayPillar(year: Int, month: Int, day: Int): BaZiPillar {
        val ganZhi = LunarCalendarCalculator.getGanZhiDay(year, month, day)
        return BaZiPillar(
            tianGan = ganZhi.substring(0, 1),
            diZhi = ganZhi.substring(1, 2)
        )
    }

    /** 时柱 */
    private fun getHourPillar(dayGan: String, hour: Int): BaZiPillar {
        val zhiIdx = ((hour + 1) / 2) % 12
        val dayGanIdx = TIAN_GAN.indexOf(dayGan)
        val ganIdx = (dayGanIdx * 2 + zhiIdx) % 10
        return BaZiPillar(
            tianGan = TIAN_GAN[ganIdx],
            diZhi = DI_ZHI[zhiIdx]
        )
    }

    /** 计算十神 */
    private fun getShiShen(dayGan: String, otherGan: String): String {
        val dayIdx = TIAN_GAN.indexOf(dayGan)
        val otherIdx = TIAN_GAN.indexOf(otherGan)
        if (dayIdx < 0 || otherIdx < 0) return ""
        val diff = ((otherIdx - dayIdx) + 10) % 10
        return SHI_SHEN_NAMES[diff]
    }

    /** 计算五行分布 */
    private fun calculateWuXingDistribution(
        year: BaZiPillar, month: BaZiPillar,
        day: BaZiPillar, hour: BaZiPillar
    ): Map<String, Float> {
        val elements = mutableMapOf("金" to 0f, "木" to 0f, "水" to 0f, "火" to 0f, "土" to 0f)
        val pillars = listOf(year, month, day, hour)
        val weight = 12.5f // 每个干/支占 12.5%，共8个=100%

        for (p in pillars) {
            val ganElement = GAN_WU_XING[p.tianGan] ?: continue
            elements[ganElement] = (elements[ganElement] ?: 0f) + weight
            val zhiElement = ZHI_WU_XING[p.diZhi] ?: continue
            elements[zhiElement] = (elements[zhiElement] ?: 0f) + weight
        }
        return elements
    }

    /** 生我者 */
    private fun getGeneratingElement(element: String): String = when (element) {
        "金" -> "土"; "木" -> "水"; "水" -> "金"; "火" -> "木"; "土" -> "火"; else -> "水"
    }

    /** 我克者 */
    private fun getControlledElement(element: String): String = when (element) {
        "金" -> "木"; "木" -> "土"; "水" -> "火"; "火" -> "金"; "土" -> "水"; else -> "土"
    }

    /** 克我者 */
    private fun getControllingElement(element: String): String = when (element) {
        "金" -> "火"; "木" -> "金"; "水" -> "土"; "火" -> "水"; "土" -> "木"; else -> "金"
    }

    /**
     * 生成大运数据
     * 简化：每10年一步大运
     */
    fun getDaYun(birthYear: Int, birthMonth: Int): List<DaYunItem> {
        val result = mutableListOf<DaYunItem>()
        val yearGanIdx = (birthYear - 4) % 10
        val startGanIdx = (yearGanIdx * 2 + 2 + birthMonth) % 10
        val startZhiIdx = (birthMonth + 2) % 12

        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)

        for (i in 0..7) {
            val startYear = birthYear + 4 + i * 10
            val ganIdx = (startGanIdx + i) % 10
            val zhiIdx = (startZhiIdx + i) % 12
            result.add(
                DaYunItem(
                    startYear = startYear,
                    ganZhi = TIAN_GAN[ganIdx] + DI_ZHI[zhiIdx],
                    isCurrent = currentYear in startYear until (startYear + 10)
                )
            )
        }
        return result
    }

    /**
     * 生成流年详批
     */
    fun getFortuneData(birthYear: Int, birthMonth: Int, targetYear: Int): FortuneData {
        val ganZhiYear = LunarCalendarCalculator.getGanZhiYear(targetYear)
        val yearGan = ganZhiYear.substring(0, 1)
        val yearZhi = ganZhiYear.substring(1, 2)
        val zodiac = LunarCalendarCalculator.getZodiac(targetYear)

        // 简化运势生成
        val seed = (birthYear * 100 + birthMonth) xor targetYear
        val score = 60 + (seed % 35)
        val careerLevel = if (score > 80) "TOP" else if (score > 60) "GOOD" else "FAIR"

        val titles = listOf(
            "岁运并临·潜龙出海", "否极泰来·柳暗花明",
            "乘风破浪·鹏程万里", "稳中求进·厚积薄发"
        )
        val descriptions = listOf(
            "本年${yearGan}木遇干，水生木旺，才华得以施展。需注意辰辰自刑，情绪波动较大，凡事三思而后行。",
            "流年运势稳健，事业有贵人相助，财运亨通。注意健康方面需多加留意。",
            "本年运势向好，学业事业皆有进展，人际关系和谐。宜主动出击，把握良机。",
            "本年宜守不宜攻，稳扎稳打方为上策。下半年运势回升，可适当拓展。"
        )

        val monthlyFortunes = (2..4).map { m ->
            val mGanIdx = ((targetYear - 4) % 10 * 2 + m) % 10
            val mZhiIdx = (m + 1) % 12
            MonthlyFortune(
                ganZhi = "${TIAN_GAN[mGanIdx]}${DI_ZHI[mZhiIdx]}",
                monthDisplay = "${m}月",
                keyword = listOf("伤官见官", "七杀当令", "正财合身", "偏印夺食")[(m - 2) % 4],
                tag = listOf("忌争执", "利开拓", "财运亨通", "宜学习")[(m - 2) % 4],
                tagType = listOf("negative", "positive", "positive", "neutral")[(m - 2) % 4]
            )
        }

        return FortuneData(
            year = targetYear,
            ganZhiYear = ganZhiYear,
            title = titles[seed % titles.size],
            description = descriptions[seed % descriptions.size],
            fortuneScore = score,
            careerLevel = careerLevel,
            monthlyFortunes = monthlyFortunes
        )
    }
}

