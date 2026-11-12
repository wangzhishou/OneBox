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

package com.wanbaohe.dsh.wire

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonPrimitive
import java.util.UUID

/**
 * 模块内统一的 Json 配置:宽松读(忽略未知键)、省略 null 写、补默认值。
 */
@OptIn(ExperimentalSerializationApi::class)
val DshJson: Json = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    encodeDefaults = true
}

/**
 * 上行信封(0.1.5 Connection `/api` 通道):
 * `{type:'client-request', rpcId, method, payload}`。
 *
 * 两条硬约束(服务端会校验):
 * - `method` 必须与 URL 路径上的端点名逐字一致(如都用 `session/list`)
 * - `payload` 恒为 `{args:{…}}`,0.1.5 的 Remote 端点只接受这一个字段
 *
 * rpcId 只由发起方 mint(UUID),响应永远回显、永不新造。
 */
@Serializable
data class ClientRequest(
    val type: String = "client-request",
    val rpcId: String,
    val method: String,
    val payload: JsonObject
) {
    companion object {
        /** mint 一条新请求,rpcId 用 UUIDv4 */
        fun mint(method: String, payload: JsonObject): ClientRequest = ClientRequest(
            rpcId = UUID.randomUUID().toString(),
            method = method,
            payload = payload
        )
    }
}

/**
 * 下行信封(ServerResponse):`{type:'server-response', rpcId, result}`。
 *
 * 两级解析纪律:这里只解析到信封,result 保持 JsonObject 原样,
 * 由 [RpcResult.parse] 分流 ok/error;业务 value 再由调用方二次 parse。
 */
@Serializable
data class ServerResponse(
    val type: String,
    val rpcId: String,
    val result: JsonObject
)

/** RPC 结果:`{ok:true, value?}` 或 `{ok:false, error:{code, message, details?}}` */
sealed interface RpcResult {

    /** ok 分支:value 缺席时为 [JsonNull](如 commands/execute 成功返回 void) */
    data class Ok(val value: JsonElement) : RpcResult

    /** error 分支:错误码原样保留(见 [RpcError]) */
    data class Err(val error: RpcError) : RpcResult

    companion object {
        /** 分流 result 对象。信封畸形(缺 ok / ok:false 无 error 体)抛 [CarrierException]。 */
        fun parse(result: JsonObject): RpcResult {
            val ok = result["ok"]?.jsonPrimitive?.content
                ?: throw CarrierException("result 缺少 ok 字段")
            return if (ok == "true") {
                Ok(result["value"] ?: JsonNull)
            } else {
                val errorJson = result["error"] as? JsonObject
                    ?: throw CarrierException("ok:false 但缺少 error 体")
                val raw = DshJson.decodeFromJsonElement<RpcError>(errorJson)
                Err(RpcError.of(raw.code, raw.message, raw.details))
            }
        }
    }
}
