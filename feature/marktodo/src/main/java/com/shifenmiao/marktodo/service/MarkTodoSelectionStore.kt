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

package com.shifenmiao.marktodo.service

/** 记录新建待办时最近使用的分组。 */
interface MarkTodoSelectionStore {
    /** 返回真实分组 id、[com.shifenmiao.marktodo.model.TodoCategory.INBOX_ID] 或 null（尚无记录）。 */
    fun getLastCategoryId(): String?

    /** 保存分组 id；null 表示收集箱。 */
    fun setLastCategoryId(categoryId: String?)
}
