/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

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
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

/**
 * 消息反馈域(messageFeedback 远程端点,对齐 Flutter feedback_store.dart,DSH-PROTOCOL §9)。
 *
 * 0.1.5-rc.2 差异(端点名与 args 形状已按实测核对):
 * - 三个端点的斜杠名不变,args 恒是 `{request:{…}}`(Remote 方法只有这一个业务参数,
 *   服务端按字段名严格校验:多字段 = gateway/arguments-invalid,嵌套请求体还会做
 *   边界校验,rating 非法即 gateway/input-invalid)
 * - **put 的 request 必填 `ifVersion`**:null = 要求当前不存在(创建),token = 要求的版本;
 *   旧代码"创建时不发该字段"会让服务端拿 undefined 与 null 比较,直接判 version-conflict
 * - **delete 的 request 必填 `ifVersion`,且必须是非空 token**(删除只表达"我看到的版本是它")
 * - 条目新增 `category`(本 store 不透出:wire 模型未含,UI 也未用);
 *   `createdAt`/`updatedAt` 是 epoch ms **数字**(防御式解析按原样转字符串保留)
 * - 错误码仍是**业务域裸码**:version-conflict / note-too-large / note-blank /
 *   session-not-found / target-not-found(不是网关斜杠词表,故这里保留本地常量,
 *   不要换成 [com.wanbaohe.dsh.wire.RpcErrorCodes] 的 session/not-found)
 * - `sessionFeedback/record`(0.1.5 新增的会话级反馈 Remote)也走本 store,
 *   args `{request:{sessionId, text?, category?}}` → 回值 `{recorded:true}`
 * - 无实时推送:代际 ready(重连)清缓存并发变更广播,消费端(UI)自行重拉
 *
 * 全部走 [DshApiClient.callRemote]:它拆掉一级 RemoteResult 后,本域的回值还带一层
 * 业务信封 `{ok:true,value}|{ok:false,error}`,再拆一层才是 {items}/条目/{absent};
 * 失败时抛出的 [RpcBusinessException] 里就是上面的业务域裸码。
 *
 * - list `{request:{sessionId}}` → `{items:[…]}`
 * - put `{request:{sessionId, messageId, rating, note?, ifVersion}}` → 更新后的条目(新 version)
 * - delete `{request:{sessionId, messageId, ifVersion}}` → `{absent:true}`(幂等,
 *   条目已缺席时 ifVersion 被忽略)
 * - CAS:ifVersion = null 表示要求当前不存在(创建);token 来自上次 list/put,
 *   每次 material create/update 都会轮换
 * - version-conflict → 自动 list 重读(权威条目落地 + 广播),再抛
 *   [FeedbackVersionConflictException] 携带权威条目(并发删除后可能为 null → 视为未评)
 * - note-too-large(服务端上限 maxNoteBytes=8192)→ [FeedbackNoteTooLargeException]
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

/** messageFeedback 业务域裸码(0.1.5 仍是裸码,非网关斜杠词表) */
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
     *
     * 0.1.5 的 request 里 `ifVersion` **必填**:[ifVersion] 为 null 时显式发 JSON null
     * (= 要求当前不存在,创建);不发字段会被判 version-conflict(服务端拿 undefined
     * 与 null 比较)。version-conflict → 自动重读后抛
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
                        put("ifVersion", ifVersion ?: JsonNull)
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
     *
     * 0.1.5 的 request 里 `ifVersion` 必填且**不可为 null**(非空 CAS token),所以
     * [ifVersion] 为 null 时不发该字段:服务端会按边界校验拒掉(gateway/input-invalid)。
     * 调用方(UI)撤回的是已存在的条目,应传其当前 version。
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

    /**
     * sessionFeedback/record:记录一条**会话级**反馈(与逐消息评分分域,0.1.5 新增)。
     *
     * - args `{request:{sessionId, text?, category?}}`;text 是自由文本(空白视为缺席),
     *   category 取服务端固定词表(task-result/instruction-following/product-interaction/
     *   service-stability/resource-cost/security-privacy-permission/other)
     * - 回值 `{recorded:true}`;只落会话日志,不进模型上下文,也不影响消息评分缓存
     * - 失败码仍是业务域裸码 session-not-found(仅活会话可记录)
     * - 当前 UI 只做逐消息评分,本方法是会话级提交的接入点(暂无调用方)
     */
    suspend fun record(sessionId: String, text: String? = null, category: String? = null) {
        try {
            api.callRemote(
                RemoteSessionFeedbackRecord,
                buildJsonObject {
                    putJsonObject("request") {
                        put("sessionId", sessionId)
                        text?.takeIf { it.isNotBlank() }?.let { put("text", it) }
                        category?.let { put("category", it) }
                    }
                }
            )
        } catch (e: RpcBusinessException) {
            throw FeedbackStoreException(e.error.code, e.error.message)
        } catch (e: ApiTimeoutException) {
            throw FeedbackStoreException("timeout", "会话反馈记录超时")
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
        /** 远程端点方法名(斜杠命名,集中这里便于与 Harness 对照) */
        private const val RemoteList = "messageFeedback/list"
        private const val RemotePut = "messageFeedback/put"
        private const val RemoteDelete = "messageFeedback/delete"
        private const val RemoteSessionFeedbackRecord = "sessionFeedback/record"
    }
}
