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

package com.wanbaohe.unitconverter.domain

import com.wanbaohe.unitconverter.R
import kotlin.math.PI

/**
 * 所有换算类别及其单位列表。
 * 基准单位（toBase = 1.0）已在各类别中注明。
 */
object UnitData {

    // ─── 长度（基准：米 m） ──────────────────────────────────────────────────
    val LENGTH = listOf(
        UnitItem(R.string.unit_name_meter, "m", 1.0),
        UnitItem(R.string.unit_name_centimeter, "cm", 0.01),
        UnitItem(R.string.unit_name_kilometer, "km", 1000.0),
        UnitItem(R.string.unit_name_millimeter, "mm", 0.001),
        UnitItem(R.string.unit_name_nanometer, "nm", 1e-9),
        UnitItem(R.string.unit_name_decimeter, "dm", 0.1),
        UnitItem(R.string.unit_name_light_year, "ly", 9.4607304725808e15),
        UnitItem(R.string.unit_name_gongfen, "cm*", 0.01),
        UnitItem(R.string.unit_name_gongli, "km*", 1000.0),
        UnitItem(R.string.unit_name_picometer, "pm", 1e-12),
        UnitItem(R.string.unit_name_micrometer, "μm", 1e-6),
        UnitItem(R.string.unit_name_inch, "in", 0.0254),
        UnitItem(R.string.unit_name_foot, "ft", 0.3048),
        UnitItem(R.string.unit_name_mile, "mi", 1609.344),
        UnitItem(R.string.unit_name_fathom, "fm", 1.8288),
        UnitItem(R.string.unit_name_nautical_mile, "nmi", 1852.0),
        UnitItem(R.string.unit_name_furlong, "fur", 201.168),
        UnitItem(R.string.unit_name_li_cn, "li_cn", 1.0 / 3000.0),
        UnitItem(R.string.unit_name_cun, "cun", 1.0 / 30.0),
        UnitItem(R.string.unit_name_zhang, "zhang", 10.0 / 3.0),
        UnitItem(R.string.unit_name_chi, "chi", 1.0 / 3.0),
        UnitItem(R.string.unit_name_fen_cn, "fen_cn", 1.0 / 300.0),
        UnitItem(R.string.unit_name_hao, "hao", 1.0 / 30000.0),
        UnitItem(R.string.unit_name_li, "li", 500.0),
    )

    // ─── 质量（基准：千克 kg） ──────────────────────────────────────────────
    val MASS = listOf(
        UnitItem(R.string.unit_name_kilogram, "kg", 1.0),
        UnitItem(R.string.unit_name_gram, "g", 0.001),
        UnitItem(R.string.unit_name_milligram, "mg", 1e-6),
        UnitItem(R.string.unit_name_microgram, "μg", 1e-9),
        UnitItem(R.string.unit_name_ton, "t", 1000.0),
        UnitItem(R.string.unit_name_pound, "lb", 0.45359237),
        UnitItem(R.string.unit_name_ounce, "oz", 0.028349523125),
        UnitItem(R.string.unit_name_carat, "ct", 0.0002),
        UnitItem(R.string.unit_name_jin, "jin", 0.5),
        UnitItem(R.string.unit_name_liang, "liang", 0.05),
        UnitItem(R.string.unit_name_qian, "qian", 0.005),
    )

    // ─── 温度（基准：摄氏度 °C，非线性） ───────────────────────────────────
    val TEMPERATURE = listOf(
        UnitItem(
            nameRes = R.string.unit_name_celsius, symbol = "°C",
            toBaseFn = { it },
            fromBaseFn = { it }
        ),
        UnitItem(
            nameRes = R.string.unit_name_fahrenheit, symbol = "°F",
            toBaseFn = { (it - 32.0) * 5.0 / 9.0 },
            fromBaseFn = { it * 9.0 / 5.0 + 32.0 }
        ),
        UnitItem(
            nameRes = R.string.unit_name_kelvin, symbol = "K",
            toBaseFn = { it - 273.15 },
            fromBaseFn = { it + 273.15 }
        ),
        UnitItem(
            nameRes = R.string.unit_name_rankine, symbol = "°Ra",
            toBaseFn = { (it - 491.67) * 5.0 / 9.0 },
            fromBaseFn = { (it + 273.15) * 9.0 / 5.0 }
        ),
    )

    // ─── 面积（基准：平方米 m²） ────────────────────────────────────────────
    val AREA = listOf(
        UnitItem(R.string.unit_name_square_meter, "m²", 1.0),
        UnitItem(R.string.unit_name_square_centimeter, "cm²", 1e-4),
        UnitItem(R.string.unit_name_square_millimeter, "mm²", 1e-6),
        UnitItem(R.string.unit_name_square_kilometer, "km²", 1e6),
        UnitItem(R.string.unit_name_hectare, "ha", 10000.0),
        UnitItem(R.string.unit_name_mu, "mu", 666.6667),
        UnitItem(R.string.unit_name_square_decimeter, "dm²", 0.01),
        UnitItem(R.string.unit_name_square_inch, "in²", 6.4516e-4),
        UnitItem(R.string.unit_name_square_foot, "ft²", 0.09290304),
        UnitItem(R.string.unit_name_square_yard, "yd²", 0.83612736),
        UnitItem(R.string.unit_name_acre, "acre", 4046.8564224),
        UnitItem(R.string.unit_name_square_mile, "mi²", 2589988.110336),
    )

    // ─── 体积（基准：升 L） ────────────────────────────────────────────────
    val VOLUME = listOf(
        UnitItem(R.string.unit_name_liter, "L", 1.0),
        UnitItem(R.string.unit_name_milliliter, "mL", 0.001),
        UnitItem(R.string.unit_name_microliter, "μL", 1e-6),
        UnitItem(R.string.unit_name_cubic_meter, "m³", 1000.0),
        UnitItem(R.string.unit_name_cubic_centimeter, "cm³", 0.001),
        UnitItem(R.string.unit_name_cubic_decimeter, "dm³", 1.0),
        UnitItem(R.string.unit_name_cubic_inch, "in³", 0.016387064),
        UnitItem(R.string.unit_name_cubic_foot, "ft³", 28.316847),
        UnitItem(R.string.unit_name_us_gallon, "US gal", 3.785411784),
        UnitItem(R.string.unit_name_uk_gallon, "UK gal", 4.54609),
        UnitItem(R.string.unit_name_us_pint, "US pt", 0.473176473),
        UnitItem(R.string.unit_name_uk_pint, "UK pt", 0.56826125),
        UnitItem(R.string.unit_name_barrel, "bbl", 158.987295),
    )

    // ─── 压力（基准：帕斯卡 Pa） ────────────────────────────────────────────
    val PRESSURE = listOf(
        UnitItem(R.string.unit_name_pascal, "Pa", 1.0),
        UnitItem(R.string.unit_name_kilopascal, "kPa", 1000.0),
        UnitItem(R.string.unit_name_megapascal, "MPa", 1e6),
        UnitItem(R.string.unit_name_atm, "atm", 101325.0),
        UnitItem(R.string.unit_name_bar, "bar", 1e5),
        UnitItem(R.string.unit_name_millibar, "mbar", 100.0),
        UnitItem(R.string.unit_name_mmhg, "mmHg", 133.322387415),
        UnitItem(R.string.unit_name_torr, "Torr", 133.322387415),
        UnitItem(R.string.unit_name_psi, "psi", 6894.757293168),
    )

    // ─── 功率（基准：瓦特 W） ──────────────────────────────────────────────
    val POWER = listOf(
        UnitItem(R.string.unit_name_watt, "W", 1.0),
        UnitItem(R.string.unit_name_kilowatt, "kW", 1000.0),
        UnitItem(R.string.unit_name_megawatt, "MW", 1e6),
        UnitItem(R.string.unit_name_gigawatt, "GW", 1e9),
        UnitItem(R.string.unit_name_metric_hp, "PS", 735.49875),
        UnitItem(R.string.unit_name_imperial_hp, "hp", 745.69987),
        UnitItem(R.string.unit_name_kcal_per_hour, "kcal/h", 1.163),
        UnitItem(R.string.unit_name_btu_per_hour, "BTU/h", 0.29307107),
        UnitItem(R.string.unit_name_erg_per_second, "erg/s", 1e-7),
    )

    // ─── 功、能和热量（基准：焦耳 J） ──────────────────────────────────────
    val ENERGY = listOf(
        UnitItem(R.string.unit_name_joule, "J", 1.0),
        UnitItem(R.string.unit_name_kilojoule, "kJ", 1000.0),
        UnitItem(R.string.unit_name_megajoule, "MJ", 1e6),
        UnitItem(R.string.unit_name_gigajoule, "GJ", 1e9),
        UnitItem(R.string.unit_name_calorie, "cal", 4.1868),
        UnitItem(R.string.unit_name_kilocalorie, "kcal", 4186.8),
        UnitItem(R.string.unit_name_kilowatt_hour, "kWh", 3.6e6),
        UnitItem(R.string.unit_name_electric_unit, "度(kWh)", 3.6e6),
        UnitItem(R.string.unit_name_electronvolt, "eV", 1.602176634e-19),
        UnitItem(R.string.unit_name_btu, "BTU", 1055.05585262),
        UnitItem(R.string.unit_name_erg, "erg", 1e-7),
        UnitItem(R.string.unit_name_foot_pound, "ft·lbf", 1.3558179483),
    )

    // ─── 力（基准：牛顿 N） ────────────────────────────────────────────────
    val FORCE = listOf(
        UnitItem(R.string.unit_name_newton, "N", 1.0),
        UnitItem(R.string.unit_name_kilonewton, "kN", 1000.0),
        UnitItem(R.string.unit_name_meganewton, "MN", 1e6),
        UnitItem(R.string.unit_name_dyne, "dyn", 1e-5),
        UnitItem(R.string.unit_name_kilogram_force, "kgf", 9.80665),
        UnitItem(R.string.unit_name_gram_force, "gf", 0.00980665),
        UnitItem(R.string.unit_name_ton_force, "tf", 9806.65),
        UnitItem(R.string.unit_name_pound_force, "lbf", 4.4482216152605),
        UnitItem(R.string.unit_name_ounce_force, "ozf", 0.278013851),
    )

    // ─── 时间（基准：秒 s） ────────────────────────────────────────────────
    val TIME = listOf(
        UnitItem(R.string.unit_name_second, "s", 1.0),
        UnitItem(R.string.unit_name_millisecond, "ms", 0.001),
        UnitItem(R.string.unit_name_microsecond, "μs", 1e-6),
        UnitItem(R.string.unit_name_nanosecond, "ns", 1e-9),
        UnitItem(R.string.unit_name_minute, "min", 60.0),
        UnitItem(R.string.unit_name_hour, "h", 3600.0),
        UnitItem(R.string.unit_name_day, "d", 86400.0),
        UnitItem(R.string.unit_name_week, "week", 604800.0),
        UnitItem(R.string.unit_name_month_avg, "month", 2629800.0),
        UnitItem(R.string.unit_name_year_avg, "yr", 31557600.0),
        UnitItem(R.string.unit_name_century, "century", 3.15576e9),
    )

    // ─── 速度（基准：米/秒 m/s） ────────────────────────────────────────────
    val SPEED = listOf(
        UnitItem(R.string.unit_name_meter_per_second, "m/s", 1.0),
        UnitItem(R.string.unit_name_kilometer_per_hour, "km/h", 1.0 / 3.6),
        UnitItem(R.string.unit_name_mile_per_hour, "mph", 0.44704),
        UnitItem(R.string.unit_name_knot, "kn", 0.514444),
        UnitItem(R.string.unit_name_foot_per_second, "ft/s", 0.3048),
        UnitItem(R.string.unit_name_mach, "Ma", 340.29),
        UnitItem(R.string.unit_name_speed_of_light, "c", 299792458.0),
    )

    // ─── 角度（基准：度 °） ────────────────────────────────────────────────
    val ANGLE = listOf(
        UnitItem(R.string.unit_name_degree, "°", 1.0),
        UnitItem(R.string.unit_name_radian, "rad", 180.0 / PI),
        UnitItem(R.string.unit_name_arcminute, "'", 1.0 / 60.0),
        UnitItem(R.string.unit_name_arcsecond, "\"", 1.0 / 3600.0),
        UnitItem(R.string.unit_name_gradian, "grad", 0.9),
        UnitItem(R.string.unit_name_revolution, "rev", 360.0),
        UnitItem(R.string.unit_name_milliradian, "mrad", 180.0 / PI / 1000.0),
    )

    // ─── 密度（基准：千克/立方米 kg/m³） ────────────────────────────────────
    val DENSITY = listOf(
        UnitItem(R.string.unit_name_kg_per_m3, "kg/m³", 1.0),
        UnitItem(R.string.unit_name_g_per_cm3, "g/cm³", 1000.0),
        UnitItem(R.string.unit_name_g_per_l, "g/L", 1.0),
        UnitItem(R.string.unit_name_kg_per_l, "kg/L", 1000.0),
        UnitItem(R.string.unit_name_mg_per_ml, "mg/mL", 1.0),
        UnitItem(R.string.unit_name_lb_per_ft3, "lb/ft³", 16.0184633739601),
        UnitItem(R.string.unit_name_lb_per_in3, "lb/in³", 27679.9047102031),
        UnitItem(R.string.unit_name_lb_per_us_gal, "lb/US gal", 119.826427),
    )

    // ─── 数据存储（基准：字节 B） ────────────────────────────────────────────
    val DATA = listOf(
        UnitItem(R.string.unit_name_byte, "B", 1.0),
        UnitItem(R.string.unit_name_bit, "bit", 0.125),
        UnitItem(R.string.unit_name_kilobyte, "KB", 1024.0),
        UnitItem(R.string.unit_name_megabyte, "MB", 1048576.0),
        UnitItem(R.string.unit_name_gigabyte, "GB", 1073741824.0),
        UnitItem(R.string.unit_name_terabyte, "TB", 1099511627776.0),
        UnitItem(R.string.unit_name_petabyte, "PB", 1.125899906842624e15),
        UnitItem(R.string.unit_name_kilobyte_si, "kB", 1000.0),
        UnitItem(R.string.unit_name_megabyte_si, "MB(SI)", 1e6),
        UnitItem(R.string.unit_name_gigabyte_si, "GB(SI)", 1e9),
        UnitItem(R.string.unit_name_terabyte_si, "TB(SI)", 1e12),
    )

    // ─── 人民币汇率（基准：人民币 CNY，参考汇率）────────────────────────────
    val CURRENCY = listOf(
        UnitItem(R.string.unit_name_cny, "CNY", 1.0),
        UnitItem(R.string.unit_name_usd, "USD", 7.25),
        UnitItem(R.string.unit_name_eur, "EUR", 7.85),
        UnitItem(R.string.unit_name_gbp, "GBP", 9.15),
        UnitItem(R.string.unit_name_jpy, "JPY", 0.048),
        UnitItem(R.string.unit_name_hkd, "HKD", 0.93),
        UnitItem(R.string.unit_name_krw, "KRW", 0.0052),
        UnitItem(R.string.unit_name_aud, "AUD", 4.65),
        UnitItem(R.string.unit_name_cad, "CAD", 5.30),
        UnitItem(R.string.unit_name_chf, "CHF", 8.10),
        UnitItem(R.string.unit_name_sgd, "SGD", 5.40),
        UnitItem(R.string.unit_name_twd, "TWD", 0.22),
        UnitItem(R.string.unit_name_myr, "MYR", 1.62),
        UnitItem(R.string.unit_name_thb, "THB", 0.21),
    )

    // ─── 电阻（基准：欧姆 Ω） ──────────────────────────────────────────────
    val RESISTANCE = listOf(
        UnitItem(R.string.unit_name_ohm, "Ω", 1.0),
        UnitItem(R.string.unit_name_milliohm, "mΩ", 0.001),
        UnitItem(R.string.unit_name_microohm, "μΩ", 1e-6),
        UnitItem(R.string.unit_name_kiloohm, "kΩ", 1000.0),
        UnitItem(R.string.unit_name_megaohm, "MΩ", 1e6),
        UnitItem(R.string.unit_name_gigaohm, "GΩ", 1e9),
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
