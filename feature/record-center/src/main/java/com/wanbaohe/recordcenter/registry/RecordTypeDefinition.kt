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

package com.wanbaohe.recordcenter.registry

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 健康记录类型定义 — 每种记录(心率/血压/血糖…)一份,描述其展示信息与字段契约。
 *
 * 新增类型:在 `registry/definitions/` 加一个 object 实现本接口,
 * 再在 `registry/di/RecordTypeModule` 里 @IntoMap 注册即可,
 * UI 与 Agent 工具都经 [RecordTypeCatalog] 动态感知。
 */
interface RecordTypeDefinition {
    /** 类型唯一标识,如 "blood_pressure",落库到 HealthRecordEntity.type */
    val key: String

    /** 类型名称字符串资源 */
    @get:StringRes
    val titleRes: Int

    /** 类型描述字符串资源 */
    @get:StringRes
    val descriptionRes: Int

    /** 列表/目录页展示图标 */
    val icon: ImageVector

    /** 字段定义列表(顺序即表单与图表的展示顺序) */
    val fields: List<RecordField>

    /** 参考范围字符串资源,无参考范围的类型(如体重)为 null */
    @get:StringRes
    val referenceRangeRes: Int?

    /** 趋势图上绘制的字段 key 列表 */
    val chartFieldKeys: List<String>

    /** 趋势聚合页卡片使用的图表形态,默认折线 */
    val trendChartKind: TrendChartKind
        get() = TrendChartKind.LINE

    /** 目录排序权重,越小越靠前 */
    val sortOrder: Int
}

/** 趋势卡片图表形态:折线(连续监测类)/ 柱状(离散测量类) */
enum class TrendChartKind { LINE, BAR }

/**
 * 单个数值字段定义。
 *
 * @param key      字段 key,如 "systolic",作为 fieldsJson 的键
 * @param labelRes 字段名称字符串资源
 * @param unit     展示单位(空串表示无单位,如 BMI)
 * @param required 是否必填(血脂的 ldl/hdl 等为选填)
 * @param min/max  合理范围校验,null 表示不校验该端
 */
data class RecordField(
    val key: String,
    @StringRes val labelRes: Int,
    val unit: String,
    val required: Boolean = true,
    val min: Float? = null,
    val max: Float? = null,
)
