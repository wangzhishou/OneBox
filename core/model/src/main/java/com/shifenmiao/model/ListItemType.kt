package com.shifenmiao.model

import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.core.R


enum class ListItemType(val id: Int, val title: String, val description: String) {
    NORMAL(
        1,
        AppContext.getString(R.string.type_default),
        "Default Description"
    ),
    HTML(
        2,
        AppContext.getString(R.string.type_web),
        "Web Description"
    ),
    WECHAT(
        5,
        AppContext.getString(R.string.type_wechat_miniprogram),
        "WeChat Description"
    ),
    VIDEO(
        3,
        AppContext.getString(R.string.type_wechat_video),
        "Video Description"
    ),
    PROMPT(
        4,
        AppContext.getString(R.string.type_prompt),
        "Prompt Description"
    ),
    AGENT(
        6,
        AppContext.getString(R.string.type_agent),
        "Agent Description"
    ),
    BLOG(
        7,
        AppContext.getString(R.string.type_blog),
        "Blog Description"
    ),
    NOTE(
        8,
        AppContext.getString(R.string.item_note_title),
        "Note Description"
    );
    companion object {
        fun fromId(id: Int?): ListItemType? {
            return entries.find { it.id == id }
        }
    }
}