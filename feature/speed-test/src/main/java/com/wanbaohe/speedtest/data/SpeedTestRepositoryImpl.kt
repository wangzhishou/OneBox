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

package com.wanbaohe.speedtest.data

import com.shifenmiao.database.speedtest.dao.SpeedTestRecordDao
import com.shifenmiao.database.speedtest.entity.SpeedTestRecordEntity
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.network.service.CloudflareNetworkInfo
import com.shifenmiao.network.service.CloudflareNetworkInfoService
import com.wanbaohe.speedtest.R
import com.wanbaohe.speedtest.domain.SpeedTestPhase
import com.wanbaohe.speedtest.domain.SpeedTestRecord
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class SpeedTestRepositoryImpl @Inject constructor(
    private val dao: SpeedTestRecordDao,
    @Named("SpeedTestOkHttpClient") private val okHttpClient: OkHttpClient,
    private val networkInfoService: CloudflareNetworkInfoService,
) : SpeedTestRepository {

    override fun startTest(config: SpeedTestConfig, networkType: String): Flow<SpeedTestPhase> =
        flow {
            coroutineScope {
                val metaDeferred = async { networkInfoService.load() }
                emit(SpeedTestPhase.MeasuringLatency)

                val latencyUrl = if (config.supportsUpload) CLOUDFLARE_LATENCY_URL else config.testUrl
                val latency = measureLatency(latencyUrl)
                val download = measureDownload(config) { speed, progress ->
                    val overallProgress = if (config.supportsUpload) progress * 0.5f else progress
                    emit(SpeedTestPhase.Downloading(speed, overallProgress, latency.latencyMs))
                }
                val upload = if (config.supportsUpload) {
                    measureUpload(config.durationSeconds) { speed, progress ->
                        emit(
                            SpeedTestPhase.Uploading(
                                liveMbps = speed,
                                progress = 0.5f + progress * 0.5f,
                                latencyMs = latency.latencyMs
                            )
                        )
                    }
                } else null

                val meta = metaDeferred.await()
                val record = SpeedTestRecord(
                    networkType = networkType,
                    downloadMbps = download.mbps,
                    uploadMbps = upload?.mbps ?: -1f,
                    latencyMs = latency.latencyMs,
                    clientIp = meta?.clientIp.orEmpty(),
                    clientLocation = meta?.let {
                        formatLocation(it.countryCode, it.region, it.city)
                    }.orEmpty(),
                    carrier = meta?.let { formatCarrier(it.organization, it.asn ?: 0) }.orEmpty(),
                    serverHost = config.serverHost,
                    latencyNode = formatNode(config.serverHost, latency.nodeCode, meta),
                    downloadNode = formatNode(config.serverHost, download.nodeCode, meta),
                    uploadNode = if (upload == null) "" else formatNode(config.serverHost, upload.nodeCode, meta),
                    recordedAt = System.currentTimeMillis()
                )
                emit(SpeedTestPhase.Done(record))
            }
        }.catch {
            emit(SpeedTestPhase.Error(it.message ?: AppContext.getString(R.string.speed_test_error_failed)))
        }.flowOn(Dispatchers.IO)

    private suspend fun measureDownload(
        config: SpeedTestConfig,
        onProgress: suspend (speed: Float, progress: Float) -> Unit
    ): TransferMeasurement {
        val request = Request.Builder().url(config.testUrl).build()
        val startTime = monotonicMillis()
        val maxDurationMs = config.durationSeconds.coerceAtLeast(1) * 1000L
        var totalBytes = 0L
        var lastEmitTime = startTime
        var lastBytes = 0L
        var nodeCode = ""
        onProgress(0f, 0f)

        val finalMbps = okHttpClient.newCall(request).useCancellable { response ->
            check(response.isSuccessful) { "HTTP ${response.code}" }
            nodeCode = response.cloudflareNodeCode()
            val buffer = ByteArray(DOWNLOAD_BUFFER_BYTES)
            val stream = response.body.byteStream()
            while (true) {
                currentCoroutineContext().ensureActive()
                val read = stream.read(buffer)
                if (read == -1) break
                totalBytes += read
                val now = monotonicMillis()
                val elapsed = now - startTime
                if (now - lastEmitTime >= PROGRESS_INTERVAL_MS) {
                    val intervalBytes = totalBytes - lastBytes
                    val intervalSec = (now - lastEmitTime) / 1000.0
                    val mbps = ((intervalBytes * 8) / 1_000_000.0 / intervalSec).toFloat()
                    onProgress(mbps, (elapsed.toFloat() / maxDurationMs).coerceIn(0f, 1f))
                    lastEmitTime = now
                    lastBytes = totalBytes
                }
                if (elapsed >= maxDurationMs) break
            }
            val totalElapsedSec = (monotonicMillis() - startTime).coerceAtLeast(1) / 1000.0
            ((totalBytes * 8) / 1_000_000.0 / totalElapsedSec).toFloat()
        }
        return TransferMeasurement(finalMbps, nodeCode)
    }

    private suspend fun measureUpload(
        durationSeconds: Int,
        onProgress: suspend (speed: Float, progress: Float) -> Unit
    ): TransferMeasurement {
        val startTime = monotonicMillis()
        val maxDurationMs = durationSeconds.coerceAtLeast(1) * 1000L
        val payload = ByteArray(UPLOAD_CHUNK_BYTES)
        val contentType = "application/octet-stream".toMediaType()
        var totalBytes = 0L
        var nodeCode = ""
        onProgress(0f, 0f)

        while (monotonicMillis() - startTime < maxDurationMs) {
            currentCoroutineContext().ensureActive()
            val request = Request.Builder()
                .url(CLOUDFLARE_UPLOAD_URL)
                .post(payload.toRequestBody(contentType))
                .build()
            okHttpClient.newCall(request).useCancellable { response ->
                check(response.isSuccessful) { "HTTP ${response.code}" }
                nodeCode = response.cloudflareNodeCode()
                val uploadedBytes = response.header("cf-meta-upload-bytes")?.toLongOrNull()
                totalBytes += uploadedBytes?.takeIf { it > 0 } ?: payload.size.toLong()
            }
            val elapsed = monotonicMillis() - startTime
            val mbps = ((totalBytes * 8) / 1_000_000.0 / (elapsed.coerceAtLeast(1) / 1000.0)).toFloat()
            onProgress(mbps, (elapsed.toFloat() / maxDurationMs).coerceIn(0f, 1f))
        }

        val totalElapsedSec = (monotonicMillis() - startTime).coerceAtLeast(1) / 1000.0
        val finalMbps = ((totalBytes * 8) / 1_000_000.0 / totalElapsedSec).toFloat()
        return TransferMeasurement(finalMbps, nodeCode)
    }

    private suspend fun measureLatency(url: String): LatencyMeasurement = try {
        val request = Request.Builder().url(url).head().build()
        val start = monotonicMillis()
        var nodeCode = ""
        val latency = okHttpClient.newCall(request).useCancellable { response ->
            nodeCode = response.cloudflareNodeCode()
            if (response.isSuccessful) (monotonicMillis() - start).toInt() else -1
        }
        LatencyMeasurement(latency, nodeCode)
    } catch (_: Exception) {
        currentCoroutineContext().ensureActive()
        LatencyMeasurement(-1, "")
    }

    private fun formatLocation(countryCode: String, region: String, city: String): String {
        val country = countryCode.takeIf { it.isNotBlank() }?.let {
            Locale.Builder().setRegion(it).build().getDisplayCountry(Locale.getDefault())
        }.orEmpty()
        return listOf(country, region, city)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .joinToString(" · ")
    }

    private fun formatCarrier(rawCarrier: String, asn: Int): String {
        val normalized = rawCarrier.lowercase(Locale.ROOT)
        val carrierName = when {
            "china unicom" in normalized -> AppContext.getString(R.string.speed_test_carrier_china_unicom)
            "china mobile" in normalized -> AppContext.getString(R.string.speed_test_carrier_china_mobile)
            "china telecom" in normalized || "chinanet" in normalized ->
                AppContext.getString(R.string.speed_test_carrier_china_telecom)
            else -> rawCarrier.trim()
        }
        return listOfNotNull(
            carrierName.takeIf { it.isNotEmpty() },
            asn.takeIf { it > 0 }?.let { "AS$it" }
        ).joinToString(" · ")
    }

    private fun formatNode(host: String, code: String, meta: CloudflareNetworkInfo?): String {
        if (host != SpeedTestConfig.CLOUDFLARE_HOST) return host
        val city = meta?.nodeCity.orEmpty().takeIf {
            code.isNotEmpty() && code.equals(meta?.nodeCode, ignoreCase = true)
        }.orEmpty()
        return when {
            city.isNotEmpty() && code.isNotEmpty() -> "Cloudflare · $city ($code)"
            code.isNotEmpty() -> "Cloudflare · $code"
            else -> "Cloudflare"
        }
    }

    private fun Response.cloudflareNodeCode(): String =
        header("cf-meta-colo") ?: header("colo").orEmpty()

    private fun monotonicMillis() = System.nanoTime() / 1_000_000L

    private suspend fun <T> Call.useCancellable(block: suspend (Response) -> T): T = coroutineScope {
        val cancellation = launch(Dispatchers.IO, start = CoroutineStart.UNDISPATCHED) {
            try {
                awaitCancellation()
            } finally {
                this@useCancellable.cancel()
            }
        }
        try {
            execute().use { block(it) }
        } finally {
            cancellation.cancel()
        }
    }

    override suspend fun saveRecord(record: SpeedTestRecord) {
        dao.insert(
            SpeedTestRecordEntity(
                networkType = record.networkType,
                downloadMbps = record.downloadMbps,
                uploadMbps = record.uploadMbps,
                latencyMs = record.latencyMs,
                clientIp = record.clientIp,
                clientLocation = record.clientLocation,
                carrier = record.carrier,
                serverHost = record.serverHost,
                latencyNode = record.latencyNode,
                downloadNode = record.downloadNode,
                uploadNode = record.uploadNode,
                recordedAt = record.recordedAt
            )
        )
    }

    override fun getHistory(): Flow<List<SpeedTestRecord>> =
        dao.getAll().map { list ->
            list.map { entity ->
                SpeedTestRecord(
                    id = entity.id,
                    networkType = entity.networkType,
                    downloadMbps = entity.downloadMbps,
                    uploadMbps = entity.uploadMbps,
                    latencyMs = entity.latencyMs,
                    clientIp = entity.clientIp,
                    clientLocation = entity.clientLocation,
                    carrier = entity.carrier,
                    serverHost = entity.serverHost,
                    latencyNode = entity.latencyNode,
                    downloadNode = entity.downloadNode,
                    uploadNode = entity.uploadNode,
                    recordedAt = entity.recordedAt
                )
            }
        }

    override suspend fun clearHistory() = dao.clearAll()

    private data class TransferMeasurement(val mbps: Float, val nodeCode: String)
    private data class LatencyMeasurement(val latencyMs: Int, val nodeCode: String)

    private companion object {
        const val CLOUDFLARE_LATENCY_URL = "https://speed.cloudflare.com/__down?bytes=0"
        const val CLOUDFLARE_UPLOAD_URL = "https://speed.cloudflare.com/__up"
        const val DOWNLOAD_BUFFER_BYTES = 8 * 1024
        const val UPLOAD_CHUNK_BYTES = 1024 * 1024
        const val PROGRESS_INTERVAL_MS = 500L
    }
}
