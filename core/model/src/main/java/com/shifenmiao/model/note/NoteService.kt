package com.shifenmiao.model.note

/**
 * 笔记服务接口 - 跨模块使用
 *
 * 工具和页面都调用此接口，保证逻辑一致性。
 * 实现类在 feature/online 模块中。
 */
interface NoteService {
    /**
     * 保存笔记（创建或更新）
     *
     * existingItemId 为 null 时创建新笔记，非 null 时更新已有笔记。
     * 成功后自动记录活动日志。
     *
     * @param params 保存参数
     * @return 保存结果
     */
    suspend fun saveNote(params: NoteSaveParams): NoteResult

    /**
     * 获取笔记详情
     *
     * @param itemId 笔记 ID
     * @return 笔记详情，如果不存在返回 null
     */
    suspend fun getNoteById(itemId: Int): NoteDetail?

    /**
     * 搜索笔记
     *
     * @param query 搜索关键词
     * @return 笔记摘要列表
     */
    suspend fun searchNotes(query: String): List<NoteSummary>

}
