package com.wanbaohe.poem.model

import com.google.gson.annotations.SerializedName
import com.shifenmiao.database.poem.entity.PoemEntity
import com.shifenmiao.model.ModelProvider

/** 中国古诗词领域模型 */
data class Poem(
    /** 服务端诗词 id */
    val id: Long,
    val title: String,
    /** 逐句内容 */
    val content: List<String>,
    val author: String,
    val dynasty: String,
    val type: String,
    val aiInsight: String? = null,
    /** AI 生成的逐字拼音(每句一行,行内空格分隔),为空表示尚未生成 */
    val pinyin: String? = null,
    /** AI 生成的现代汉语翻译 */
    val translation: String? = null,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
) {
    /** "唐·李白" 形式的作者朝代描述 */
    val authorWithDynasty: String
        get() = listOf(dynasty, author)
            .filter { it.isNotBlank() }
            .joinToString("·")
}

private val gson = ModelProvider.provideGson()

fun Poem.toEntity(): PoemEntity = PoemEntity(
    id = id,
    title = title,
    content = gson.toJson(content),
    author = author,
    dynasty = dynasty,
    type = type,
    aiInsight = aiInsight,
    pinyin = pinyin,
    translation = translation,
    isFavorite = isFavorite,
    createdAt = createdAt,
)

fun PoemEntity.toDomain(): Poem = Poem(
    id = id,
    title = title,
    content = runCatching {
        gson.fromJson(content, Array<String>::class.java)?.toList()
    }.getOrNull() ?: emptyList(),
    author = author,
    dynasty = dynasty,
    type = type,
    aiInsight = aiInsight,
    pinyin = pinyin,
    translation = translation,
    isFavorite = isFavorite,
    createdAt = createdAt,
)

/**
 * 解析存储的逐字拼音:每句对应一行,行内每个汉字的拼音以空格分隔。
 * 返回外层按句、内层按字的二维结构;空输入返回空列表。
 */
fun parsePinyinLines(pinyin: String?): List<List<String>> {
    if (pinyin.isNullOrBlank()) return emptyList()
    return pinyin.lines()
        .map { line -> line.trim().split(Regex("\\s+")).filter { it.isNotBlank() } }
        .filter { it.isNotEmpty() }
}

/**
 * 已存拼音是否可用于逐字网格:拼音 token 总数须等于全诗字数(与 PoemVerseGrid 的拆分口径一致)。
 * AI 对杂言/长诗容易对错字数,对齐失败的拼音会被网格整体隐藏,视为「无可用拼音」。
 */
fun Poem.isPinyinAligned(): Boolean {
    val tokens = parsePinyinLines(pinyin).flatten()
    if (tokens.isEmpty()) return false
    val charCount = content.sumOf { line -> line.count { it.isLetter() } }
    return tokens.size == charCount
}

// ── 诗泉 API DTO ─────────────────────────────────

/** 命名包装:{ "name": "..." } */
data class PoemNameDto(
    @SerializedName("name") val name: String = "",
)

data class PoemDto(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("title") val title: String = "",
    @SerializedName("content") val content: List<String>? = null,
    @SerializedName("author") val author: PoemNameDto? = null,
    @SerializedName("dynasty") val dynasty: PoemNameDto? = null,
    @SerializedName("type") val type: PoemNameDto? = null,
) {
    fun toDomain(): Poem = Poem(
        id = id,
        title = title,
        content = content.orEmpty(),
        author = author?.name.orEmpty(),
        dynasty = dynasty?.name.orEmpty(),
        type = type?.name.orEmpty(),
    )
}

/** GET /api/poems/random 响应 */
data class SinglePoemResponse(
    @SerializedName("data") val data: PoemDto? = null,
)

/** GET /api/search 响应(data 可能缺失或为空数组) */
data class PoemListResponse(
    @SerializedName("data") val data: List<PoemDto>? = null,
)

/** GET /api/dynasties、/api/types 响应 */
data class PoemNameListResponse(
    @SerializedName("data") val data: List<PoemNameDto>? = null,
)
