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
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.shifenmiao.online.component

import androidx.annotation.StringRes
import com.shifenmiao.online.R

/** AI 结果写回笔记的方式：整篇替换 / 追加到末尾 / 插入到开头。 */
enum class NoteAiApplyMode {
    Replace,
    Append,
    Prepend,
}

/** 笔记 AI 写作的预设动作：菜单、面板 chips 与提示词共用一套定义。 */
enum class NoteAiAction(
    @StringRes val labelRes: Int,
    @StringRes val promptRes: Int,
    val applyMode: NoteAiApplyMode,
) {
    Format(
        labelRes = R.string.note_ai_action_format,
        promptRes = R.string.note_ai_prompt_format,
        applyMode = NoteAiApplyMode.Replace,
    ),
    Expand(
        labelRes = R.string.note_ai_action_expand,
        promptRes = R.string.note_ai_prompt_expand,
        applyMode = NoteAiApplyMode.Replace,
    ),
    Polish(
        labelRes = R.string.note_ai_action_polish,
        promptRes = R.string.note_ai_prompt_polish,
        applyMode = NoteAiApplyMode.Replace,
    ),
    Condense(
        labelRes = R.string.note_ai_action_condense,
        promptRes = R.string.note_ai_prompt_condense,
        applyMode = NoteAiApplyMode.Replace,
    ),
    Checklist(
        labelRes = R.string.note_ai_action_checklist,
        promptRes = R.string.note_ai_prompt_checklist,
        applyMode = NoteAiApplyMode.Append,
    ),
    Summary(
        labelRes = R.string.note_ai_action_summary,
        promptRes = R.string.note_ai_prompt_summary,
        applyMode = NoteAiApplyMode.Prepend,
    ),
}

/**
 * 笔记 AI 写作面板的完整状态。
 *
 * 请求在 [CreateNoteComponent] 的生命周期里执行（配合 [com.shifenmiao.base.longtask.LongTaskCenter] 前台保活），
 * 退出后台再回来仍能拿到流式结果；面板只负责展示与把结果写回编辑器。
 */
data class NoteAiUiState(
    val visible: Boolean = false,
    val action: NoteAiAction = NoteAiAction.Format,
    val prompt: String = "",
    val originalMarkdown: String = "",
    val result: String = "",
    val isGenerating: Boolean = false,
    val error: String? = null,
    val tokens: Int = 0,
)
