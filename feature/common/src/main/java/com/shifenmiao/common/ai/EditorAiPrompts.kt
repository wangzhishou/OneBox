package com.shifenmiao.common.ai

enum class EditorAiPromptKey {
    NOTE_SYSTEM,
    NOTE_FORMAT,
    NOTE_EXPAND,
    NOTE_POLISH,
    NOTE_CONDENSE,
    NOTE_CHECKLIST,
    NOTE_SUMMARY,
    NOTE_METADATA,
    PROMPT_SYSTEM,
    PROMPT_CREATE,
    PROMPT_ENHANCE,
    PROMPT_FORMAT,
    PROMPT_CONDENSE,
}

interface EditorAiPrompts {
    suspend fun get(key: EditorAiPromptKey): String
    suspend fun ensureDefaults()
}
