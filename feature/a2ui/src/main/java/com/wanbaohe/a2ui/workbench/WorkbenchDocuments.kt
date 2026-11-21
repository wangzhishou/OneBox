package com.wanbaohe.a2ui.workbench

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class WorkbenchDocuments @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val resolver = context.contentResolver

    suspend fun read(uri: Uri): String = withContext(Dispatchers.IO) {
        checkNotNull(resolver.openInputStream(uri)).bufferedReader(Charsets.UTF_8).use { reader ->
            buildString {
                val buffer = CharArray(4096)
                while (true) {
                    val count = reader.read(buffer)
                    if (count < 0) break
                    require(length + count <= MAX_SOURCE)
                    append(buffer, 0, count)
                }
            }.removePrefix("\uFEFF")
        }
    }

    suspend fun write(uri: Uri, source: String) = withContext(Dispatchers.IO) {
        checkNotNull(resolver.openOutputStream(uri)).bufferedWriter(Charsets.UTF_8).use { it.write(source) }
    }
}
