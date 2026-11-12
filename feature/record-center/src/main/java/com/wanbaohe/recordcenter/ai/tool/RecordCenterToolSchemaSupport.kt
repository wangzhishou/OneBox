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

package com.wanbaohe.recordcenter.ai.tool

import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.wanbaohe.recordcenter.R
import com.wanbaohe.recordcenter.registry.RecordTypeCatalog
import com.wanbaohe.recordcenter.registry.RecordTypeDefinition

/**
 * 记录中心工具的动态 schema 支撑:
 * record_type 的 enum 与工具描述都由 [RecordTypeCatalog] 在运行时拼装,
 * 新增类型(注册进 catalog)后工具自动感知,无需改工具代码。
 */
internal class RecordCenterToolSchemaSupport(
    private val catalog: RecordTypeCatalog,
    private val textProvider: AgentToolTextProvider,
) {

    /** record_type 参数的动态枚举值 */
    fun recordTypeEnum(): List<String> = catalog.all().map { it.key }

    /**
     * 逐类型列出字段契约,拼在工具描述后面,例如:
     * "blood_pressure(Blood Pressure): systolic=Systolic (mmHg), diastolic=Diastolic (mmHg)"
     */
    fun buildTypeFieldsDescription(): String {
        return catalog.all().joinToString("\n") { definition ->
            val fields = definition.fields.joinToString(", ") { field ->
                val label = textProvider.string(field.labelRes)
                val unit = field.unit.takeIf { it.isNotBlank() }?.let { " ($it)" }.orEmpty()
                val optional = if (field.required) {
                    ""
                } else {
                    textProvider.string(R.string.agent_tool_record_center_field_optional)
                }
                "${field.key}=$label$unit$optional"
            }
            "- ${definition.key} (${textProvider.string(definition.titleRes)}): $fields"
        }
    }

    fun requireType(key: String): RecordTypeDefinition {
        return catalog.byKey(key)
            ?: error(textProvider.string(R.string.record_center_error_unknown_type, key))
    }
}
