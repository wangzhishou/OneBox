package com.wanbaohe.calendar.data

@Suppress("MagicNumber")
object LunarCalendarCalculator {

    val TIAN_GAN = arrayOf("甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸")
    val DI_ZHI = arrayOf("子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥")
    private val ZODIAC = arrayOf("鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪")
    private val LMN = arrayOf("正月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "冬月", "腊月")
    private val LDN = arrayOf("初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十", "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十", "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十")
    val SOLAR_TERMS = arrayOf("小寒", "大寒", "立春", "雨水", "惊蛰", "春分", "清明", "谷雨", "立夏", "小满", "芒种", "夏至", "小暑", "大暑", "立秋", "处暑", "白露", "秋分", "寒露", "霜降", "立冬", "小雪", "大雪", "冬至")

    private val CONS_N = arrayOf("摩羯座", "水瓶座", "双鱼座", "白羊座", "金牛座", "双子座", "巨蟹座", "狮子座", "处女座", "天秤座", "天蝎座", "射手座", "摩羯座")
    private val CONS_E = intArrayOf(20, 19, 21, 20, 21, 22, 23, 23, 23, 24, 23, 22)
    private val S28 = arrayOf("角", "亢", "氐", "房", "心", "尾", "箕", "斗", "牛", "女", "虚", "危", "室", "壁", "奎", "娄", "胃", "昴", "毕", "觜", "参", "井", "鬼", "柳", "星", "张", "翼", "轸")
    private val JC = arrayOf("建", "除", "满", "平", "定", "执", "破", "危", "成", "收", "开", "闭")
    private val PZG = arrayOf("甲不开仓财物耗散", "乙不栽植千株不长", "丙不修灶必见灾殃", "丁不剃头头必生疮", "戊不受田田主不祥", "己不破券二比并亡", "庚不经络织机虚张", "辛不合酱主人不尝", "壬不汲水更难提防", "癸不词讼理弱敌强")
    private val PZZ = arrayOf("子不问卜自惹祸殃", "丑不冠带主不还乡", "寅不祭祀神鬼不尝", "卯不穿井水泉不香", "辰不哭泣必主重丧", "巳不远行财物伏藏", "午不苫盖屋主更张", "未不服药毒气入肠", "申不安床鬼祟入房", "酉不宴客醉坐颠狂", "戌不吃犬作怪上床", "亥不嫁娶不利新郎")
    private val XS = arrayOf("东北", "西北", "西南", "正南", "东南", "东北", "西北", "西南", "正南", "东南")
    private val CS = arrayOf("东北", "东北", "正西", "正西", "正北", "正北", "正东", "正东", "正南", "正南")
    private val FS = arrayOf("东南", "东南", "正东", "正东", "正北", "正北", "西南", "西南", "西北", "西北")
    private val NY = arrayOf("海中金", "炉中火", "大林木", "路旁土", "剑锋金", "山头火", "涧下水", "城头土", "白蜡金", "杨柳木", "泉中水", "屋上土", "霹雳火", "松柏木", "长流水", "砂石金", "山下火", "平地木", "壁上土", "金箔金", "覆灯火", "天河水", "大驿土", "钗环金", "桑柘木", "大溪水", "沙中土", "天上火", "石榴木", "大海水")

    private val LI = longArrayOf(0x04bd8,0x04ae0,0x0a570,0x054d5,0x0d260,0x0d950,0x16554,0x056a0,0x09ad0,0x055d2,0x04ae0,0x0a5b6,0x0a4d0,0x0d250,0x1d255,0x0b540,0x0d6a0,0x0ada2,0x095b0,0x14977,0x04970,0x0a4b0,0x0b4b5,0x06a50,0x06d40,0x1ab54,0x02b60,0x09570,0x052f2,0x04970,0x06566,0x0d4a0,0x0ea50,0x06e95,0x05ad0,0x02b60,0x186e3,0x092e0,0x1c8d7,0x0c950,0x0d4a0,0x1d8a6,0x0b550,0x056a0,0x1a5b4,0x025d0,0x092d0,0x0d2b2,0x0a950,0x0b557,0x06ca0,0x0b550,0x15355,0x04da0,0x0a5b0,0x14573,0x052b0,0x0a9a8,0x0e950,0x06aa0,0x0aea6,0x0ab50,0x04b60,0x0aae4,0x0a570,0x05260,0x0f263,0x0d950,0x05b57,0x056a0,0x096d0,0x04dd5,0x04ad0,0x0a4d0,0x0d4d4,0x0d250,0x0d558,0x0b540,0x0b6a0,0x195a6,0x095b0,0x049b0,0x0a974,0x0a4b0,0x0b27a,0x06a50,0x06d40,0x0af46,0x0ab60,0x09570,0x04af5,0x04970,0x064b0,0x074a3,0x0ea50,0x06b58,0x05ac0,0x0ab60,0x096d5,0x092e0,0x0c960,0x0d954,0x0d4a0,0x0da50,0x07552,0x056a0,0x0abb7,0x025d0,0x092d0,0x0cab5,0x0a950,0x0b4a0,0x0baa4,0x0ad50,0x055d9,0x04ba0,0x0a5b0,0x15176,0x052b0,0x0a930,0x07954,0x06aa0,0x0ad50,0x05b52,0x04b60,0x0a6e6,0x0a4e0,0x0d260,0x0ea65,0x0d530,0x05aa0,0x076a3,0x096d0,0x04afb,0x04ad0,0x0a4d0,0x1d0b6,0x0d250,0x0d520,0x0dd45,0x0b5a0,0x056d0,0x055b2,0x049b0,0x0a577,0x0a4b0,0x0aa50,0x1b255,0x06d20,0x0ada0,0x14b63,0x09370,0x049f8,0x04970,0x064b0,0x168a6,0x0ea50,0x06aa0,0x1a6c4,0x0aae0,0x092e0,0x0d2e3,0x0c960,0x0d557,0x0d4a0,0x0da50,0x05d55,0x056a0,0x0a6d0,0x055d4,0x052d0,0x0a9b8,0x0a950,0x0b4a0,0x0b6a6,0x0ad50,0x055a0,0x0aba4,0x0a5b0,0x052b0,0x0b273,0x06930,0x07337,0x06aa0,0x0ad50,0x14b55,0x04b60,0x0a570,0x054e4,0x0d160,0x0e968,0x0d520,0x0daa0,0x16aa6,0x056d0,0x04ae0,0x0a9d4,0x0a4d0,0x0d150,0x0f252,0x0d520)

    private fun leapMonth(y: Int): Int { val i = y - 1900; return if (i < 0 || i >= LI.size) 0 else ((LI[i] shr 12) and 0xF).toInt() }
    private fun leapDays(y: Int): Int { if (leapMonth(y) == 0) return 0; val i = y - 1900; return if (i < 0 || i >= LI.size) 0 else if ((LI[i] shr 16) and 1L == 1L) 30 else 29 }
    private fun monthDays(y: Int, m: Int): Int { val i = y - 1900; return if (i < 0 || i >= LI.size) 29 else if ((LI[i] shr (m - 1)) and 1L == 1L) 30 else 29 }
    private fun yearDays(y: Int): Int { var t = 0; for (m in 1..12) t += monthDays(y, m); t += leapDays(y); return t }

    fun getGanZhiYear(ly: Int): String { val o = ly - 4; return TIAN_GAN[((o % 10) + 10) % 10] + DI_ZHI[((o % 12) + 12) % 12] }
    fun getZodiac(ly: Int): String = ZODIAC[((ly - 4) % 12 + 12) % 12]
    fun getGanZhiMonth(ly: Int, lm: Int): String { val yg = ((ly - 4) % 10 + 10) % 10; return TIAN_GAN[(yg * 2 + lm) % 10] + DI_ZHI[(lm + 1) % 12] }
    fun getGanZhiDay(y: Int, m: Int, d: Int): String { val o = dd(y, m, d) + 36; return TIAN_GAN[((o % 10) + 10) % 10] + DI_ZHI[((o % 12) + 12) % 12] }
    fun getDayGanIndex(y: Int, m: Int, d: Int): Int { val o = dd(y, m, d) + 36; return ((o % 10) + 10) % 10 }
    fun getDayZhiIndex(y: Int, m: Int, d: Int): Int { val o = dd(y, m, d) + 36; return ((o % 12) + 12) % 12 }
    private fun dd(y: Int, m: Int, d: Int): Int { val c = java.util.GregorianCalendar(y, m - 1, d); val b = java.util.GregorianCalendar(1900, 0, 1); return ((c.timeInMillis - b.timeInMillis) / 86400000L).toInt() }

    fun getConstellation(m: Int, d: Int): String = if (d < CONS_E[m - 1]) CONS_N[m - 1] else CONS_N[m]
    fun getTwentyEightStar(y: Int, m: Int, d: Int): String = S28[((dd(y, m, d) + 11) % 28 + 28) % 28]
    fun getJianChu(y: Int, m: Int, d: Int, lm: Int): String { val z = getDayZhiIndex(y, m, d); return JC[((z - (lm + 1) % 12) % 12 + 12) % 12] }
    fun getNaYin(y: Int, m: Int, d: Int): String { val g = getDayGanIndex(y, m, d); val z = getDayZhiIndex(y, m, d); return NY[((g * 12 + z) / 2) % 30] }
    fun getPengZu(y: Int, m: Int, d: Int): Pair<String, String> = PZG[getDayGanIndex(y, m, d)] to PZZ[getDayZhiIndex(y, m, d)]
    fun getLuckyDirections(y: Int, m: Int, d: Int): Map<String, String> { val g = getDayGanIndex(y, m, d); return mapOf("喜神" to XS[g], "福神" to FS[g], "财神" to CS[g]) }
    fun getWeekDayName(y: Int, m: Int, d: Int): String { val c = java.util.GregorianCalendar(y, m - 1, d); return arrayOf("星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六")[c.get(java.util.Calendar.DAY_OF_WEEK) - 1] }

    fun solarToLunar(sy: Int, sm: Int, sd: Int): LunarDate {
        LunarJavaBridge.solarToLunarDate(sy, sm, sd)?.let { return it }

        val base = java.util.GregorianCalendar(1900, 0, 31)
        val tgt = java.util.GregorianCalendar(sy, sm - 1, sd)
        var off = ((tgt.timeInMillis - base.timeInMillis) / 86400000L).toInt()
        var ly = 1900; while (ly < 2101 && off > 0) { val yd = yearDays(ly); if (off < yd) break; off -= yd; ly++ }
        val leapM = leapMonth(ly); var isLeap = false; var lm = 1
        for (m in 1..13) { val dim: Int
            if (leapM > 0 && m == leapM + 1 && !isLeap) { dim = leapDays(ly); isLeap = true; lm = leapM }
            else { val am = if (isLeap || (leapM > 0 && m > leapM + 1)) m - 1 else m; if (am > 12) break; dim = monthDays(ly, am); lm = am; if (isLeap && m == leapM + 2) isLeap = false }
            if (off < dim) break; off -= dim }
        val ld = off + 1
        val mn = if (isLeap) "闰${LMN[lm - 1]}" else LMN[lm - 1]
        val dn = LDN.getOrElse(ld - 1) { "三十" }
        val st = getSolarTerm(sy, sm, sd)
        val lf = if (isLeap) null else LUNAR_FESTS["$lm-$ld"]
        val sf = SOLAR_FESTS["$sm-$sd"]
        val pz = getPengZu(sy, sm, sd); val dirs = getLuckyDirections(sy, sm, sd)
        return LunarDate(year = ly, month = lm, day = ld, isLeapMonth = isLeap, ganZhiYear = getGanZhiYear(ly), ganZhiMonth = getGanZhiMonth(ly, lm), ganZhiDay = getGanZhiDay(sy, sm, sd), zodiac = getZodiac(ly), monthName = mn, dayName = dn, solarTerm = st, lunarFestival = lf, lunarFestivals = listOfNotNull(lf), solarFestival = sf, solarFestivals = listOfNotNull(sf), legalHolidayName = null, isLegalHoliday = false, constellation = getConstellation(sm, sd), star28 = getTwentyEightStar(sy, sm, sd), jianChu = getJianChu(sy, sm, sd, lm), naYin = getNaYin(sy, sm, sd), pengZuGan = pz.first, pengZuZhi = pz.second, xiShen = dirs["喜神"] ?: "", fuShen = dirs["福神"] ?: "", caiShen = dirs["财神"] ?: "", chong = "", sha = "", zhiShen = "", dayJiShen = "", dayXiongSha = "", taiShen = "", nineStar = "")
    }

    fun getMonthCalendarDays(year: Int, month: Int): List<CalendarDayInfo> {
        val r = mutableListOf<CalendarDayInfo>()
        val c = java.util.GregorianCalendar(year, month - 1, 1)
        val dim = c.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
        val fdw = c.get(java.util.Calendar.DAY_OF_WEEK) - 1
        val now = java.util.GregorianCalendar()
        val ty = now.get(java.util.Calendar.YEAR); val tm = now.get(java.util.Calendar.MONTH) + 1; val td = now.get(java.util.Calendar.DAY_OF_MONTH)
        if (fdw > 0) { val pc = java.util.GregorianCalendar(year, month - 2, 1); val pdim = pc.getActualMaximum(java.util.Calendar.DAY_OF_MONTH); val pm = if (month == 1) 12 else month - 1; val py = if (month == 1) year - 1 else year; for (i in (pdim - fdw + 1)..pdim) { val l = solarToLunar(py, pm, i); r.add(mk(py, pm, i, l, py == ty && pm == tm && i == td, false)) } }
        for (d in 1..dim) { val l = solarToLunar(year, month, d); r.add(mk(year, month, d, l, year == ty && month == tm && d == td, true)) }
        val rem = 7 - (r.size % 7); if (rem in 1..6) { val nm = if (month == 12) 1 else month + 1; val ny = if (month == 12) year + 1 else year; for (d in 1..rem) { val l = solarToLunar(ny, nm, d); r.add(mk(ny, nm, d, l, ny == ty && nm == tm && d == td, false)) } }
        return r
    }
    private fun mk(y: Int, m: Int, d: Int, l: LunarDate, t: Boolean, cur: Boolean) = CalendarDayInfo(y, m, d, l.solarTerm ?: l.lunarFestival ?: l.solarFestival ?: l.dayName, l.solarTerm, l.lunarFestival, l.solarFestival, t, cur, isLegalHoliday = l.isLegalHoliday && cur, holidayBadge = if (l.isLegalHoliday && cur) "休" else null)

    fun getSolarTerm(y: Int, m: Int, d: Int): String? { val i1 = (m - 1) * 2; val i2 = i1 + 1; return when (d) { stDay(y, i1) -> SOLAR_TERMS[i1]; stDay(y, i2) -> SOLAR_TERMS[i2]; else -> null } }
    fun getNextSolarTerm(y: Int, m: Int, d: Int): Pair<String, String>? { for (mo in m..12) { val s = if (mo == m) (m - 1) * 2 else (mo - 1) * 2; for (i in s..(mo - 1) * 2 + 1) { if (i >= 24) continue; val td2 = stDay(y, i); val tm2 = i / 2 + 1; if (tm2 > m || (tm2 == m && td2 >= d)) return SOLAR_TERMS[i] to "${tm2}月${td2}日" } }; return SOLAR_TERMS[0] to "1月${stDay(y + 1, 0)}日" }
    private fun stDay(y: Int, n: Int): Int { val yy = y % 100; val t20 = doubleArrayOf(6.11,20.84,4.15,19.02,6.50,20.93,5.63,20.65,6.36,21.37,6.22,21.81,7.75,23.13,8.09,23.13,8.28,23.35,8.44,23.22,8.17,22.36,7.66,22.18); val t21 = doubleArrayOf(5.4055,20.12,3.87,18.73,5.63,20.646,4.81,20.1,5.52,21.04,5.678,21.37,7.108,22.83,7.5,22.84,7.646,23.042,8.318,23.438,7.438,22.36,7.18,21.94); val c = if (y / 100 + 1 == 21) t21[n] else t20[n]; return (yy * 0.2422 + c).toInt() - (yy / 4) }

    private val LUNAR_FESTS = mapOf("1-1" to "春节", "1-15" to "元宵节", "2-2" to "龙抬头", "3-3" to "上巳节", "5-5" to "端午节", "7-7" to "七夕", "7-15" to "中元节", "8-15" to "中秋节", "9-9" to "重阳节", "10-1" to "寒衣节", "10-15" to "下元节", "12-8" to "腊八节", "12-23" to "小年", "12-30" to "除夕")
    val LUNAR_FESTIVALS: Map<String, String> = LUNAR_FESTS
    private val SOLAR_FESTS = mapOf("1-1" to "元旦", "2-14" to "情人节", "3-8" to "妇女节", "3-12" to "植树节", "3-15" to "消费者权益日", "4-1" to "愚人节", "4-22" to "地球日", "5-1" to "劳动节", "5-4" to "青年节", "5-12" to "护士节", "6-1" to "儿童节", "6-5" to "环境日", "7-1" to "建党节", "8-1" to "建军节", "9-3" to "抗战胜利", "9-10" to "教师节", "10-1" to "国庆节", "11-11" to "光棍节", "12-13" to "国家公祭日", "12-24" to "平安夜", "12-25" to "圣诞节")
    val SOLAR_FESTIVALS: Map<String, String> = SOLAR_FESTS
    fun getMonthName(m: Int): String = LMN.getOrElse(m - 1) { "正月" }
    fun getDayName(d: Int): String = LDN.getOrElse(d - 1) { "初一" }

    fun normalizeLunarMonthName(monthName: String, isLeapMonth: Boolean = false): String {
        val trimmed = monthName.trim()
        if (trimmed.isEmpty()) return if (isLeapMonth) "闰月" else "正月"

        val withLeapPrefix = if (isLeapMonth && !trimmed.startsWith("闰")) {
            "闰$trimmed"
        } else {
            trimmed
        }

        return if (withLeapPrefix.endsWith("月")) withLeapPrefix else "${withLeapPrefix}月"
    }
}

data class CalendarDayInfo(
    val solarYear: Int, val solarMonth: Int, val solarDay: Int,
    val lunarDayName: String,
    val solarTerm: String? = null, val lunarFestival: String? = null,
    val solarFestival: String? = null,
    val isToday: Boolean = false, val isCurrentMonth: Boolean = true,
    val isLegalHoliday: Boolean = false,
    val holidayBadge: String? = null
)
