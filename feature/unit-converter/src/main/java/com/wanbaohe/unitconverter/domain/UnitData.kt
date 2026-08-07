package com.wanbaohe.unitconverter.domain

import kotlin.math.PI

/**
 * 所有换算类别及其单位列表。
 * 基准单位（toBase = 1.0）已在各类别中注明。
 */
object UnitData {

    // ─── 长度（基准：米 m） ──────────────────────────────────────────────────
    val LENGTH = listOf(
        UnitItem("米", "m", 1.0),
        UnitItem("厘米", "cm", 0.01),
        UnitItem("千米", "km", 1000.0),
        UnitItem("毫米", "mm", 0.001),
        UnitItem("纳米", "nm", 1e-9),
        UnitItem("分米", "dm", 0.1),
        UnitItem("光年", "ly", 9.4607304725808e15),
        UnitItem("公分", "cm*", 0.01),
        UnitItem("公里", "km*", 1000.0),
        UnitItem("皮米", "pm", 1e-12),
        UnitItem("微米", "μm", 1e-6),
        UnitItem("英寸", "in", 0.0254),
        UnitItem("英尺", "ft", 0.3048),
        UnitItem("英里", "mi", 1609.344),
        UnitItem("英寻", "fm", 1.8288),
        UnitItem("海里", "nmi", 1852.0),
        UnitItem("弗隆", "fur", 201.168),
        UnitItem("厘", "li_cn", 1.0 / 3000.0),
        UnitItem("寸", "cun", 1.0 / 30.0),
        UnitItem("丈", "zhang", 10.0 / 3.0),
        UnitItem("尺", "chi", 1.0 / 3.0),
        UnitItem("分", "fen_cn", 1.0 / 300.0),
        UnitItem("毫", "hao", 1.0 / 30000.0),
        UnitItem("里", "li", 500.0),
    )

    // ─── 质量（基准：千克 kg） ──────────────────────────────────────────────
    val MASS = listOf(
        UnitItem("千克", "kg", 1.0),
        UnitItem("克", "g", 0.001),
        UnitItem("毫克", "mg", 1e-6),
        UnitItem("微克", "μg", 1e-9),
        UnitItem("吨", "t", 1000.0),
        UnitItem("磅", "lb", 0.45359237),
        UnitItem("盎司", "oz", 0.028349523125),
        UnitItem("克拉", "ct", 0.0002),
        UnitItem("斤", "jin", 0.5),
        UnitItem("两", "liang", 0.05),
        UnitItem("钱", "qian", 0.005),
    )

    // ─── 温度（基准：摄氏度 °C，非线性） ───────────────────────────────────
    val TEMPERATURE = listOf(
        UnitItem(
            name = "摄氏度", symbol = "°C",
            toBaseFn = { it },
            fromBaseFn = { it }
        ),
        UnitItem(
            name = "华氏度", symbol = "°F",
            toBaseFn = { (it - 32.0) * 5.0 / 9.0 },
            fromBaseFn = { it * 9.0 / 5.0 + 32.0 }
        ),
        UnitItem(
            name = "开尔文", symbol = "K",
            toBaseFn = { it - 273.15 },
            fromBaseFn = { it + 273.15 }
        ),
        UnitItem(
            name = "兰氏度", symbol = "°Ra",
            toBaseFn = { (it - 491.67) * 5.0 / 9.0 },
            fromBaseFn = { (it + 273.15) * 9.0 / 5.0 }
        ),
    )

    // ─── 面积（基准：平方米 m²） ────────────────────────────────────────────
    val AREA = listOf(
        UnitItem("平方米", "m²", 1.0),
        UnitItem("平方厘米", "cm²", 1e-4),
        UnitItem("平方毫米", "mm²", 1e-6),
        UnitItem("平方千米", "km²", 1e6),
        UnitItem("公顷", "ha", 10000.0),
        UnitItem("亩", "mu", 666.6667),
        UnitItem("平方分米", "dm²", 0.01),
        UnitItem("平方英寸", "in²", 6.4516e-4),
        UnitItem("平方英尺", "ft²", 0.09290304),
        UnitItem("平方码", "yd²", 0.83612736),
        UnitItem("英亩", "acre", 4046.8564224),
        UnitItem("平方英里", "mi²", 2589988.110336),
    )

    // ─── 体积（基准：升 L） ────────────────────────────────────────────────
    val VOLUME = listOf(
        UnitItem("升", "L", 1.0),
        UnitItem("毫升", "mL", 0.001),
        UnitItem("微升", "μL", 1e-6),
        UnitItem("立方米", "m³", 1000.0),
        UnitItem("立方厘米", "cm³", 0.001),
        UnitItem("立方分米", "dm³", 1.0),
        UnitItem("立方英寸", "in³", 0.016387064),
        UnitItem("立方英尺", "ft³", 28.316847),
        UnitItem("美加仑", "US gal", 3.785411784),
        UnitItem("英加仑", "UK gal", 4.54609),
        UnitItem("美品脱", "US pt", 0.473176473),
        UnitItem("英品脱", "UK pt", 0.56826125),
        UnitItem("桶", "bbl", 158.987295),
    )

    // ─── 压力（基准：帕斯卡 Pa） ────────────────────────────────────────────
    val PRESSURE = listOf(
        UnitItem("帕斯卡", "Pa", 1.0),
        UnitItem("千帕", "kPa", 1000.0),
        UnitItem("兆帕", "MPa", 1e6),
        UnitItem("标准大气压", "atm", 101325.0),
        UnitItem("巴", "bar", 1e5),
        UnitItem("毫巴", "mbar", 100.0),
        UnitItem("毫米汞柱", "mmHg", 133.322387415),
        UnitItem("托", "Torr", 133.322387415),
        UnitItem("磅/平方英寸", "psi", 6894.757293168),
    )

    // ─── 功率（基准：瓦特 W） ──────────────────────────────────────────────
    val POWER = listOf(
        UnitItem("瓦特", "W", 1.0),
        UnitItem("千瓦", "kW", 1000.0),
        UnitItem("兆瓦", "MW", 1e6),
        UnitItem("吉瓦", "GW", 1e9),
        UnitItem("马力(公制)", "PS", 735.49875),
        UnitItem("马力(英制)", "hp", 745.69987),
        UnitItem("千卡/时", "kcal/h", 1.163),
        UnitItem("BTU/时", "BTU/h", 0.29307107),
        UnitItem("尔格/秒", "erg/s", 1e-7),
    )

    // ─── 功、能和热量（基准：焦耳 J） ──────────────────────────────────────
    val ENERGY = listOf(
        UnitItem("焦耳", "J", 1.0),
        UnitItem("千焦", "kJ", 1000.0),
        UnitItem("兆焦", "MJ", 1e6),
        UnitItem("吉焦", "GJ", 1e9),
        UnitItem("卡路里", "cal", 4.1868),
        UnitItem("千卡", "kcal", 4186.8),
        UnitItem("千瓦时", "kWh", 3.6e6),
        UnitItem("度", "度(kWh)", 3.6e6),
        UnitItem("电子伏特", "eV", 1.602176634e-19),
        UnitItem("BTU", "BTU", 1055.05585262),
        UnitItem("尔格", "erg", 1e-7),
        UnitItem("英尺·磅力", "ft·lbf", 1.3558179483),
    )

    // ─── 力（基准：牛顿 N） ────────────────────────────────────────────────
    val FORCE = listOf(
        UnitItem("牛顿", "N", 1.0),
        UnitItem("千牛", "kN", 1000.0),
        UnitItem("兆牛", "MN", 1e6),
        UnitItem("达因", "dyn", 1e-5),
        UnitItem("千克力", "kgf", 9.80665),
        UnitItem("克力", "gf", 0.00980665),
        UnitItem("吨力", "tf", 9806.65),
        UnitItem("磅力", "lbf", 4.4482216152605),
        UnitItem("盎司力", "ozf", 0.278013851),
    )

    // ─── 时间（基准：秒 s） ────────────────────────────────────────────────
    val TIME = listOf(
        UnitItem("秒", "s", 1.0),
        UnitItem("毫秒", "ms", 0.001),
        UnitItem("微秒", "μs", 1e-6),
        UnitItem("纳秒", "ns", 1e-9),
        UnitItem("分钟", "min", 60.0),
        UnitItem("小时", "h", 3600.0),
        UnitItem("天", "d", 86400.0),
        UnitItem("周", "week", 604800.0),
        UnitItem("月(均)", "month", 2629800.0),
        UnitItem("年(均)", "yr", 31557600.0),
        UnitItem("世纪", "century", 3.15576e9),
    )

    // ─── 速度（基准：米/秒 m/s） ────────────────────────────────────────────
    val SPEED = listOf(
        UnitItem("米/秒", "m/s", 1.0),
        UnitItem("千米/时", "km/h", 1.0 / 3.6),
        UnitItem("英里/时", "mph", 0.44704),
        UnitItem("海里/时(节)", "kn", 0.514444),
        UnitItem("英尺/秒", "ft/s", 0.3048),
        UnitItem("马赫(15°C)", "Ma", 340.29),
        UnitItem("光速", "c", 299792458.0),
    )

    // ─── 角度（基准：度 °） ────────────────────────────────────────────────
    val ANGLE = listOf(
        UnitItem("度", "°", 1.0),
        UnitItem("弧度", "rad", 180.0 / PI),
        UnitItem("分", "'", 1.0 / 60.0),
        UnitItem("秒", "\"", 1.0 / 3600.0),
        UnitItem("百分度", "grad", 0.9),
        UnitItem("圈", "rev", 360.0),
        UnitItem("毫弧度", "mrad", 180.0 / PI / 1000.0),
    )

    // ─── 密度（基准：千克/立方米 kg/m³） ────────────────────────────────────
    val DENSITY = listOf(
        UnitItem("千克/立方米", "kg/m³", 1.0),
        UnitItem("克/立方厘米", "g/cm³", 1000.0),
        UnitItem("克/升", "g/L", 1.0),
        UnitItem("千克/升", "kg/L", 1000.0),
        UnitItem("毫克/毫升", "mg/mL", 1.0),
        UnitItem("磅/立方英尺", "lb/ft³", 16.0184633739601),
        UnitItem("磅/立方英寸", "lb/in³", 27679.9047102031),
        UnitItem("磅/美加仑", "lb/US gal", 119.826427),
    )

    // ─── 数据存储（基准：字节 B） ────────────────────────────────────────────
    val DATA = listOf(
        UnitItem("字节", "B", 1.0),
        UnitItem("位", "bit", 0.125),
        UnitItem("千字节", "KB", 1024.0),
        UnitItem("兆字节", "MB", 1048576.0),
        UnitItem("吉字节", "GB", 1073741824.0),
        UnitItem("太字节", "TB", 1099511627776.0),
        UnitItem("拍字节", "PB", 1.125899906842624e15),
        UnitItem("千字节(SI)", "kB", 1000.0),
        UnitItem("兆字节(SI)", "MB(SI)", 1e6),
        UnitItem("吉字节(SI)", "GB(SI)", 1e9),
        UnitItem("太字节(SI)", "TB(SI)", 1e12),
    )

    // ─── 人民币汇率（基准：人民币 CNY，参考汇率）────────────────────────────
    val CURRENCY = listOf(
        UnitItem("人民币", "CNY", 1.0),
        UnitItem("美元", "USD", 7.25),
        UnitItem("欧元", "EUR", 7.85),
        UnitItem("英镑", "GBP", 9.15),
        UnitItem("日元", "JPY", 0.048),
        UnitItem("港元", "HKD", 0.93),
        UnitItem("韩元", "KRW", 0.0052),
        UnitItem("澳大利亚元", "AUD", 4.65),
        UnitItem("加拿大元", "CAD", 5.30),
        UnitItem("瑞士法郎", "CHF", 8.10),
        UnitItem("新加坡元", "SGD", 5.40),
        UnitItem("新台币", "TWD", 0.22),
        UnitItem("马来西亚令吉", "MYR", 1.62),
        UnitItem("泰铢", "THB", 0.21),
    )

    // ─── 电阻（基准：欧姆 Ω） ──────────────────────────────────────────────
    val RESISTANCE = listOf(
        UnitItem("欧姆", "Ω", 1.0),
        UnitItem("毫欧", "mΩ", 0.001),
        UnitItem("微欧", "μΩ", 1e-6),
        UnitItem("千欧", "kΩ", 1000.0),
        UnitItem("兆欧", "MΩ", 1e6),
        UnitItem("吉欧", "GΩ", 1e9),
    )

    /** 所有类别（顺序与截图一致） */
    val ALL_CATEGORIES = listOf(
        UnitCategory.Length,
        UnitCategory.Volume,
        UnitCategory.Mass,
        UnitCategory.Temperature,
        UnitCategory.Area,
        UnitCategory.Pressure,
        UnitCategory.Power,
        UnitCategory.Energy,
        UnitCategory.Force,
        UnitCategory.Time,
        UnitCategory.Speed,
        UnitCategory.Angle,
        UnitCategory.Density,
        UnitCategory.DataStorage,
        UnitCategory.Currency,
        UnitCategory.Resistance,
    )

    fun unitsFor(category: UnitCategory): List<UnitItem> = when (category) {
        UnitCategory.Length -> LENGTH
        UnitCategory.Mass -> MASS
        UnitCategory.Temperature -> TEMPERATURE
        UnitCategory.Area -> AREA
        UnitCategory.Volume -> VOLUME
        UnitCategory.Pressure -> PRESSURE
        UnitCategory.Power -> POWER
        UnitCategory.Energy -> ENERGY
        UnitCategory.Force -> FORCE
        UnitCategory.Time -> TIME
        UnitCategory.Speed -> SPEED
        UnitCategory.Angle -> ANGLE
        UnitCategory.Density -> DENSITY
        UnitCategory.DataStorage -> DATA
        UnitCategory.Currency -> CURRENCY
        UnitCategory.Resistance -> RESISTANCE
    }
}

