package com.shifenmiao.database.ai.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 工具目录的持久化快照表 (**遗留表, 当前无读写方**).
 *
 * 历史上由 ToolCatalogRepository (已删除) 负责快照 / 导出 / 导入, 运行时查询一直走
 * [com.shifenmiao.ai.agent.tool.AgentToolRegistry] (in-memory, 编译期权威).
 * 该仓库从未接入任何 UI / 同步链路, 删除后本表成为孤儿.
 *
 * 保留本实体而不 DROP 表的原因: 删除 Room 实体需要一次数据库迁移
 * (DROP TABLE + 版本号升级), 而项目没有数据库测试可验证迁移正确性;
 * 表内数据无害且体积小, 留待下次必须升级 schema 时一并清理.
 *
 * Schema 注意点 (与历史上 v1 实现的差异):
 * - 集合字段 (keywords / examples / dependencies / bootstrapModes) 全部以 JSON 数组存储,
 *   避免历史上 [enabledByDefault: Boolean] 那种有损压缩.
 *   注: 聊天工作模式 (Ask/Plan/Agent) 已删除并收敛为单一 Agent 模式, 运行时工具模型的
 *   bootstrapModes 已降级为 `bootstrap: Boolean`; 本表为孤儿遗留表, 列保持不动以避免 Room 迁移,
 *   bootstrap_modes_json 列不再反映运行时语义.
 * - 工具元数据 (name/title/.../version) 全部是 [com.shifenmiao.model.ai.tool.ToolCatalogItem]
 *   的无损镜像, 导出/导入必须 1:1 往返.
 */
@Entity(
    tableName = "tool_catalog",
    indices = [
        Index(value = ["category"]),
        Index(value = ["source"]),
    ]
)
data class ToolCatalogEntity(
    @PrimaryKey
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "summary")
    val summary: String,
    @ColumnInfo(name = "description")
    val description: String,
    @ColumnInfo(name = "category")
    val category: String,
    @ColumnInfo(name = "keywords_json")
    val keywordsJson: String = "[]",
    @ColumnInfo(name = "examples_json")
    val examplesJson: String = "[]",
    @ColumnInfo(name = "dependencies_json")
    val dependenciesJson: String = "[]",
    @ColumnInfo(name = "bootstrap_modes_json")
    val bootstrapModesJson: String = "[]",
    @ColumnInfo(name = "visible_to_user")
    val visibleToUser: Boolean = true,
    @ColumnInfo(name = "requires_confirmation")
    val requiresConfirmation: Boolean = false,
    @ColumnInfo(name = "is_interactive")
    val isInteractive: Boolean = false,
    @ColumnInfo(name = "risk_level")
    val riskLevel: String = "SAFE",
    @ColumnInfo(name = "sort_order")
    val sortOrder: Int = 0,
    @ColumnInfo(name = "version")
    val version: Int = 1,
    @ColumnInfo(name = "source")
    val source: String = SOURCE_BUILT_IN,
    @ColumnInfo(name = "imported_at")
    val importedAt: Long? = null,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),
) {
    companion object {
        const val SOURCE_BUILT_IN = "BUILT_IN"
        const val SOURCE_IMPORTED = "IMPORTED"
    }
}
