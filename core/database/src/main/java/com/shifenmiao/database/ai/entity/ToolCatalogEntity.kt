package com.shifenmiao.database.ai.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 工具目录的持久化快照表.
 *
 * 关注点分离:
 * - 运行时查询 (首轮 tools 选取 / 工具中心列表) 走 [com.shifenmiao.ai.agent.tool.AgentToolRegistry]
 *   (in-memory, 编译期权威, Step 1 缓存命中).
 * - 持久化 / 导出 / 导入 / 备份走本表, 由
 *   [com.shifenmiao.ai.agent.tool.ToolCatalogRepository] 读写.
 *
 * 数据来源 ([source] 字段):
 * - [SOURCE_BUILT_IN]: 由 [ToolCatalogRepository.snapshotFromRegistry] 从 in-memory 写入,
 *   包含当前 app 版本内置的全部工具元数据. 供 export 用.
 * - [SOURCE_IMPORTED]: 由 [ToolCatalogRepository.importFromJson] 写入, 来自用户导入的 JSON.
 *   可作为 "已导入工具" 列表展示, 也可被 [ToolCatalogRepository.clearImported] 一键清除.
 *
 * Schema 注意点 (与历史上 v1 实现的差异):
 * - 集合字段 (keywords / examples / dependencies / bootstrapModes) 全部以 JSON 数组存储,
 *   避免历史上 [enabledByDefault: Boolean] 那种有损压缩. bootstrapModes 必须能区分 per-mode 信息
 *   (例如 `navigate_app_screen` 只在 AGENT 模式 bootstrap, 不能退化为 "所有模式都 bootstrap").
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
