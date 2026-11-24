/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.shifenmiao.marktodo.data

import com.shifenmiao.marktodo.model.TodoCategory
import com.shifenmiao.marktodo.service.MarkTodoSelectionStore
import com.tencent.mmkv.MMKV
import javax.inject.Inject
import javax.inject.Singleton

/** 使用与待办首页相同的 MMKV 命名空间保存最近分组。 */
@Singleton
class MmkvMarkTodoSelectionStore @Inject constructor() : MarkTodoSelectionStore {
    private val prefs by lazy { MMKV.mmkvWithID("marktodo_home") }

    override fun getLastCategoryId(): String? = prefs.decodeString(KEY_LAST_CATEGORY_ID, null)

    override fun setLastCategoryId(categoryId: String?) {
        prefs.encode(KEY_LAST_CATEGORY_ID, categoryId ?: TodoCategory.INBOX_ID)
    }

    private companion object {
        const val KEY_LAST_CATEGORY_ID = "last_selected_category_id"
    }
}
