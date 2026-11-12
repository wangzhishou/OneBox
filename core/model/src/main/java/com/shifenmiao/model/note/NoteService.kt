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
