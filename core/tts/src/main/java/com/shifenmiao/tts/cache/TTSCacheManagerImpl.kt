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

package com.shifenmiao.tts.cache

import android.content.Context
import com.shifenmiao.tts.service.TTSCacheManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TTSCacheManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : TTSCacheManager {

    private val cacheDir: File by lazy {
        File(context.filesDir, CACHE_DIR_NAME).also { it.mkdirs() }
    }

    override fun getCacheFile(hash: String): File? {
        val file = File(cacheDir, "$hash.mp3")
        return if (file.exists() && file.length() > 0) file else null
    }

    override fun saveToCache(hash: String, bytes: ByteArray): File {
        cacheDir.mkdirs()
        val file = File(cacheDir, "$hash.mp3")
        file.writeBytes(bytes)
        return file
    }

    override fun deleteCache(hash: String): Boolean {
        return File(cacheDir, "$hash.mp3").delete()
    }

    override fun clearAllCache() {
        cacheDir.listFiles()?.forEach { it.delete() }
    }

    override fun getCacheSize(): Long {
        return cacheDir.listFiles()?.sumOf { it.length() } ?: 0L
    }

    companion object {
        private const val CACHE_DIR_NAME = "tts"
    }
}
