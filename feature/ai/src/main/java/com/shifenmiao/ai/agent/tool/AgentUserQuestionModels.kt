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

package com.shifenmiao.ai.agent.tool

import kotlinx.serialization.Serializable

enum class AgentUserQuestionPresentation {
    dialog,
    bottom_sheet
}

/**
 * 单道题的输入类型。
 *
 * - [text]：纯文本输入（默认）。
 * - [time]：点击右侧图标唤起时间选择器，结果以 "HH:mm" 写入。
 * - [time_range]：点击右侧图标唤起时间段选择器，结果以 "HH:mm ~ HH:mm" 写入。
 * - [date]：点击右侧图标唤起日期选择器，结果以 "yyyy-MM-dd" 写入。
 * - [date_range]：点击右侧图标唤起日期段选择器，结果以 "yyyy-MM-dd ~ yyyy-MM-dd" 写入。
 * - [color]：点击右侧图标唤起颜色选择器，结果以 "#AARRGGBB" 写入（不含 alpha 时为 "#RRGGBB"）。
 * - [city]：点击右侧图标唤起城市选择器，结果以 "省份 城市 区县" 写入。
 * - [image]：点击右侧图标唤起系统图片选择器；结果自动转写为 file:// URI（避免 SAF 权限过期）。
 * - [file]：点击右侧图标唤起系统文件选择器；结果自动转写为 file:// URI。
 * - [folder]：点击右侧图标唤起系统目录选择器；结果自动转写为 file:// URI。
 */
enum class AgentQuestionType {
    text,
    time,
    time_range,
    date,
    date_range,
    color,
    city,
    image,
    file,
    folder
}

@Serializable
data class AgentUserQuestionItem(
    val name: String = "",
    val header: String = "",
    val question: String = "",
    val required: Boolean = false,
    val options: List<AgentUserQuestionOption> = emptyList(),
    val multiSelect: Boolean = false,
    val placeholder: String = "",
    val multiline: Boolean = false,
    val type: AgentQuestionType = AgentQuestionType.text,
) {
    val isChoiceQuestion: Boolean
        get() = options.isNotEmpty()

    val isSpecializedField: Boolean
        get() = type != AgentQuestionType.text
}

@Serializable
data class AgentUserQuestionOption(
    val label: String = "",
    val value: String = ""
)
