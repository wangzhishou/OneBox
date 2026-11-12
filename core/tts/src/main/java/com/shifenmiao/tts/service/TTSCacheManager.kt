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

package com.shifenmiao.tts.service

import java.io.File

/**
 * TTS 音频文件缓存管理器。
 *
 * 缓存目录位于 app filesDir/tts/，使用 filesDir 而非 cacheDir，
 * 确保系统不会自动清理用户生成的音频。
 */
interface TTSCacheManager {

    /**
     * 根据哈希获取缓存文件。
     * @return 文件存在且大小大于 0 时返回，否则 null
     */
    fun getCacheFile(hash: String): File?

    /**
     * 将字节数据保存到缓存。
     * @return 保存后的文件
     */
    fun saveToCache(hash: String, bytes: ByteArray): File

    /**
     * 删除指定哈希对应的缓存文件。
     */
    fun deleteCache(hash: String): Boolean

    /**
     * 清除所有缓存文件。
     */
    fun clearAllCache()

    /**
     * 获取缓存目录总大小（字节）。
     */
    fun getCacheSize(): Long
}
