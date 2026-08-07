package com.wanbaohe.code.editor

import com.shifenmiao.database.data_draft.DataDraftHelper

/**
 * 临时存储超长文本数据，避免通过 Intent/Bundle 传递大字符串导致 TransactionTooLargeException。
 * 底层使用 Room 数据库（[DataDraftHelper]）持久化，进程被杀后也能恢复数据。
 *
 * ## CodeEditor 专用草稿类型
 * [DRAFT_TYPE_CODE_EDITOR] = 11，与其他业务草稿（markdown 编辑器 = 10）互不干扰。
 *
 * ## 如何使用纯文本编辑模式 (Edit Mode)
 *
 * 任何 Screen 或 Component 想要调用 CodeEditor 并获取编辑结果，只需以下两步：
 *
 * **1. 启动编辑器**
 * ```kotlin
 * val draftId = CodeEditorDataStore.put(dataDraftHelper, yourHugeString)
 * component.onNavigate(Screen.CodeEditor(editDraftId = draftId, editTitle = "编辑标题"))
 * ```
 *
 * **2. 监听并自动更新结果**
 * 在调用方的 Component 中注册 EventBus 并接收回调：
 * ```kotlin
 * fun onEditorResult(event: EditorResultEvent) {
 *     if (event.editDraftId == myCurrentDraftId) {
 *         updateMyString(event.text)
 *         scope.launch { CodeEditorDataStore.clear(dataDraftHelper, event.editDraftId) }
 *     }
 * }
 * ```
 */
object CodeEditorDataStore {

    /** CodeEditor 专用的草稿类型 */
    const val DRAFT_TYPE_CODE_EDITOR = 11

    /**
     * 存入待编辑或已编辑完成的字符串，返回草稿 ID（数据库自增主键）。
     */
    suspend fun put(dataDraftHelper: DataDraftHelper, text: String): Long {
        return dataDraftHelper.createDraft(
            draftType = DRAFT_TYPE_CODE_EDITOR,
            data = text,
        )
    }

    /**
     * 更新指定草稿的内容
     */
    suspend fun update(dataDraftHelper: DataDraftHelper, draftId: Long, text: String) {
        dataDraftHelper.updateDraft(
            draftId = draftId,
            data = text,
        )
    }

    /**
     * 根据 draftId 获取字符串
     */
    suspend fun get(dataDraftHelper: DataDraftHelper, draftId: Long): String? {
        return dataDraftHelper.getById(draftId)?.data
    }

    /**
     * 移除草稿，释放空间
     */
    suspend fun clear(dataDraftHelper: DataDraftHelper, draftId: Long) {
        dataDraftHelper.deleteById(draftId)
    }
}
