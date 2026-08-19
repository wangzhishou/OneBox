package com.wanbaohe.dsh.session

import com.wanbaohe.dsh.connection.DshApiClient
import com.wanbaohe.dsh.wire.DshJson
import com.wanbaohe.dsh.wire.model.SkillEntry
import com.wanbaohe.dsh.wire.model.SkillListValue
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.put

/**
 * skill 目录(DSH-PROTOCOL §5)。
 *
 * skill 无专线调用:目录由 skill.list 一次拉取并缓存;调用 = 内容恰好是单个
 * "/" 开头文本块的普通 prompt(见 [promptFor])。
 * 生命周期与 ChatBundle 绑定(dispose 空操作,保持一致形态)。
 */
class SkillCatalog(
    private val api: DshApiClient
) {

    @Volatile
    private var cache: List<SkillEntry>? = null

    /**
     * 拉取技能目录(带缓存,force 强制重拉)。
     * sessionId 必填:主机从会话头解析项目根目录,缺席直接 bad-request。
     */
    suspend fun list(sessionId: String, force: Boolean = false): List<SkillEntry> {
        cache?.let { if (!force) return it }
        val payload = buildJsonObject { put("sessionId", sessionId) }
        val value = DshJson.decodeFromJsonElement<SkillListValue>(api.call(RpcSkillList, payload))
        cache = value.skills
        return value.skills
    }

    fun dispose() = Unit

    companion object {
        private const val RpcSkillList = "skill.list"

        /** 生成调用某 skill 的 prompt 文本(斜杠命令 = 单个 "/" 开头文本块) */
        fun promptFor(name: String, args: String? = null): String =
            if (args.isNullOrEmpty()) "/$name" else "/$name $args"
    }
}
