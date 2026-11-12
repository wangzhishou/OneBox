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

package com.shifenmiao.common.voice

import android.util.Base64
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * 讯飞「大模型识别」WebSocket 签名 URL 本地生成。
 *
 * 联调期临时方案:RemoteConfig 直接下发 appId/apiKey/apiSecret 时,App 本地签名直连讯飞,
 * 不走路由网关代签;量大后应移除下发(回到 /api/voice/asr/ws-auth 代签)并重置凭据。
 * 签名规则与 Go 侧 buildIflytekAsrWsURL 保持一致:
 * signature_origin = "host: <host>\ndate: <RFC1123 GMT>\nGET /v1 HTTP/1.1",
 * HMAC-SHA256(apiSecret) → base64 得 signature,再拼 authorization 参数。
 */
object IFlytekAuth {

    private const val DEFAULT_HOST = "iat.xf-yun.com"
    private const val PATH = "/v1"

    fun buildWsUrl(apiKey: String, apiSecret: String, host: String = DEFAULT_HOST): String {
        val date = rfc1123GmtNow()
        val signatureOrigin = "host: $host\ndate: $date\nGET $PATH HTTP/1.1"
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(apiSecret.toByteArray(Charsets.UTF_8), "HmacSHA256"))
        val signature = Base64.encodeToString(
            mac.doFinal(signatureOrigin.toByteArray(Charsets.UTF_8)),
            Base64.NO_WRAP
        )
        val authorizationOrigin = "api_key=\"$apiKey\", algorithm=\"hmac-sha256\", " +
            "headers=\"host date request-line\", signature=\"$signature\""
        val authorization = Base64.encodeToString(
            authorizationOrigin.toByteArray(Charsets.UTF_8),
            Base64.NO_WRAP
        )
        val query = listOf(
            "authorization" to authorization,
            "date" to date,
            "host" to host
        ).joinToString("&") { (k, v) -> "$k=${URLEncoder.encode(v, "UTF-8")}" }
        return "wss://$host$PATH?$query"
    }

    private fun rfc1123GmtNow(): String =
        SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("GMT")
        }.format(Date())
}
