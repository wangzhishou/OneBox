package com.wanbaohe.a2ui.workbench

import android.content.Context
import android.util.AtomicFile
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
internal data class WorkbenchSnapshot(
    val id: Long,
    val source: String,
    val title: String = "",
    val renderer: String = "1.0.0-alpha02",
)

@Serializable
internal data class WorkbenchArchive(
    val draft: String? = null,
    val snapshots: List<WorkbenchSnapshot> = emptyList(),
)

internal interface WorkbenchHistory {
    suspend fun read(): WorkbenchArchive
    suspend fun update(transform: (WorkbenchArchive) -> WorkbenchArchive): WorkbenchArchive
}

@Module
@InstallIn(SingletonComponent::class)
internal interface WorkbenchHistoryModule {
    @Binds
    fun bind(implementation: LocalWorkbenchHistory): WorkbenchHistory
}

@Singleton
internal class LocalWorkbenchHistory @Inject constructor(
    @ApplicationContext context: Context,
) : WorkbenchHistory {
    private val file = AtomicFile(File(context.noBackupFilesDir, "a2ui-workbench.json"))
    private val mutex = Mutex()
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun read() = withContext(Dispatchers.IO) {
        mutex.withLock { readArchive() }
    }

    override suspend fun update(transform: (WorkbenchArchive) -> WorkbenchArchive) =
        withContext(Dispatchers.IO) {
            mutex.withLock {
                val archive = transform(readArchive())
                val stream = file.startWrite()
                try {
                    stream.write(json.encodeToString(WorkbenchArchive.serializer(), archive).encodeToByteArray())
                    file.finishWrite(stream)
                } catch (error: Exception) {
                    file.failWrite(stream)
                    throw error
                }
                archive
            }
        }

    private fun readArchive(): WorkbenchArchive = try {
        json.decodeFromString(WorkbenchArchive.serializer(), file.readFully().decodeToString())
    } catch (_: java.io.FileNotFoundException) {
        WorkbenchArchive()
    }
}
