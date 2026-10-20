/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2024 T8RIN (Malik Mukhametzyanov)
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

package com.t8rin.imagetoolbox.feature.zip.domain

interface ZipManager {

    /**
     * 解析待压缩文件的显示名、大小和类型。
     */
    suspend fun describeSources(uris: List<String>): List<ZipSourceFile>

    /**
     * 把 [files] 压缩到用户选定的 [destinationUri]。
     */
    suspend fun compress(
        files: List<String>,
        destinationUri: String,
        onProgress: (ZipProgress) -> Unit
    )

    /**
     * 读取压缩包显示名（打不开时也用它在标题栏显示文件名）。
     */
    suspend fun displayName(uri: String): String?

    /**
     * 打开一个 zip 压缩包用于浏览/解压；调用方负责 [ZipArchive.close]。
     */
    suspend fun openArchive(
        uri: String,
        onProgress: (ZipProgress) -> Unit = {}
    ): ZipArchive

}
