package com.wanbaohe.dsh.session

import com.wanbaohe.dsh.connection.ConnectionPhase
import com.wanbaohe.dsh.connection.DshApiClient
import com.wanbaohe.dsh.connection.DshConnectionController
import com.wanbaohe.dsh.wire.ApiTimeoutException
import com.wanbaohe.dsh.wire.CarrierException
import com.wanbaohe.dsh.wire.RpcBusinessException
import com.wanbaohe.dsh.wire.model.FeedbackItem
import com.wanbaohe.dsh.wire.model.parseFeedbackItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

/**
 * 消息反馈域(messageFeedback 远程端点,对齐 Flutter feedback_store.dart,DSH-PROTOCOL §9)。
 *
 * - 全部走 [DshApiClient.callRemote](内层信封剥离 + 内层错误码原样保留):
 *   list {request:{sessionId}} → {items:[...]};put {request:{sessionId, messageId,
 *   rating, note?, ifVersion?}} → 更新后的条目(新 version);delete {request:{sessionId,
 *   messageId, ifVersion?}} → {absent:true}(幂等,条目已缺席时 ifVersion 被忽略)
 * - CAS:ifVersion 缺席 = 要求当前不存在(创建);token 来自上次 list/put,
 *   每次 material create/update 都会轮换
 * - version-conflict → 自动 list 重读(权威条目落地 + 广播),再抛
 *   [FeedbackVersionConflictException] 携带权威条目(并发删除后可能为 null → 视为未评)
 * - note-too-large(服务端上限 maxNoteBytes=8192)→ [FeedbackNoteTooLargeException]
 * - 无实时推送:代际 ready(重连)清缓存并发变更广播,消费端(UI)自行重拉
 *
 * 生命周期与连接实例绑定:由组件层创建并 [dispose]。
 */

/** 反馈域异常基类:code = 服务端/本地错误码(UI 文案按 code 区分) */
open class FeedbackStoreException(
    val code: String,
    override val message: String?
) : Exception("FeedbackStoreException($code${message?.let { ": $it" }.orEmpty()})")

/** CAS 冲突:put 被拒后已自动重读;[authoritative] 为重读后的权威条目(可能为 null) */
class FeedbackVersionConflictException(
    val authoritative: FeedbackItem?
) : FeedbackStoreException(CodeVersionConflict, "评分已过期,请重试")

/** 备注超长(服务端 note-too-large) */
class FeedbackNoteTooLargeException :
    FeedbackStoreException(CodeNoteTooLarge, "备注过长(note-too-large)")

const val CodeVersionConflict = "version-conflict"
const val CodeNoteTooLarge = "note-too-large"

class FeedbackStore(
    private val api: DshApiClient,
    private val connection: DshConnectionController,
    parentScope: CoroutineScope
) {

    private val scope = CoroutineScope(
        parentScope.coroutineContext + SupervisorJob(parentScope.coroutineContext[Job])
    )

    private val _changed = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    /** 变更广播(list/put/delete/代际失效都会推;UI 订阅后重读 itemsFor) */
    val changed: SharedFlow<Unit> = _changed.asSharedFlow()

    private val cache = HashMap<String, List<FeedbackItem>>()

    @Volatile
    private var disposed = false
    private var started = false
    private var lastReadyGeneration = 0

    fun start() {
        if (started) return
        started = true
        scope.launch {
            connection.snapshots.collect { snapshot ->
                if (disposed || snapshot.phase != ConnectionPhase.Ready) return@collect
                if (snapshot.generation <= lastReadyGeneration) return@collect
                lastReadyGeneration = snapshot.generation
                // 重连 = 新代际:反馈无实时帧,缓存作废,消费端自行重拉(web 同款 resync)
                cache.clear()
                emitChanged()
            }
        }
    }

    fun dispose() {
        disposed = true
        scope.cancel()
    }

    /** 某会话的条目快照(同步读;未加载/代际失效后为空列表) */
    fun itemsFor(sessionId: String): List<FeedbackItem> = cache[sessionId].orEmpty()

    /** 拉取某会话全部条目(单次 list 填充整段对话;缓存命中即返回,force 跳过) */
    suspend fun list(sessionId: String, force: Boolean = false): List<FeedbackItem> {
        if (!force) cache[sessionId]?.let { return it }
        try {
            val value = api.callRemote(
                RemoteList,
                buildJsonObject {
                    putJsonObject("request") { put("sessionId", sessionId) }
                }
            )
            val itemsObj = value as? JsonObject
                ?: throw CarrierException("messageFeedback/list: value 不是对象")
            val items = (itemsObj["items"] as? JsonArray)
                ?: throw CarrierException("messageFeedback/list: items 不是数组")
            val parsed = items.map(::parseFeedbackItem)
            cache[sessionId] = parsed
            emitChanged()
            return parsed
        } catch (e: RpcBusinessException) {
            throw FeedbackStoreException(e.error.code, e.error.message)
        } catch (e: ApiTimeoutException) {
            throw FeedbackStoreException("timeout", "反馈读取超时")
        } catch (e: CarrierException) {
            throw FeedbackStoreException("transport", e.message)
        }
    }

    /**
     * CAS put:成功返回更新后的条目(新 version,缓存 + 广播)。
     * [ifVersion] 缺席 = 要求当前不存在(创建);version-conflict → 自动重读后抛
     * [FeedbackVersionConflictException](携带权威条目,UI 直接对账)。
     */
    suspend fun put(
        sessionId: String,
        messageId: String,
        rating: String,
        note: String? = null,
        ifVersion: JsonElement? = null
    ): FeedbackItem {
        try {
            val value = api.callRemote(
                RemotePut,
                buildJsonObject {
                    putJsonObject("request") {
                        put("sessionId", sessionId)
                        put("messageId", messageId)
                        put("rating", rating)
                        note?.let { put("note", it) }
                        ifVersion?.let { put("ifVersion", it) }
                    }
                }
            )
            val item = parseFeedbackItem(value)
            upsert(sessionId, item)
            emitChanged()
            return item
        } catch (e: RpcBusinessException) {
            when (e.error.code) {
                CodeVersionConflict -> {
                    // 冲突恢复语义:自动 list 重读(权威条目 + 广播),再抛 typed 异常
                    resync(sessionId)
                    throw FeedbackVersionConflictException(find(sessionId, messageId))
                }

                CodeNoteTooLarge -> throw FeedbackNoteTooLargeException()
                else -> throw FeedbackStoreException(e.error.code, e.error.message)
            }
        } catch (e: ApiTimeoutException) {
            throw FeedbackStoreException("timeout", "评分请求超时")
        } catch (e: CarrierException) {
            throw FeedbackStoreException("transport", e.message)
        }
    }

    /**
     * 幂等 delete;返回 true = 条目已缺席(absent:true,无操作),
     * false = 本次删除了既有条目。成功都会清除本地缓存并广播。
     */
    suspend fun delete(
        sessionId: String,
        messageId: String,
        ifVersion: JsonElement? = null
    ): Boolean {
        try {
            val value = api.callRemote(
                RemoteDelete,
                buildJsonObject {
                    putJsonObject("request") {
                        put("sessionId", sessionId)
                        put("messageId", messageId)
                        ifVersion?.let { put("ifVersion", it) }
                    }
                }
            )
            // 内层 value = {absent:bool};缺 absent 视为删除成功(false)
            val absent = ((value as? JsonObject)?.get("absent") as? JsonPrimitive)
                ?.booleanOrNull ?: false
            remove(sessionId, messageId)
            emitChanged()
            return absent
        } catch (e: RpcBusinessException) {
            throw FeedbackStoreException(e.error.code, e.error.message)
        } catch (e: ApiTimeoutException) {
            throw FeedbackStoreException("timeout", "评分撤回超时")
        } catch (e: CarrierException) {
            throw FeedbackStoreException("transport", e.message)
        }
    }

    /** 冲突后的重读:失败保持原缓存(权威条目以当前缓存为准) */
    private suspend fun resync(sessionId: String) {
        runCatching { list(sessionId, force = true) }
    }

    private fun upsert(sessionId: String, item: FeedbackItem) {
        val items = cache[sessionId].orEmpty().toMutableList()
        val idx = items.indexOfFirst { it.messageId == item.messageId }
        if (idx >= 0) items[idx] = item else items.add(item)
        cache[sessionId] = items
    }

    private fun remove(sessionId: String, messageId: String) {
        val items = cache[sessionId] ?: return
        cache[sessionId] = items.filterNot { it.messageId == messageId }
    }

    private fun find(sessionId: String, messageId: String): FeedbackItem? =
        cache[sessionId].orEmpty().firstOrNull { it.messageId == messageId }

    private fun emitChanged() {
        _changed.tryEmit(Unit)
    }

    companion object {
        /** 远程端点方法名(斜杠命名,不在核心点号方法集里) */
        private const val RemoteList = "messageFeedback/list"
        private const val RemotePut = "messageFeedback/put"
        private const val RemoteDelete = "messageFeedback/delete"
    }
}
