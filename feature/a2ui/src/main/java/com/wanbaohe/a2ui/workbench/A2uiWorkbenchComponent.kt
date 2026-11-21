package com.wanbaohe.a2ui.workbench

import android.net.Uri
import com.arkivanov.decompose.ComponentContext
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.a2ui.official.OfficialA2uiSession
import com.wanbaohe.a2ui.official.OfficialA2uiSessionFactory
import com.wanbaohe.core.ui.utils.BaseComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal data class WorkbenchState(
    val ready: Boolean = false,
    val viewingSnapshot: Boolean = false,
    val source: String = "",
    val snapshots: List<WorkbenchSnapshot> = emptyList(),
    val session: OfficialA2uiSession? = null,
    val sent: Int = 0,
    val total: Int = 0,
    val error: Int? = null,
)

class A2uiWorkbenchComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    dispatchersHolder: DispatchersHolder,
    private val sessions: OfficialA2uiSessionFactory,
    private val history: WorkbenchHistory,
    private val samples: WorkbenchSamples,
    private val media: WorkbenchMedia,
    private val documents: WorkbenchDocuments,
) : BaseComponent(dispatchersHolder, componentContext) {
    private val mutable = MutableStateFlow(WorkbenchState())
    internal val state = mutable.asStateFlow()
    private var playback: Job? = null
    private var draftWrite: Job? = null
    private var frames = emptyList<String>()
    private var runSource: String? = null
    private var draft = ""

    init {
        componentScope.launch {
            val archive = attempt(R.string.a2wb_storage_error) { history.read() }
            val source = archive?.draft ?: sampleSource(WorkbenchSample.Form)
            draft = source
            mutable.update { it.copy(ready = true, source = source, snapshots = archive?.snapshots.orEmpty()) }
            if (source.isNotBlank()) runAll()
        }
    }

    internal fun edit(source: String) {
        if (source.length > MAX_SOURCE) {
            mutable.update { it.copy(error = R.string.a2wb_input_error) }
            return
        }
        mutable.update { it.copy(source = source, error = null) }
        if (state.value.viewingSnapshot) return
        draft = source
        draftWrite?.cancel()
        draftWrite = componentScope.launch {
            delay(350)
            persistDraft(source)
        }
    }

    internal fun goBack() {
        val source = draft
        draftWrite?.cancel()
        componentScope.launch {
            if (state.value.ready) persistDraft(source)
            onGoBack()
        }
    }

    private suspend fun persistDraft(source: String) {
        attempt(R.string.a2wb_storage_error) { history.update { it.copy(draft = source) } }
    }

    internal fun loadSample(sample: WorkbenchSample) = open(sampleSource(sample))
    private fun sampleSource(sample: WorkbenchSample) = samples.source(sample, sessions.basicCatalogId)
    private fun open(source: String) {
        edit(source)
        runAll()
    }

    internal fun openSnapshot(source: String) {
        mutable.update { it.copy(viewingSnapshot = true) }
        open(source)
    }

    internal fun returnToDraft() {
        mutable.update { it.copy(viewingSnapshot = false) }
        open(draft)
    }

    internal fun importSource(uri: Uri) {
        componentScope.launch {
            attempt(R.string.a2wb_file_error) { documents.read(uri) }?.let(::open)
        }
    }

    internal fun exportSource(uri: Uri, source: String) {
        componentScope.launch {
            attempt(R.string.a2wb_file_error) { documents.write(uri, source) }
        }
    }

    internal fun format() {
        val current = state.value.source
        componentScope.launch {
            val formatted = attempt(R.string.a2wb_input_error) {
                withContext(Dispatchers.Default) { WorkbenchSource.format(current) }
            }
            if (formatted != null && state.value.source == current) edit(formatted)
        }
    }

    internal fun reset() {
        playback?.cancel()
        startSession()
    }

    internal fun runAll() = play(restart = true, single = false)
    internal fun step() = play(restart = false, single = true)

    private fun play(restart: Boolean, single: Boolean) {
        playback?.cancel()
        playback = componentScope.launch {
            val source = state.value.source
            if (restart || source != runSource || state.value.session == null) {
                val messages = attempt(R.string.a2wb_input_error) {
                    withContext(Dispatchers.Default) { WorkbenchSource.frames(source) }
                } ?: return@launch
                startSession(source, messages)
            }
            val session = state.value.session ?: return@launch
            do {
                val next = state.value.sent
                val frame = frames.getOrNull(next) ?: break
                attempt(R.string.a2wb_input_error) { session.submit(frame) } ?: break
                mutable.update { it.copy(sent = next + 1) }
            } while (!single)
        }
    }

    private fun startSession(source: String? = null, messages: List<String> = emptyList()) {
        state.value.session?.close()
        runSource = source
        frames = messages
        val session = if (source == null) null else sessions.create(componentScope, media.host)
        mutable.update { it.copy(session = session, sent = 0, total = frames.size, error = null) }
    }

    internal fun save(title: String) {
        val source = state.value.source
        if (source.isBlank()) return
        changeHistory { archive ->
            val id = maxOf(System.currentTimeMillis(), (archive.snapshots.maxOfOrNull { it.id } ?: 0L) + 1)
            val snapshot = WorkbenchSnapshot(id, source, title.trim().take(80))
            archive.copy(snapshots = (listOf(snapshot) + archive.snapshots).take(30))
        }
    }

    internal fun rename(id: Long, title: String) = changeHistory { archive ->
        archive.copy(snapshots = archive.snapshots.map {
            if (it.id == id) it.copy(title = title.trim().take(80)) else it
        })
    }

    internal fun delete(id: Long) = changeHistory { archive ->
        archive.copy(snapshots = archive.snapshots.filterNot { it.id == id })
    }

    internal fun clearHistory() = changeHistory { it.copy(snapshots = emptyList()) }

    private fun changeHistory(transform: (WorkbenchArchive) -> WorkbenchArchive) {
        componentScope.launch {
            val archive = attempt(R.string.a2wb_storage_error) { history.update(transform) }
            if (archive != null) mutable.update { it.copy(snapshots = archive.snapshots) }
        }
    }

    private suspend fun <T> attempt(errorId: Int, action: suspend () -> T): T? = try {
        action()
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (_: Exception) {
        mutable.update { it.copy(error = errorId) }
        null
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(componentContext: ComponentContext, onGoBack: () -> Unit): A2uiWorkbenchComponent
    }
}
