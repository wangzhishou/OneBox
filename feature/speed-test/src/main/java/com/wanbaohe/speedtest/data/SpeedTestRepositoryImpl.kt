package com.wanbaohe.speedtest.data

import com.shifenmiao.database.speedtest.dao.SpeedTestRecordDao
import com.shifenmiao.database.speedtest.entity.SpeedTestRecordEntity
import com.shifenmiao.interfaces.singleton.AppContext
import com.wanbaohe.speedtest.R
import com.wanbaohe.speedtest.domain.SpeedTestPhase
import com.wanbaohe.speedtest.domain.SpeedTestRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class SpeedTestRepositoryImpl @Inject constructor(
    private val dao: SpeedTestRecordDao,
    @Named("SpeedTestOkHttpClient") private val okHttpClient: OkHttpClient
) : SpeedTestRepository {

    override fun startTest(config: SpeedTestConfig, networkType: String): Flow<SpeedTestPhase> =
        flow {
            emit(SpeedTestPhase.MeasuringLatency)

            // ── Step 1: HEAD 请求测延迟 ──────────────────────────────────
            val latencyMs = measureLatency(config.testUrl)

            emit(SpeedTestPhase.Downloading(liveMbps = 0f, progress = 0f))

            // ── Step 2: 下载测速 ─────────────────────────────────────────
            val request = Request.Builder().url(config.testUrl).build()
            val startTime = System.currentTimeMillis()
            val maxDurationMs = config.durationSeconds * 1000L
            var totalBytes = 0L
            var lastEmitTime = startTime
            var lastBytes = 0L
            var finalMbps = 0f

            runCatching {
                okHttpClient.newCall(request).execute().use { response ->
                    val body = response.body
                    val buffer = ByteArray(8 * 1024)
                    val stream = body.byteStream()
                    while (true) {
                        val read = stream.read(buffer)
                        if (read == -1) break
                        totalBytes += read
                        val now = System.currentTimeMillis()
                        val elapsed = now - startTime
                        // 每 500ms 发射一次实时速度
                        if (now - lastEmitTime >= 500L) {
                            val intervalBytes = totalBytes - lastBytes
                            val intervalSec = (now - lastEmitTime) / 1000.0
                            val mbps =
                                ((intervalBytes * 8) / 1_000_000.0 / intervalSec).toFloat()
                            val progress =
                                (elapsed.toFloat() / maxDurationMs).coerceIn(0f, 1f)
                            emit(SpeedTestPhase.Downloading(liveMbps = mbps, progress = progress))
                            lastEmitTime = now
                            lastBytes = totalBytes
                            finalMbps = mbps
                        }
                        if (elapsed >= maxDurationMs) break
                    }
                    // 计算整体均值作为最终速度
                    val totalElapsedSec =
                        (System.currentTimeMillis() - startTime) / 1000.0
                    finalMbps =
                        if (totalElapsedSec > 0)
                            ((totalBytes * 8) / 1_000_000.0 / totalElapsedSec).toFloat()
                        else 0f
                }
            }.onFailure {
                emit(SpeedTestPhase.Error(it.message ?: AppContext.getString(R.string.speed_test_error_failed)))
                return@flow
            }

            val record = SpeedTestRecord(
                networkType = networkType,
                downloadMbps = finalMbps,
                latencyMs = latencyMs,
                recordedAt = System.currentTimeMillis()
            )
            emit(SpeedTestPhase.Done(record))
        }.flowOn(Dispatchers.IO)

    /** HEAD 请求测延迟（ms），失败返回 -1 */
    private fun measureLatency(url: String): Int = try {
        val req = Request.Builder().url(url).head().build()
        val start = System.currentTimeMillis()
        okHttpClient.newCall(req).execute().close()
        (System.currentTimeMillis() - start).toInt()
    } catch (_: Exception) {
        -1
    }

    override suspend fun saveRecord(record: SpeedTestRecord) {
        dao.insert(
            SpeedTestRecordEntity(
                networkType = record.networkType,
                downloadMbps = record.downloadMbps,
                latencyMs = record.latencyMs,
                recordedAt = record.recordedAt
            )
        )
    }

    override fun getHistory(): Flow<List<SpeedTestRecord>> =
        dao.getAll().map { list ->
            list.map { e ->
                SpeedTestRecord(
                    id = e.id,
                    networkType = e.networkType,
                    downloadMbps = e.downloadMbps,
                    latencyMs = e.latencyMs,
                    recordedAt = e.recordedAt
                )
            }
        }

    override suspend fun clearHistory() = dao.clearAll()
}

