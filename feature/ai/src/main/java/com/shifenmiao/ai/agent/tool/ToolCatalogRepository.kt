package com.shifenmiao.ai.agent.tool

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.shifenmiao.database.ai.dao.ToolCatalogDao
import com.shifenmiao.database.ai.entity.ToolCatalogEntity
import com.shifenmiao.model.ai.tool.ChatWorkingMode
import com.shifenmiao.model.ai.tool.ToolCatalogItem
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import com.shifenmiao.storage.AppSharedStorage
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 工具目录快照 / 导入 / 导出仓库.
 *
 * 关注点分离:
 * - **运行时** (首轮 tools 选取 / 工具中心列表 / 元数据查询) 走 [AgentToolRegistry] (in-memory,
 *   Step 1 缓存命中). 本仓库**绝不**进入运行时热路径.
 * - **持久化** (快照 / 导出 / 导入 / 备份) 走本仓库, 表是 [ToolCatalogEntity].
 *
 * 数据流:
 * ```
 *   AgentToolRegistry (in-memory, 代码级权威)
 *         │  snapshotFromRegistry()   ─▶  tool_catalog 表 (BUILT_IN)
 *         │                                │
 *         │  exportToJson()             ▼
 *         └──────────────────────▶   JSON  ◀──── importFromJson()
 *                                              │
 *                                              ▼
 *                                       tool_catalog (IMPORTED)
 * ```
 *
 * 用途矩阵 (供 UI 层 / 同步模块参考):
 * - "导出我的工具集" → [ensureSnapshot] + [exportToJson]
 * - "导入朋友的工具集" → [importFromJson] (UI 需提示 "导入后无执行器, 仅展示")
 * - "展示我导入的工具" → [getImportedTools]
 * - "重置/清空导入" → [clearImported]
 * - "完全清空" → [clearAll] (慎用, 包含 BUILT_IN 快照)
 */
@Singleton
class ToolCatalogRepository @Inject constructor(
    private val dao: ToolCatalogDao,
    private val registry: AgentToolRegistry,
    private val gson: Gson,
) {

    /**
     * 把当前 in-memory [AgentToolRegistry] 写入 BUILT_IN 记录.
     *
     * - 幂等: 重复调用覆盖既有 BUILT_IN 记录.
     * - 不影响 IMPORTED 记录.
     * - 同步把 [AgentToolRegistry.getCatalogVersion] 写入 [AppSharedStorage],
     *   供 [ensureSnapshot] 判断是否需要重写.
     */
    suspend fun snapshotFromRegistry() {
        val now = System.currentTimeMillis()
        val entities = registry.getToolCatalogItems().map { item ->
            item.toEntity(
                source = ToolCatalogEntity.SOURCE_BUILT_IN,
                importedAt = null,
                updatedAt = now
            )
        }
        dao.upsertAll(entities)
        AppSharedStorage.saveToolCatalogSnapshotVersion(registry.getCatalogVersion())
    }

    /**
     * 智能快照: 仅当 in-memory 版本号变化时执行.
     *
     * 适合在 app 启动或首屏加载时调用, 不会重复写盘. 冷启动首调一次, 之后仅在
     * [AgentToolRegistry.getCatalogVersion] 变化 (工具实现变更 / 新增 / 升级) 时再写.
     */
    suspend fun ensureSnapshot() {
        val currentVersion = registry.getCatalogVersion()
        val storedVersion = AppSharedStorage.loadToolCatalogSnapshotVersion()
        if (currentVersion != storedVersion) {
            snapshotFromRegistry()
        }
    }

    /**
     * 导出整个目录 (BUILT_IN + IMPORTED) 为 JSON 字符串.
     * 用于备份 / 跨设备同步 / 分享.
     */
    suspend fun exportToJson(): String {
        val items = dao.getAll().map(::entityToModel)
        return gson.toJson(items)
    }

    /**
     * 导入 JSON, 全部标记为 IMPORTED.
     *
     * - 同名 IMPORTED 记录会被新内容替换.
     * - 不影响 BUILT_IN 记录.
     * - 字段校验: 解析失败抛 [com.google.gson.JsonSyntaxException] 等, 由调用方处理.
     * - 导入的工具在当前 app 版本下没有执行器, 仅作为元数据展示; 后续如果引入 Step 2
     *   的 [com.shifenmiao.ai.agent.tool.ToolExecutor] 架构, 可为 IMPORTED 项挂接脚本解释器.
     */
    suspend fun importFromJson(json: String): ImportResult {
        val type = object : TypeToken<List<ToolCatalogItem>>() {}.type
        val items: List<ToolCatalogItem> = gson.fromJson(json, type)
        val now = System.currentTimeMillis()
        val entities = items.map { item ->
            item.toEntity(
                source = ToolCatalogEntity.SOURCE_IMPORTED,
                importedAt = now,
                updatedAt = now
            )
        }
        dao.upsertAll(entities)
        return ImportResult(imported = entities.size)
    }

    /** 列出已导入工具 (UI: "我导入的工具" 列表). */
    suspend fun getImportedTools(): List<ToolCatalogItem> =
        dao.getBySource(ToolCatalogEntity.SOURCE_IMPORTED).map(::entityToModel)

    /** 列出全部 (BUILT_IN + IMPORTED). 供 export / 调试用. */
    suspend fun getAll(): List<ToolCatalogItem> =
        dao.getAll().map(::entityToModel)

    suspend fun getByName(name: String): ToolCatalogItem? =
        dao.getByName(name)?.let(::entityToModel)

    /** 清除所有 IMPORTED 记录. BUILT_IN 快照保留. */
    suspend fun clearImported() {
        dao.deleteBySource(ToolCatalogEntity.SOURCE_IMPORTED)
    }

    /** 清除全部 (包括 BUILT_IN 快照). 慎用, 会丢失当前所有持久化数据. */
    suspend fun clearAll() {
        dao.clearAll()
        AppSharedStorage.saveToolCatalogSnapshotVersion(0)
    }

    // ── 转换器: ToolCatalogItem ⇄ ToolCatalogEntity ─────────────────────────

    private fun ToolCatalogItem.toEntity(
        source: String,
        importedAt: Long?,
        updatedAt: Long,
    ): ToolCatalogEntity = ToolCatalogEntity(
        name = name,
        title = title,
        summary = summary,
        description = description,
        category = category.name,
        keywordsJson = gson.toJson(keywords),
        examplesJson = gson.toJson(examples),
        dependenciesJson = gson.toJson(dependencies),
        bootstrapModesJson = gson.toJson(bootstrapModes.map { it.name }),
        visibleToUser = visibleToUser,
        requiresConfirmation = requiresConfirmation,
        isInteractive = isInteractive,
        riskLevel = riskLevel.name,
        sortOrder = sortOrder,
        version = version,
        source = source,
        importedAt = importedAt,
        updatedAt = updatedAt,
    )

    private fun entityToModel(entity: ToolCatalogEntity): ToolCatalogItem = ToolCatalogItem(
        name = entity.name,
        title = entity.title,
        summary = entity.summary,
        description = entity.description,
        category = runCatching { ToolCategory.valueOf(entity.category) }
            .getOrDefault(ToolCategory.SYSTEM),
        keywords = decodeStringList(entity.keywordsJson),
        examples = decodeStringList(entity.examplesJson),
        dependencies = decodeStringList(entity.dependenciesJson),
        bootstrapModes = decodeStringList(entity.bootstrapModesJson)
            .mapNotNull { runCatching { ChatWorkingMode.valueOf(it) }.getOrNull() }
            .toSet(),
        visibleToUser = entity.visibleToUser,
        requiresConfirmation = entity.requiresConfirmation,
        isInteractive = entity.isInteractive,
        riskLevel = runCatching { ToolRiskLevel.valueOf(entity.riskLevel) }
            .getOrDefault(ToolRiskLevel.SAFE),
        sortOrder = entity.sortOrder,
        version = entity.version,
    )

    private fun decodeStringList(json: String): List<String> {
        if (json.isBlank()) return emptyList()
        return try {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson<List<String>>(json, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    data class ImportResult(val imported: Int)
}
