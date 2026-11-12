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

import androidx.annotation.StringRes
import com.wanbaohe.unitconverter.R

/** 换算类别枚举 */
enum class UnitCategory(
    @StringRes val labelRes: Int,
    /** 导航/深链兼容别名（枚举名也会参与匹配） */
    val aliases: List<String> = emptyList(),
) {
    Length(R.string.unit_cat_length, aliases = listOf("长度")),
    Volume(R.string.unit_cat_volume, aliases = listOf("体积", "體積")),
    Mass(R.string.unit_cat_mass, aliases = listOf("质量", "質量")),
    Temperature(R.string.unit_cat_temperature, aliases = listOf("温度", "溫度")),
    Area(R.string.unit_cat_area, aliases = listOf("面积", "面積")),
    Pressure(R.string.unit_cat_pressure, aliases = listOf("压力", "壓力")),
    Power(R.string.unit_cat_power, aliases = listOf("功率")),
    Energy(R.string.unit_cat_energy, aliases = listOf("功、能和热量", "功、能和熱量")),
    Force(R.string.unit_cat_force, aliases = listOf("力")),
    Time(R.string.unit_cat_time, aliases = listOf("时间", "時間")),
    Speed(R.string.unit_cat_speed, aliases = listOf("速度")),
    Angle(R.string.unit_cat_angle, aliases = listOf("角度")),
    Density(R.string.unit_cat_density, aliases = listOf("密度")),
    DataStorage(R.string.unit_cat_data_storage, aliases = listOf("数据存储", "資料儲存")),
    Currency(R.string.unit_cat_currency, aliases = listOf("人民币", "人民幣")),
    Resistance(R.string.unit_cat_resistance, aliases = listOf("电阻", "電阻")),
}
