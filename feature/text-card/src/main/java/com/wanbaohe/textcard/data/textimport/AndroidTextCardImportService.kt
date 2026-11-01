package com.wanbaohe.textcard.data.textimport

import android.content.Context
import android.net.Uri
import com.wanbaohe.textcard.domain.TextCardImportService
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class AndroidTextCardImportService @Inject constructor(
    @ApplicationContext private val context: Context,
) : TextCardImportService {

    override suspend fun readText(uri: String): String = withContext(Dispatchers.IO) {
        val input = context.contentResolver.openInputStream(Uri.parse(uri))
            ?: throw IOException("Could not open text document")
        input.bufferedReader(Charsets.UTF_8).use { reader ->
            val result = StringBuilder()
            val buffer = CharArray(8192)
            while (true) {
                val count = reader.read(buffer)
                if (count < 0) break
                if (result.length + count > MaxImportCharacters) {
                    throw IOException("Text document is too large")
                }
                result.append(buffer, 0, count)
            }
            result.toString()
        }
    }

    private companion object {
        const val MaxImportCharacters = 500_000
    }
}
