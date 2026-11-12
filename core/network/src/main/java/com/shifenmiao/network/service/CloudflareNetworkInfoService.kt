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

package com.shifenmiao.network.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

data class CloudflareNetworkInfo(
    val clientIp: String,
    val countryCode: String,
    val region: String,
    val city: String,
    val asn: Int?,
    val organization: String,
    val hostname: String,
    val httpProtocol: String,
    val nodeCode: String,
    val nodeCity: String,
    val nodeCountryCode: String,
)

@Singleton
class CloudflareNetworkInfoService @Inject constructor() {
    private val client = OkHttpClient.Builder()
        .callTimeout(10, TimeUnit.SECONDS)
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    suspend fun load(): CloudflareNetworkInfo? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(META_URL)
                .header("Accept", "application/json")
                .header("Origin", "https://speed.cloudflare.com")
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@use null
                parse(JSONObject(response.body.string()))
            }
        } catch (_: Exception) {
            currentCoroutineContext().ensureActive()
            null
        }
    }

    private fun parse(json: JSONObject): CloudflareNetworkInfo {
        val colo = json.opt("colo")
        val coloObject = colo as? JSONObject
        return CloudflareNetworkInfo(
            clientIp = json.optString("clientIp"),
            countryCode = json.optString("country"),
            region = json.optString("region"),
            city = json.optString("city"),
            asn = json.optInt("asn", 0).takeIf { it > 0 },
            organization = json.optString("asOrganization"),
            hostname = json.optString("hostname"),
            httpProtocol = json.optString("httpProtocol"),
            nodeCode = coloObject?.optString("iata") ?: colo as? String ?: "",
            nodeCity = coloObject?.optString("city").orEmpty(),
            nodeCountryCode = coloObject?.optString("cca2").orEmpty(),
        )
    }

    private companion object {
        const val META_URL = "https://speed.cloudflare.com/meta"
    }
}
