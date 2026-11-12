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

package com.wanbaohe.markdown.edit

import com.shifenmiao.database.data_draft.DataDraftHelper

/**
 * 临时存储超长文本数据，避免通过 Intent/Bundle 传递大字符串导致 TransactionTooLargeException。
 * 底层使用 Room 数据库（[DataDraftHelper]）持久化，进程被杀后也能恢复数据。
 *
 * ## Markdown 编辑器的草稿类型
 * [DRAFT_TYPE_MARKDOWN_EDITOR] = 10，与其他业务草稿互不干扰。
 *
 * ## 如何使用纯文本编辑模式 (Edit Mode)
 *
 * 任何其他 Screen 或 Component 想要调用 MarkdownEditor 并获取编辑结果，只需以下两步：
 *
 * **1. 启动编辑器**
 * ```kotlin
 * // 1. 将大文本存入数据库草稿，拿到唯一 draftId
 * val draftId = EditorDataStore.put(dataDraftHelper, yourHugeString)
 *
 * // 2. 发起导航，跳转到 MarkdownEditorScreen
 * component.onNavigate(Screen.MarkdownEditor(editDraftId = draftId, editTitle = "编辑标题"))
 * ```
 *
 * **2. 监听并自动更新结果**
 * 在调用方的 Component 中注册 EventBus 并接收回调：
 * ```kotlin
 * @Subscribe(threadMode = ThreadMode.MAIN)
 * fun onEditorResult(event: EditorResultEvent) {
 *     if (event.editDraftId == myCurrentDraftId) {
 *         // 更新原有字符串
 *         updateMyString(event.text)
 *
 *         // （可选）清理数据库草稿，释放空间
 *         scope.launch { EditorDataStore.clear(dataDraftHelper, event.editDraftId) }
 *     }
 * }
 * ```
 */
object EditorDataStore {

    /** Markdown 编辑器专用的草稿类型 */
    const val DRAFT_TYPE_MARKDOWN_EDITOR = 10

    /**
     * 存入待编辑或已编辑完成的字符串，返回草稿 ID（数据库自增主键）。
     */
    suspend fun put(dataDraftHelper: DataDraftHelper, text: String): Long {
        return dataDraftHelper.createDraft(
            draftType = DRAFT_TYPE_MARKDOWN_EDITOR,
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
