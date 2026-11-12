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

package com.shifenmiao.imagegeneration.loader

import com.shifenmiao.imagegeneration.model.ImageGenerationRequest
import java.io.File

data class CachedGeneratedImage(
    /** 持久化的本地文件，位于 App 工作目录(无存储权限时回退私有目录)；可直接传给 Coil/ImageGetter，跨 App 分享时需转换为 FileProvider URI。 */
    val file: File,
    val cacheKey: String,
    val fromCache: Boolean,
)

/** 将 Provider 返回的临时图片地址下载为本地持久缓存。 */
interface ImageGenerationLoader {

    /** 最简调用：同一提示词、活动配置和模型默认复用本地图片。 */
    suspend fun load(
        prompt: String,
        forceRefresh: Boolean = false,
    ): Result<CachedGeneratedImage>

    /** 高级调用：缓存键包含所有影响生成结果的请求参数。 */
    suspend fun load(
        request: ImageGenerationRequest,
        forceRefresh: Boolean = false,
    ): Result<CachedGeneratedImage>

    fun clearCache(): Result<Unit>
}
