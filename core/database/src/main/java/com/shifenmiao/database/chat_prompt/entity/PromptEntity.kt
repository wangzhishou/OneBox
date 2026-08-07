package com.shifenmiao.database.chat_prompt.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.shifenmiao.model.Source

/**
 * PROMPT 类型条目的资源行。
 * 通过 [com.shifenmiao.database.item.entity.ItemPromptLink] 与 item 建立 1:1 关联。
 *
 * placeholder 字段语义：
 * - 非空 = 系统预置 key（用于 [isSystemPreset]）
 * - null  = 远端 / 本地用户 prompt
 */
@Entity(
    tableName = "item_prompt",
    indices = [
        Index(value = ["source", "remote_id"], unique = true),
    ]
)
data class PromptEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "remote_id") val remoteId: Int? = null,
    @ColumnInfo(name = "source", defaultValue = "0") val source: Source = Source.REMOTE,
    val title: String = "",
    val description: String? = null,
    val prompt: String? = null,
    val placeholder: String? = null,
    val templates: String? = null,
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
) {
    companion object {
        /** 系统预置标识：AI 助手默认提示词 */
        const val SYSTEM_PROMPT_KEY_DEFAULT_PROMPT = "system_default_prompt"
        /** 系统预置标识：AI Agent生成器提示词 */
        const val SYSTEM_PROMPT_KEY_AGENT_CREATE = "system_agent_create"
        /** 系统预置标识：AI ChatPrompt生成器提示词 */
        const val SYSTEM_PROMPT_KEY_CHAT_PROMPT_CREATE = "system_prompt_create"
        /** 系统预置标识：八字命盘解析提示词 */
        const val SYSTEM_PROMPT_KEY_BAZI = "system_bazi"
        /** 系统预置标识：中国象棋走子提示词 */
        const val SYSTEM_PROMPT_KEY_XIANGQI_MOVE = "system_xiangqi_move"
        /** 系统预置标识：ASK 仅问答模式执行协议 */
        const val SYSTEM_PROMPT_KEY_WORKING_MODE_ASK = "system_working_mode_ask"
        /** 系统预置标识：PLAN 仅规划模式执行协议 */
        const val SYSTEM_PROMPT_KEY_WORKING_MODE_PLAN = "system_working_mode_plan"
        /** 系统预置标识：AGENT 执行模式执行协议 */
        const val SYSTEM_PROMPT_KEY_WORKING_MODE_AGENT = "system_working_mode_agent"
        /** 系统预置标识：AI 对聊/互动 prompt 模板集合 */
        const val SYSTEM_PROMPT_KEY_DUEL_TEMPLATES = "system_duel_templates"
    }

    fun isSystemPreset(): Boolean = source == Source.SYSTEM
}
