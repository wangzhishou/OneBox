package com.shifenmiao.model

enum class HomeTabKey(val slug: String) {
    TEXT("text"),
    APP("app"),
    AGENT("agent"),
    PROMPT("prompt"),
    WEB("web"),
    BLOG("blog");

    companion object {
        fun fromSlug(slug: String?): HomeTabKey? {
            if (slug.isNullOrBlank()) return null
            val normalized = slug.trim().lowercase()
            return entries.firstOrNull { it.slug == normalized }
        }
    }
}
