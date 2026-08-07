package com.wanbaohe.calendar.data

/**
 * lunar-java 反射桥接层。
 *
 * 使用反射是为了降低 API 版本差异带来的编译耦合，
 * 当库不可用或方法签名变化时自动回退到本地算法。
 */
object LunarJavaBridge {

    private const val SOLAR_CLASS = "com.nlf.calendar.Solar"
    private const val LUNAR_CLASS = "com.nlf.calendar.Lunar"
    private const val FOTO_CLASS = "com.nlf.calendar.Foto"
    private const val TAO_CLASS = "com.nlf.calendar.Tao"
    private const val HOLIDAY_UTIL_CLASS = "com.nlf.calendar.util.HolidayUtil"

    fun solarToLunarDate(year: Int, month: Int, day: Int): LunarDate? {
        val solar = createSolar(year, month, day) ?: return null
        val lunar = callAny(solar, "getLunar") ?: return null
        val isLeapMonth = callBoolean(lunar, "isLeap")

        val lunarYear = callInt(lunar, "getYear")
        val lunarMonth = callInt(lunar, "getMonth")
        val lunarDay = callInt(lunar, "getDay")

        if (lunarYear <= 0 || lunarMonth == 0 || lunarDay <= 0) return null

        val lunarFestivals = callStringList(lunar, "getFestivals")
        val solarFestivals = callStringList(solar, "getFestivals")
        val holidayInfo = getHolidayInfo(year, month, day)


        return LunarDate(
            year = lunarYear,
            month = kotlin.math.abs(lunarMonth),
            day = lunarDay,
            isLeapMonth = isLeapMonth,
            ganZhiYear = callString(lunar, "getYearInGanZhi"),
            ganZhiMonth = callString(lunar, "getMonthInGanZhi"),
            ganZhiDay = callString(lunar, "getDayInGanZhi"),
            zodiac = callString(lunar, "getYearShengXiao"),
            monthName = LunarCalendarCalculator.normalizeLunarMonthName(
                monthName = callString(lunar, "getMonthInChinese"),
                isLeapMonth = isLeapMonth
            ),
            dayName = callString(lunar, "getDayInChinese"),
            solarTerm = callStringOrNull(lunar, "getJieQi"),
            lunarFestival = lunarFestivals.firstOrNull(),
            lunarFestivals = lunarFestivals,
            solarFestival = solarFestivals.firstOrNull(),
            solarFestivals = solarFestivals,
            legalHolidayName = holidayInfo?.name,
            isLegalHoliday = holidayInfo?.isRestDay == true,
            constellation = callString(solar, "getXingZuo"),
            star28 = callString(lunar, "getXiu"),
            jianChu = callString(lunar, "getZhiXing"),
            naYin = callFirstString(
                lunar,
                "getDayNaYin",
                "getNaYin",
                "getDayInNaYin"
            ),
            pengZuGan = callString(lunar, "getPengZuGan"),
            pengZuZhi = callString(lunar, "getPengZuZhi"),
            xiShen = callString(lunar, "getPositionXiDesc"),
            fuShen = callString(lunar, "getPositionFuDesc"),
            caiShen = callString(lunar, "getPositionCaiDesc"),
            chong = callString(lunar, "getChongDesc"),
            sha = callString(lunar, "getSha"),
            zhiShen = callFirstString(
                lunar,
                "getDayTianShen",
                "getZhiShen",
                "getDayZhiShen"
            ),
            dayJiShen = joinNotEmpty(callStringList(lunar, "getDayJiShen")),
            dayXiongSha = joinNotEmpty(callStringList(lunar, "getDayXiongSha")),
            taiShen = callFirstString(lunar, "getDayPositionTai", "getDayPositionTaiDesc"),
            nineStar = callString(callAny(lunar, "getDayNineStar") ?: "", "toString")
        )
    }

    private fun getHolidayInfo(year: Int, month: Int, day: Int): HolidayInfo? {
        val holiday = runCatching {
            val cls = Class.forName(HOLIDAY_UTIL_CLASS)
            cls.getMethod(
                "getHoliday",
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType
            ).invoke(null, year, month, day)
        }.getOrNull() ?: return null

        val name = callFirstString(holiday, "getName", "toString").trim()
        val isWork = callBooleanOrNull(holiday, "isWork")
            ?: callBooleanOrNull(holiday, "getWork")
            ?: false

        // lunar-java 的 Holiday 中 isWork=true 通常代表调休上班，false 代表休假
        return HolidayInfo(
            name = name.takeIf { it.isNotBlank() },
            isRestDay = !isWork
        )
    }

    fun getYiJi(year: Int, month: Int, day: Int): YiJi? {
        val solar = createSolar(year, month, day) ?: return null
        val lunar = callAny(solar, "getLunar") ?: return null
        val yi = callStringList(lunar, "getDayYi")
        val ji = callStringList(lunar, "getDayJi")
        if (yi.isEmpty() && ji.isEmpty()) return null
        return YiJi(yi = yi, ji = ji)
    }

    fun getTimeSlots(year: Int, month: Int, day: Int): List<LunarTimeSlot> {
        val solar = createSolar(year, month, day) ?: return emptyList()
        val lunar = callAny(solar, "getLunar") ?: return emptyList()
        val times = callObjectList(lunar, "getTimes")
        if (times.isEmpty()) return emptyList()

        return times.map { time ->
            val minHm = callFirstString(time, "getMinHm")
            val maxHm = callFirstString(time, "getMaxHm")
            LunarTimeSlot(
                ganZhi = callFirstString(time, "getGanZhi", "getZhi"),
                timeRange = listOf(minHm, maxHm)
                    .filter { it.isNotBlank() }
                    .joinToString("-")
                    .ifBlank { "--:--" },
                tianShen = callFirstString(time, "getTianShen", "getTianShenDesc"),
                chong = callFirstString(time, "getChongDesc", "getChong"),
                sha = callFirstString(time, "getSha"),
                tianShenType = callFirstString(time, "getTianShenType", "getTianShenTypeDesc"),
                luck = callFirstString(time, "getTianShenLuck", "getTianShenLuckDesc"),
                yi = callStringList(time, "getYi"),
                ji = callStringList(time, "getJi")
            )
        }
    }

    fun lunarToSolarDate(year: Int, month: Int, day: Int, isLeapMonth: Boolean = false): SolarDate? {
        val lunar = createLunar(year, month, day, isLeapMonth) ?: return null
        val solar = callAny(lunar, "getSolar") ?: return null

        val solarYear = callInt(solar, "getYear")
        val solarMonth = callInt(solar, "getMonth")
        val solarDay = callInt(solar, "getDay")

        if (solarYear <= 0 || solarMonth <= 0 || solarDay <= 0) return null

        return SolarDate(
            year = solarYear,
            month = solarMonth,
            day = solarDay,
            weekDay = callString(solar, "getWeekInChinese")
        )
    }

    /**
     * 获取佛历数据（通过反射调用 Foto）
     */
    fun getFotoData(year: Int, month: Int, day: Int): FotoData? {
        val solar = createSolar(year, month, day) ?: return null
        val lunar = callAny(solar, "getLunar") ?: return null
        val foto = createFotoFromLunar(lunar) ?: return null

        val fotoYear = callInt(foto, "getYear")
        if (fotoYear <= 0) return null

        // 解析因果犯忌列表
        val rawFestivals = callObjectList(foto, "getFestivals")
        val festivals = rawFestivals.map { f ->
            FotoFestivalItem(
                name = callString(f, "getName"),
                result = callString(f, "getResult"),
                remark = callString(f, "getRemark")
            )
        }

        // 解析纪念日列表
        val otherFestivals = callStringList(foto, "getOtherFestivals")

        return FotoData(
            year = fotoYear,
            yearInChinese = callString(foto, "getYearInChinese"),
            monthInChinese = callString(foto, "getMonthInChinese"),
            dayInChinese = callString(foto, "getDayInChinese"),
            festivals = festivals,
            otherFestivals = otherFestivals,
            isMonthZhai = callBoolean(foto, "isMonthZhai"),
            isDayYangGong = callBoolean(foto, "isDayYangGong"),
            isDayZhaiShuoWang = callBoolean(foto, "isDayZhaiShuoWang"),
            isDayZhaiSix = callBoolean(foto, "isDayZhaiSix"),
            isDayZhaiTen = callBoolean(foto, "isDayZhaiTen"),
            isDayZhaiGuanYin = callBoolean(foto, "isDayZhaiGuanYin"),
            xiu = callString(foto, "getXiu"),
            xiuLuck = callString(foto, "getXiuLuck"),
            xiuSong = callString(foto, "getXiuSong"),
            fullString = callString(foto, "toFullString")
        )
    }

    /**
     * 获取道历数据（通过反射调用 Tao）
     */
    fun getTaoData(year: Int, month: Int, day: Int): TaoData? {
        val solar = createSolar(year, month, day) ?: return null
        val lunar = callAny(solar, "getLunar") ?: return null
        val tao = createTaoFromLunar(lunar) ?: return null

        val taoYear = callInt(tao, "getYear")
        if (taoYear <= 0) return null

        // 解析节日列表
        val rawFestivals = callObjectList(tao, "getFestivals")
        val festivals = rawFestivals.map { f ->
            TaoFestivalItem(
                name = callString(f, "getName"),
                remark = callString(f, "getRemark")
            )
        }

        return TaoData(
            year = taoYear,
            yearInChinese = callString(tao, "getYearInChinese"),
            monthInChinese = callString(tao, "getMonthInChinese"),
            dayInChinese = callString(tao, "getDayInChinese"),
            festivals = festivals,
            isDaySanHui = callBoolean(tao, "isDaySanHui"),
            isDaySanYuan = callBoolean(tao, "isDaySanYuan"),
            isDayBaJie = callBoolean(tao, "isDayBaJie"),
            isDayWuLa = callBoolean(tao, "isDayWuLa"),
            isDayBaHui = callBoolean(tao, "isDayBaHui"),
            isDayWu = callBoolean(tao, "isDayWu"),
            isDayTianShe = callBoolean(tao, "isDayTianShe"),
            fullString = callString(tao, "toFullString")
        )
    }

    private fun createFotoFromLunar(lunar: Any): Any? = runCatching {
        val cls = Class.forName(FOTO_CLASS)
        cls.getMethod("fromLunar", lunar.javaClass).invoke(null, lunar)
    }.getOrElse {
        // 如果参数类型不匹配，尝试用接口或父类
        runCatching {
            val cls = Class.forName(FOTO_CLASS)
            val lunarCls = Class.forName(LUNAR_CLASS)
            cls.getMethod("fromLunar", lunarCls).invoke(null, lunar)
        }.getOrNull()
    }

    private fun createTaoFromLunar(lunar: Any): Any? = runCatching {
        val cls = Class.forName(TAO_CLASS)
        cls.getMethod("fromLunar", lunar.javaClass).invoke(null, lunar)
    }.getOrElse {
        runCatching {
            val cls = Class.forName(TAO_CLASS)
            val lunarCls = Class.forName(LUNAR_CLASS)
            cls.getMethod("fromLunar", lunarCls).invoke(null, lunar)
        }.getOrNull()
    }

    private fun createSolar(year: Int, month: Int, day: Int): Any? = runCatching {
        val cls = Class.forName(SOLAR_CLASS)
        cls.getMethod(
            "fromYmd",
            Int::class.javaPrimitiveType,
            Int::class.javaPrimitiveType,
            Int::class.javaPrimitiveType
        ).invoke(null, year, month, day)
    }.getOrNull()

    private fun createLunar(year: Int, month: Int, day: Int, isLeapMonth: Boolean): Any? {
        val cls = runCatching { Class.forName(LUNAR_CLASS) }.getOrNull() ?: return null

        val direct = runCatching {
            cls.getMethod(
                "fromYmd",
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType
            ).invoke(null, year, month, day)
        }.getOrNull()
        if (direct != null && !isLeapMonth) return direct

        return runCatching {
            cls.getMethod(
                "fromYmd",
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
                Boolean::class.javaPrimitiveType
            ).invoke(null, year, month, day, isLeapMonth)
        }.getOrNull()
    }

    private fun callAny(target: Any, methodName: String): Any? = runCatching {
        target.javaClass.getMethod(methodName).invoke(target)
    }.getOrNull()

    private fun callObjectList(target: Any, methodName: String): List<Any> {
        val raw = callAny(target, methodName) ?: return emptyList()
        return when (raw) {
            is Iterable<*> -> raw.filterNotNull()
            is Array<*> -> raw.filterNotNull()
            else -> emptyList()
        }
    }

    private fun callString(target: Any, methodName: String): String = runCatching {
        target.javaClass.getMethod(methodName).invoke(target)?.toString().orEmpty()
    }.getOrDefault("")

    private fun callFirstString(target: Any, vararg methodNames: String): String {
        methodNames.forEach { methodName ->
            val value = callString(target, methodName).trim()
            if (value.isNotEmpty()) return value
        }
        return ""
    }

    private fun callStringOrNull(target: Any, methodName: String): String? {
        val value = callString(target, methodName).trim()
        return value.takeIf { it.isNotEmpty() }
    }

    private fun callInt(target: Any, methodName: String): Int = runCatching {
        val value = target.javaClass.getMethod(methodName).invoke(target)
        when (value) {
            is Int -> value
            is Number -> value.toInt()
            else -> value?.toString()?.toIntOrNull() ?: 0
        }
    }.getOrDefault(0)

    private fun callBoolean(target: Any, methodName: String): Boolean = runCatching {
        val value = target.javaClass.getMethod(methodName).invoke(target)
        when (value) {
            is Boolean -> value
            else -> value?.toString()?.toBoolean() ?: false
        }
    }.getOrDefault(false)

    private fun callBooleanOrNull(target: Any, methodName: String): Boolean? = runCatching {
        val value = target.javaClass.getMethod(methodName).invoke(target)
        when (value) {
            is Boolean -> value
            else -> value?.toString()?.toBooleanStrictOrNull()
        }
    }.getOrNull()

    private fun callStringList(target: Any, methodName: String): List<String> {
        val raw = runCatching {
            target.javaClass.getMethod(methodName).invoke(target)
        }.getOrNull() ?: return emptyList()

        val values = when (raw) {
            is Iterable<*> -> raw.mapNotNull { it?.toString()?.trim() }
            is Array<*> -> raw.mapNotNull { it?.toString()?.trim() }
            else -> emptyList()
        }

        return values.filter { it.isNotEmpty() }.distinct()
    }

    private fun joinNotEmpty(items: List<String>): String = items.joinToString(" ").trim()

    private data class HolidayInfo(
        val name: String?,
        val isRestDay: Boolean
    )
}

