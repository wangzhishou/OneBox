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

package com.shifenmiao.network.downloader

import android.content.Context
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.saving.KeepAliveService
import com.t8rin.imagetoolbox.core.resources.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

data class BackgroundDownloadRequest(
    val id: String,
    val url: String,
    val destination: File,
    val title: String,
    val overwrite: Boolean = false,
)

sealed interface BackgroundDownloadState {
    val request: BackgroundDownloadRequest

    data class Running(
        override val request: BackgroundDownloadRequest,
        val downloadedBytes: Long = 0L,
        val totalBytes: Long? = null,
    ) : BackgroundDownloadState {
        val progress: Float?
            get() = totalBytes?.takeIf { it > 0L }?.let {
                (downloadedBytes.toDouble() / it).toFloat().coerceIn(0f, 1f)
            }
    }

    data class Success(
        override val request: BackgroundDownloadRequest,
        val file: File,
    ) : BackgroundDownloadState

    data class Failed(
        override val request: BackgroundDownloadRequest,
        val cause: Throwable,
    ) : BackgroundDownloadState
}

@Singleton
class BackgroundDownloadService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val keepAliveService: KeepAliveService,
    dispatchersHolder: DispatchersHolder,
) {
    private val scope = CoroutineScope(SupervisorJob() + dispatchersHolder.ioDispatcher)
    private val downloader = ResumableFileDownloader(
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .build()
    )
    private val activeJobs = ConcurrentHashMap<String, Job>()
    private val startLock = Any()
    private val _downloads = MutableStateFlow<Map<String, BackgroundDownloadState>>(emptyMap())

    val downloads: StateFlow<Map<String, BackgroundDownloadState>> = _downloads.asStateFlow()

    private var isNotificationShown = false
    private var lastNotificationAt = 0L

    fun start(request: BackgroundDownloadRequest) {
        synchronized(startLock) {
            if (activeJobs[request.id]?.isActive == true) return
            if (request.destination.exists() && !request.overwrite) {
                _downloads.update {
                    it + (request.id to BackgroundDownloadState.Success(request, request.destination))
                }
                return
            }

            _downloads.update { it + (request.id to BackgroundDownloadState.Running(request)) }
            updateNotification(force = true)
            lateinit var job: Job
            job = scope.launch(start = CoroutineStart.LAZY) {
                execute(request, job)
            }
            activeJobs[request.id] = job
            job.start()
        }
    }

    fun cancel(id: String) {
        activeJobs.remove(id)?.cancel()
        _downloads.update { it - id }
        updateNotification(force = true)
    }

    fun clear(id: String) {
        if (activeJobs[id]?.isActive == true) return
        _downloads.update { it - id }
    }

    private suspend fun execute(request: BackgroundDownloadRequest, job: Job) {
        try {
            downloader.download(request.url, request.destination).collect { state ->
                if (activeJobs[request.id] != job) return@collect
                when (state) {
                    is DownloadState.Progress -> {
                        _downloads.update {
                            it + (request.id to BackgroundDownloadState.Running(
                                request = request,
                                downloadedBytes = state.downloadedBytes,
                                totalBytes = state.totalBytes,
                            ))
                        }
                        updateNotification()
                    }

                    is DownloadState.Success -> {
                        _downloads.update {
                            it + (request.id to BackgroundDownloadState.Success(request, state.file))
                        }
                    }

                    is DownloadState.Failed -> {
                        _downloads.update {
                            it + (request.id to BackgroundDownloadState.Failed(request, state.throwable))
                        }
                    }
                }
            }
        } catch (cancelled: CancellationException) {
            if (activeJobs[request.id] == job) {
                _downloads.update { it - request.id }
            }
        } catch (error: Throwable) {
            if (activeJobs[request.id] == job) {
                _downloads.update {
                    it + (request.id to BackgroundDownloadState.Failed(request, error))
                }
            }
        } finally {
            activeJobs.remove(request.id, job)
            updateNotification(force = true)
        }
    }

    @Synchronized
    private fun updateNotification(force: Boolean = false) {
        val running = _downloads.value.values.filterIsInstance<BackgroundDownloadState.Running>()
        if (running.isEmpty()) {
            if (isNotificationShown) {
                keepAliveService.stop()
                isNotificationShown = false
            }
            lastNotificationAt = 0L
            return
        }

        val now = System.currentTimeMillis()
        if (!force && now - lastNotificationAt < NOTIFICATION_INTERVAL_MS) return

        val knownProgress = running.all { it.progress != null }
        val progress = if (knownProgress) {
            val downloaded = running.sumOf { it.downloadedBytes }
            val total = running.sumOf { it.totalBytes ?: 0L }
            (downloaded.toDouble() / total).toFloat().coerceIn(0f, 1f)
        } else {
            KeepAliveService.PROGRESS_INDETERMINATE
        }
        val description = if (running.size == 1) {
            running.single().request.title
        } else {
            context.getString(R.string.background_download_count, running.size)
        }
        val progressText = progress
            .takeIf { it >= 0f }
            ?.let {
                context.getString(
                    R.string.background_download_progress,
                    description,
                    (it * 100).roundToInt(),
                )
            }

        keepAliveService.updateOrStart(
            title = context.getString(R.string.background_downloading),
            description = progressText ?: description,
            progress = progress,
        )
        isNotificationShown = true
        lastNotificationAt = now
    }

    private companion object {
        const val NOTIFICATION_INTERVAL_MS = 500L
    }
}
