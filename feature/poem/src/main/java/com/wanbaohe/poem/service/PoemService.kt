package com.wanbaohe.poem.service

import com.shifenmiao.database.activity.ActivityLogRecorder
import com.shifenmiao.database.poem.repo.PoemRepository
import com.shifenmiao.model.ModelProvider
import com.shifenmiao.storage.MMKVName
import com.shifenmiao.storage.RemoteConfigStorage
import com.t8rin.logger.makeLog
import com.tencent.mmkv.MMKV
import com.wanbaohe.poem.model.Poem
import com.wanbaohe.poem.model.PoemListResponse
import com.wanbaohe.poem.model.PoemNameListResponse
import com.wanbaohe.poem.model.SinglePoemResponse
import com.wanbaohe.poem.model.toDomain
import com.wanbaohe.poem.model.toEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * 诗泉 API 与本地诗词库的组合服务。
 *
 * - 网络:随机取诗 / 关键词搜索 / 朝代与体裁筛选项,base URL 由 remoteConfig 下发
 * - 本地:历史与收藏的读写统一走 [PoemRepository]
 * - 行为日志:随机取诗、收藏、删除经 [ActivityLogRecorder] 记录
 */
@Singleton
class PoemService @Inject constructor(
    @Named("PoemOkHttpClient") private val okHttpClient: OkHttpClient,
    private val poemRepository: PoemRepository,
    private val activityLogRecorder: ActivityLogRecorder,
) {
    private val gson = ModelProvider.provideGson()

    /** 筛选项持久缓存(MMKV):加载过一次就不再请求 */
    private val filterMmkv: MMKV = MMKV.mmkvWithID(MMKVName.POEM_FILTER)
    private val memoryFilterCache = mutableMapOf<String, List<String>>()

    /** Base URL:优先 remoteConfig 动态下发,缺失时回退默认地址 */
    private val baseUrl: String
        get() = RemoteConfigStorage.getRemoteConfig().poemApiUrl
            ?.takeIf { it.isNotBlank() }
            ?: DEFAULT_BASE_URL

    // ── 网络:随机取诗 ─────────────────────────────

    suspend fun fetchRandomPoem(
        author: String? = null,
        dynasty: String? = null,
        type: String? = null,
        char: String? = null,
        recordLog: Boolean = true,
    ): Result<Poem> = withContext(Dispatchers.IO) {
        runCatching {
            val url = baseUrl.toHttpUrl().newBuilder()
                .addPathSegments("api/poems/random")
                .apply {
                    author?.takeIf { it.isNotBlank() }?.let { addQueryParameter("author", it) }
                    dynasty?.takeIf { it.isNotBlank() }?.let { addQueryParameter("dynasty", it) }
                    type?.takeIf { it.isNotBlank() }?.let { addQueryParameter("type", it) }
                    char?.takeIf { it.isNotBlank() }?.let { addQueryParameter("char", it) }
                }
                .build()
            val poem = get(url.toString(), SinglePoemResponse::class.java).data?.toDomain()
                ?: error("诗泉 API 返回为空")
            poemRepository.upsert(poem.toEntity())
            if (recordLog) {
                recordPoemLog(actionType = "GENERATE", poem = poem)
            }
            poem
        }.onFailure { it.makeLog(TAG) }
    }

    /**
     * 批量随机取样聚合成列表(按 id 去重,不写行为日志)。
     * 用于搜索页短关键词场景:search API 要求 q≥3 字符,两字诗人/短词只能用 random 带筛选近似。
     */
    suspend fun fetchRandomPoems(
        author: String? = null,
        dynasty: String? = null,
        type: String? = null,
        char: String? = null,
        count: Int = 12,
    ): Result<List<Poem>> = withContext(Dispatchers.IO) {
        runCatching {
            val collected = LinkedHashMap<Long, Poem>()
            repeat(count) {
                fetchRandomPoem(author, dynasty, type, char, recordLog = false)
                    .getOrNull()
                    ?.let { collected.putIfAbsent(it.id, it) }
            }
            if (collected.isEmpty()) error("诗泉 API 返回为空")
            collected.values.toList()
        }.onFailure { it.makeLog(TAG) }
    }

    // ── 网络:搜索 ─────────────────────────────────

    suspend fun searchPoems(query: String): Result<List<Poem>> = withContext(Dispatchers.IO) {
        val keyword = query.trim()
        if (keyword.isEmpty()) return@withContext Result.success(emptyList())
        runCatching {
            val url = baseUrl.toHttpUrl().newBuilder()
                .addPathSegments("api/search")
                .addQueryParameter("q", keyword)
                .build()
            get(url.toString(), PoemListResponse::class.java)
                .data.orEmpty()
                .map { it.toDomain() }
        }.onFailure { it.makeLog(TAG) }
    }

    // ── 网络:筛选项(内存 + MMKV 持久缓存,加载过一次就不再请求) ──

    suspend fun fetchDynasties(): Result<List<String>> = withContext(Dispatchers.IO) {
        loadFilterCache(KEY_DYNASTIES)?.let { return@withContext Result.success(it) }
        runCatching {
            fetchNameList("api/dynasties")
        }.onSuccess { saveFilterCache(KEY_DYNASTIES, it) }
            .onFailure { it.makeLog(TAG) }
    }

    suspend fun fetchTypes(): Result<List<String>> = withContext(Dispatchers.IO) {
        loadFilterCache(KEY_TYPES)?.let { return@withContext Result.success(it) }
        runCatching {
            fetchNameList("api/types")
        }.onSuccess { saveFilterCache(KEY_TYPES, it) }
            .onFailure { it.makeLog(TAG) }
    }

    /** 读筛选项缓存:先内存后 MMKV(换行分隔保序),均为空返回 null */
    private fun loadFilterCache(key: String): List<String>? {
        memoryFilterCache[key]?.let { return it }
        val persisted = filterMmkv.decodeString(key)?.takeIf { it.isNotBlank() }
            ?.split('\n')?.filter { it.isNotBlank() }
            ?.takeIf { it.isNotEmpty() }
        if (persisted != null) {
            memoryFilterCache[key] = persisted
        }
        return persisted
    }

    private fun saveFilterCache(key: String, value: List<String>) {
        if (value.isEmpty()) return
        memoryFilterCache[key] = value
        filterMmkv.encode(key, value.joinToString("\n"))
    }

    private fun fetchNameList(path: String): List<String> {
        val url = baseUrl.toHttpUrl().newBuilder().addPathSegments(path).build()
        return get(url.toString(), PoemNameListResponse::class.java)
            .data.orEmpty()
            .map { it.name }
            .filter { it.isNotBlank() }
    }

    // ── 本地:历史 / 收藏 ──────────────────────────

    fun observeHistory(): Flow<List<Poem>> {
        return poemRepository.observeAll().map { entities -> entities.map { it.toDomain() } }
    }

    fun observeFavorites(): Flow<List<Poem>> {
        return poemRepository.observeFavorites().map { entities -> entities.map { it.toDomain() } }
    }

    fun observePoem(id: Long): Flow<Poem?> {
        return poemRepository.observeById(id).map { it?.toDomain() }
    }

    suspend fun getPoem(id: Long): Poem? {
        return poemRepository.getById(id)?.toDomain()
    }

    suspend fun upsertPoem(poem: Poem) {
        poemRepository.upsert(poem.toEntity())
    }

    suspend fun toggleFavorite(id: Long) {
        val entity = poemRepository.getById(id) ?: return
        val newValue = !entity.isFavorite
        poemRepository.updateFavorite(id = id, isFavorite = newValue)
        recordPoemLog(
            actionType = if (newValue) "FAVORITE" else "UNFAVORITE",
            poem = entity.toDomain().copy(isFavorite = newValue),
            title = (if (newValue) "收藏《" else "取消收藏《") + entity.title + "》",
        )
    }

    suspend fun deletePoem(id: Long) {
        val entity = poemRepository.getById(id)
        poemRepository.deleteById(id)
        entity?.let {
            recordPoemLog(
                actionType = "DELETE",
                poem = it.toDomain(),
                title = "删除《${it.title}》",
            )
        }
    }

    /** 清空历史(保留收藏) */
    suspend fun clearHistory() {
        poemRepository.deleteAllNonFavorites()
    }

    // ── 内部 ─────────────────────────────────────

    private fun <T> get(url: String, clazz: Class<T>): T {
        val request = Request.Builder().url(url).get().build()
        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("诗泉 API HTTP ${response.code}")
            val body = response.body?.string().orEmpty()
            if (body.isBlank()) error("诗泉 API 响应体为空")
            return gson.fromJson(body, clazz) ?: error("诗泉 API 响应解析失败")
        }
    }

    private suspend fun recordPoemLog(
        actionType: String,
        poem: Poem,
        title: String = poem.title,
    ) {
        runCatching {
            activityLogRecorder.recordPoem(
                entityId = poem.id.toString(),
                actorType = "USER",
                actionType = actionType,
                source = SOURCE,
                title = title,
                description = "${poem.author}·${poem.dynasty}",
                screenRoute = "onebox://screen/poem?poem_id=${poem.id}",
            )
        }.onFailure { it.makeLog(TAG) }
    }

    companion object {
        private const val TAG = "PoemService"
        private const val SOURCE = "PoemScreen"

        /** 筛选项 MMKV 缓存键 */
        private const val KEY_DYNASTIES = "dynasties"
        private const val KEY_TYPES = "types"

        /** 诗泉 API 兜底地址,remoteConfig 未下发时使用 */
        const val DEFAULT_BASE_URL = "https://poetry.palemoky.com"
    }
}
