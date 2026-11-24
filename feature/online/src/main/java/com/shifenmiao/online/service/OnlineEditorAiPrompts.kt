package com.shifenmiao.online.service

import androidx.annotation.StringRes
import com.shifenmiao.common.ai.EditorAiPromptKey
import com.shifenmiao.common.ai.EditorAiPrompts
import com.shifenmiao.database.chat_prompt.SystemPromptRepository
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.online.R
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnlineEditorAiPrompts @Inject constructor(
    private val repository: SystemPromptRepository,
) : EditorAiPrompts {
    override suspend fun get(key: EditorAiPromptKey): String {
        val definition = key.definition
        val fallback = AppContext.getString(definition.contentRes).trim()
        return runCatching {
            repository.getOrCreate(
                key = key.storageKey,
                title = AppContext.getString(definition.titleRes),
                description = AppContext.getString(definition.descriptionRes),
                defaultContent = fallback,
            )
        }.getOrNull()?.prompt?.trim()?.takeIf { it.isNotBlank() } ?: fallback
    }

    override suspend fun ensureDefaults() {
        EditorAiPromptKey.entries.forEach { get(it) }
    }

    private data class Definition(
        @StringRes val titleRes: Int,
        @StringRes val descriptionRes: Int,
        @StringRes val contentRes: Int,
    )

    private val EditorAiPromptKey.storageKey: String
        get() = "editor_${name.lowercase()}"

    private val EditorAiPromptKey.definition: Definition
        get() = when (this) {
            EditorAiPromptKey.NOTE_SYSTEM -> Definition(
                R.string.editor_prompt_note_system_title,
                R.string.editor_prompt_note_system_description,
                R.string.note_ai_system_prompt,
            )
            EditorAiPromptKey.NOTE_FORMAT -> noteAction(
                R.string.note_ai_action_format, R.string.note_ai_prompt_format
            )
            EditorAiPromptKey.NOTE_EXPAND -> noteAction(
                R.string.note_ai_action_expand, R.string.note_ai_prompt_expand
            )
            EditorAiPromptKey.NOTE_POLISH -> noteAction(
                R.string.note_ai_action_polish, R.string.note_ai_prompt_polish
            )
            EditorAiPromptKey.NOTE_CONDENSE -> noteAction(
                R.string.note_ai_action_condense, R.string.note_ai_prompt_condense
            )
            EditorAiPromptKey.NOTE_CHECKLIST -> noteAction(
                R.string.note_ai_action_checklist, R.string.note_ai_prompt_checklist
            )
            EditorAiPromptKey.NOTE_SUMMARY -> noteAction(
                R.string.note_ai_action_summary, R.string.note_ai_prompt_summary
            )
            EditorAiPromptKey.NOTE_METADATA -> Definition(
                R.string.editor_prompt_note_metadata_title,
                R.string.editor_prompt_note_metadata_description,
                R.string.note_ai_metadata_system_prompt,
            )
            EditorAiPromptKey.PROMPT_SYSTEM -> Definition(
                R.string.editor_prompt_prompt_system_title,
                R.string.editor_prompt_prompt_system_description,
                R.string.prompt_ai_system_prompt,
            )
            EditorAiPromptKey.PROMPT_CREATE -> promptAction(
                R.string.prompt_ai_create, R.string.prompt_ai_create_instruction
            )
            EditorAiPromptKey.PROMPT_ENHANCE -> promptAction(
                R.string.prompt_ai_enhance, R.string.prompt_ai_enhance_instruction
            )
            EditorAiPromptKey.PROMPT_FORMAT -> promptAction(
                R.string.prompt_ai_format, R.string.prompt_ai_format_instruction
            )
            EditorAiPromptKey.PROMPT_CONDENSE -> promptAction(
                R.string.prompt_ai_condense, R.string.prompt_ai_condense_instruction
            )
        }

    private fun noteAction(@StringRes titleRes: Int, @StringRes contentRes: Int) = Definition(
        titleRes = titleRes,
        descriptionRes = R.string.editor_prompt_note_action_description,
        contentRes = contentRes,
    )

    private fun promptAction(@StringRes titleRes: Int, @StringRes contentRes: Int) = Definition(
        titleRes = titleRes,
        descriptionRes = R.string.editor_prompt_prompt_action_description,
        contentRes = contentRes,
    )
}
