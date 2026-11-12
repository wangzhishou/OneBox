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

package com.shifenmiao.imagegeneration.service

import com.shifenmiao.imagegeneration.model.ImageGenerationRequest
import com.shifenmiao.imagegeneration.model.ImageGenerationResult
import com.shifenmiao.imagegeneration.model.ImageProviderConfig
import com.shifenmiao.imagegeneration.model.ImageProviderDescriptor
import kotlinx.coroutines.flow.Flow

interface ImageGenerationManager {
    fun observeConfigs(): Flow<List<ImageProviderConfig>>
    fun observeActiveConfig(): Flow<ImageProviderConfig?>
    fun getConfigs(): List<ImageProviderConfig>
    fun getActiveConfig(): ImageProviderConfig?
    fun getProviderDescriptors(): List<ImageProviderDescriptor>
    fun createDefaultConfig(providerId: String): ImageProviderConfig
    fun saveConfig(config: ImageProviderConfig, makeActive: Boolean = false)
    fun deleteConfig(id: String)
    fun setActiveConfig(id: String)

    suspend fun generate(request: ImageGenerationRequest): Result<ImageGenerationResult>

    /** 使用未保存草稿调用，供设置页连通性测试。 */
    suspend fun generateWithConfig(
        config: ImageProviderConfig,
        request: ImageGenerationRequest,
    ): Result<ImageGenerationResult>

    companion object {
        /** 内置默认配置 id，不可删除。 */
        const val DEFAULT_CONFIG_ID = "default-image-provider"
    }
}
