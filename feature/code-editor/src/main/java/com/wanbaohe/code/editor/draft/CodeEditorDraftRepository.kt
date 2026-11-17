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

package com.wanbaohe.code.editor.draft

import com.shifenmiao.storage.CodeEditorDraftStore
import javax.inject.Inject
import javax.inject.Singleton

/** 未保存草稿： [originalText] 是进页面时磁盘上的正文，用来判断草稿是否还配得上这个文件。 */
data class CodeEditorDraft(
    val originalText: String,
    val text: String,
)

/**
 * 代码编辑器草稿读写契约。
 *
 * 上层（Component / Screen）只认这个接口，不直接碰 MMKV；
 * 换实现（比如以后换成 Room 或 DataStore）不用改调用方。
 */
interface CodeEditorDraftRepository {
    fun load(draftKey: String): CodeEditorDraft?
    fun save(draftKey: String, originalText: String, draftText: String)
    fun clear(draftKey: String)
}

@Singleton
class MmkvCodeEditorDraftRepository @Inject constructor() : CodeEditorDraftRepository {

    override fun load(draftKey: String): CodeEditorDraft? {
        val stored = CodeEditorDraftStore.load(draftKey) ?: return null
        return CodeEditorDraft(originalText = stored.original, text = stored.text)
    }

    override fun save(draftKey: String, originalText: String, draftText: String) {
        CodeEditorDraftStore.save(key = draftKey, original = originalText, text = draftText)
    }

    override fun clear(draftKey: String) {
        CodeEditorDraftStore.clear(draftKey)
    }
}
