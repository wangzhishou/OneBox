package com.shifenmiao.model.activity

/**
 * 活动日志分类 — 用户在 App 中所有「写操作」的类型枚举。
 *
 * 每种类型可携带不同的 payload（JSON），在 [ActivityLogEntry.payload] 中序列化。
 * 新增类型只需在这里加一条枚举，再在对应 feature 模块通过 Recorder 写入即可。
 */
enum class ActivityCategory {
    /** AI 对话（单聊、Prompt） */
    AI_CHAT,

    /** AI 对战 / 双人对话 */
    AI_DUEL,

    /** AI 智能体 */
    AI_AGENT,

    /** 图片编辑 / 保存 */
    IMAGE_EDIT,

    /** 文件格式转换（PDF、文档等） */
    FILE_CONVERT,

    /** 笔记编辑 */
    NOTE_EDIT,

    /** HTML 文章编辑 */
    HTML_EDIT,

    /** 音频处理 */
    AUDIO_EDIT,

    /** 博客 / 文章发布 */
    BLOG_POST,

    /** OCR 文档识别 */
    OCR_DOCUMENT,

    /** 记账（新增/编辑/删除/导入/恢复账目） */
    BOOKKEEPING,

    /** 待办清单（新增/编辑/删除/完成/收藏分类和任务） */
    TODO,

    /** 象棋对局（创建/删除/认输/重命名） */
    XIANGQI,

    /** 提词器文稿（创建/更新/删除） */
    TELEPROMPTER,

    /** 习惯打卡（创建习惯/打卡） */
    HABIT,

    /** 中国古诗词（浏览/收藏/解读等操作） */
    POEM,

    /** 其他 / 通用 */
    OTHER;

    companion object {
        fun fromName(name: String): ActivityCategory {
            return entries.find { it.name == name } ?: OTHER
        }
    }
}

