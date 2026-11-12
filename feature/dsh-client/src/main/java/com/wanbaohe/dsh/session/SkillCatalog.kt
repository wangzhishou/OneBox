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

package com.wanbaohe.dsh.session

import com.wanbaohe.dsh.connection.DshApiClient
import com.wanbaohe.dsh.wire.DshEndpoints
import com.wanbaohe.dsh.wire.DshJson
import com.wanbaohe.dsh.wire.model.SkillEntry
import com.wanbaohe.dsh.wire.model.SkillListRequest
import com.wanbaohe.dsh.wire.model.SkillListValue
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * skill 目录(DSH 0.1.5-rc.2,`skills/list`)。
 *
 * skill 无专线调用:目录一次拉取并按会话缓存;调用 = 内容恰好是单个 "/" 开头
 * 文本块的普通 prompt(见 [promptFor])。
 *
 * 与旧版(<= 0.1.1)的差异:
 * - 端点从 `skill.list` 改为 [DshEndpoints.SKILLS_LIST](命名空间带 s)
 * - args 从裸 `{sessionId}` 改成 `{request:{sessionId}}`
 *   (harness:`packages/api/session-controller/src/skill-catalog.ts` 的
 *   `list(request: SkillListRequest, signal)` → `SkillListValue`),回值模型
 *   已对齐(wire/model/Skill.kt)
 * - 调用改成 [DshApiClient.callRemote](旧版 `call` 拿的是原始信封 value)
 * - 缓存按 sessionId 分桶:主机从会话头解析项目根目录,同一进程里不同会话的技能集
 *   可以不同,旧版单槽缓存会互相污染
 * - 目录失效不在这里订阅事件(本类不持有连接):[CommandStore] 在
 *   `commands/change` / `agent-preset/selected` / 重连代际翻转时调用 [invalidate]
 *
 * 生命周期与 ChatBundle 绑定(dispose 空操作,保持一致形态)。
 */
class SkillCatalog(
    private val api: DshApiClient
) {

    /** sessionId → 技能目录(主机按会话头解析项目根目录,技能集随会话/项目变) */
    private val cache = HashMap<String, List<SkillEntry>>()

    /**
     * 拉取技能目录(带按会话缓存,force 强制重拉)。
     * sessionId 必填:主机从会话头解析项目根目录,缺席直接 bad-request。
     */
    suspend fun list(sessionId: String, force: Boolean = false): List<SkillEntry> {
        if (!force) {
            cache[sessionId]?.let { return it }
        }
        val args = buildJsonObject {
            put(
                "request",
                DshJson.encodeToJsonElement(
                    SkillListRequest.serializer(),
                    SkillListRequest(sessionId = sessionId)
                )
            )
        }
        val value = DshJson.decodeFromJsonElement(
            SkillListValue.serializer(),
            api.callRemote(DshEndpoints.SKILLS_LIST, args)
        )
        cache[sessionId] = value.skills
        return value.skills
    }

    /** 丢弃技能目录缓存:sessionId 为 null 丢全部(重连/preset 帧没带会话时);由 [CommandStore] 调用 */
    fun invalidate(sessionId: String? = null) {
        if (sessionId == null) {
            cache.clear()
        } else {
            cache.remove(sessionId)
        }
    }

    fun dispose() = Unit

    companion object {
        /** 生成调用某 skill 的 prompt 文本(斜杠命令 = 单个 "/" 开头文本块) */
        fun promptFor(name: String, args: String? = null): String =
            if (args.isNullOrEmpty()) "/$name" else "/$name $args"
    }
}
