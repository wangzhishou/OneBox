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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.shifenmiao.common.editor

import com.shifenmiao.storage.EditorDraftStore
import javax.inject.Inject
import javax.inject.Singleton

data class EditorDraft(
    val originalText: String,
    val text: String,
)

interface EditorDraftRepository {
    fun load(draftKey: String): EditorDraft?
    fun save(draftKey: String, originalText: String, draftText: String)
    fun clear(draftKey: String)
}

@Singleton
class MmkvEditorDraftRepository @Inject constructor() : EditorDraftRepository {
    override fun load(draftKey: String): EditorDraft? {
        val stored = EditorDraftStore.load(draftKey) ?: return null
        return EditorDraft(originalText = stored.original, text = stored.text)
    }

    override fun save(draftKey: String, originalText: String, draftText: String) {
        EditorDraftStore.save(key = draftKey, original = originalText, text = draftText)
    }

    override fun clear(draftKey: String) {
        EditorDraftStore.clear(draftKey)
    }
}
