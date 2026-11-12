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
