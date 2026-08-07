package com.shifenmiao.model.xiangqi

/**
 * 象棋业务 Service 接口。
 *
 * 供 UI Component 层与 AI AgentTool 层共同依赖的契约。
 * 实现类位于 feature/xiangqi/service/。
 */
interface XiangqiServiceInterface {

    /** 列出所有未归档的对局摘要（按最近对局时间倒序）。 */
    suspend fun listGames(): List<XiangqiGameSummaryDto>

    /** 获取单局详情（含走子记录），不存在返回 null。 */
    suspend fun getGameDetail(gameId: String): XiangqiGameDetailDto?

    /** 创建本地双人对局，返回 gameId。 */
    suspend fun createLocalGame(title: String): Result<String>

    /** 创建人机对局，aiAsRed = true 时 AI 执红。返回 gameId。 */
    suspend fun createAiGame(title: String, aiAsRed: Boolean): Result<String>

    /** 从 FEN 导入棋谱开局，返回 gameId。FEN 不合法抛异常。 */
    suspend fun importFen(title: String, fen: String): Result<String>

    /** 从 JSON 对战记录导入（含回放走子），返回 gameId。JSON 不合法抛异常。 */
    suspend fun importJson(title: String, json: String): Result<String>

    /** 删除对局（归档），返回 Unit 或异常。 */
    suspend fun deleteGame(gameId: String): Result<Unit>

    /** 导出当前局面 FEN，对局不存在返回空串。 */
    suspend fun exportFen(gameId: String): String

    /** 导出完整 JSON 对战记录，对局不存在返回空串。 */
    suspend fun exportJson(gameId: String): String
}
