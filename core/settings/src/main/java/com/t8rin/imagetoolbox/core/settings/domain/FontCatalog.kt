/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * This file was newly written for OneBox and is not derived
 * from ImageToolbox source. The com.t8rin.* package name is retained
 * only for compatibility with the existing upstream module layout.
 *
 * Origin: OneBox original work (upstream package name retained)
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

package com.t8rin.imagetoolbox.core.settings.domain

import com.t8rin.imagetoolbox.core.settings.domain.model.DownloadableFont
import com.t8rin.imagetoolbox.core.settings.domain.model.FontType

/**
 * 可下载字体目录(全局):内置清单 + 下载状态查询 + 下载(产出 FontType.File)。
 * 实现侧负责持久化已下载清单与字体文件。
 */
interface FontCatalog {

    /** 内置可下载字体清单 */
    val fonts: List<DownloadableFont>

    /** 已下载则返回对应 FontType.File,否则 null(文件丢失时返回 null 并清理记录) */
    fun downloadedFont(font: DownloadableFont): FontType.File?

    /** 按本地文件路径反查清单项(命中下载清单时取本地化名称,如「霞鹜文楷体」) */
    fun fontForFile(path: String): DownloadableFont? =
        fonts.find { downloadedFont(it)?.path == path }

    /**
     * 下载字体并产出 FontType.File。
     * [onProgress] 回调 0..1 进度(无 Content-Length 时只回调 0 与 1)。
     */
    suspend fun download(
        font: DownloadableFont,
        onProgress: (Float) -> Unit,
    ): Result<FontType.File>
}
