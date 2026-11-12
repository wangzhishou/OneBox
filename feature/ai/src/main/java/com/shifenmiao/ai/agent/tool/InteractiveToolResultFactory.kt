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

import com.shifenmiao.model.ModelProvider
import com.shifenmiao.model.jsonStringOf
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object InteractiveToolResultFactory {

    fun buildQuestionSubmittedResult(
        answersJson: String
    ): AgentToolResult {
        // answersJson 结构由提问方自定义,原样透传嵌入,不重解析字段
        val answers = runCatching { ModelProvider.AppJson.parseToJsonElement(answersJson) }
            .getOrDefault(buildJsonObject { })
        return AgentToolResult(
            content = jsonStringOf(
                mapOf(
                    "status" to "submitted",
                    "answers" to answers
                )
            ),
            isError = false
        )
    }

    fun buildQuestionCancelledResult(): AgentToolResult {
        return AgentToolResult(
            content = jsonStringOf(
                mapOf(
                    "status" to "cancelled"
                )
            ),
            isError = false
        )
    }

    fun buildConfirmationRejectedResult(
        toolName: String,
        reason: String
    ): AgentToolResult {
        return AgentToolResult(
            content = jsonStringOf(
                mapOf(
                    "toolName" to toolName,
                    "decision" to "rejected",
                    "executed" to false,
                    "message" to "用户拒绝了本次工具执行",
                    "reason" to reason
                )
            ),
            isError = false
        )
    }

    /**
     * 判定确认弹窗结果是否为"已批准".
     *
     * 确认结果的实际 JSON 结构由发起方 payload 决定
     * (见 AgentLoopExecutor.requestToolConfirmation):
     * - 批准: `{"decision":"approved"}`
     * - 拒绝: `{"decision":"rejected"}`
     *
     * 解析失败、结构异常或字段缺失一律按"未批准"处理 (安全方向);
     * 同时兼容历史上可能出现过的 `{"approved":true}` 形式.
     */
    fun isConfirmationApproved(payload: String?): Boolean {
        if (payload.isNullOrBlank()) return false
        return runCatching {
            val obj = ModelProvider.AppJson.parseToJsonElement(payload).jsonObject
            obj["decision"]?.jsonPrimitive?.content == "approved" ||
                obj["approved"]?.jsonPrimitive?.booleanOrNull == true
        }.getOrDefault(false)
    }
}
