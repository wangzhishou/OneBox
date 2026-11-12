/*
 * OneBox is a free AI agent and toolbox for Android
 * This file is part of OneBox, a derivative work based on
 * ImageToolbox (https://github.com/T8RIN/ImageToolbox) by T8RIN.
 *
 * Copyright (c) 2024 T8RIN (Malik Mukhametzyanov)
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * This file has been modified from the original ImageToolbox source.
 *
 * Origin: ImageToolbox derivative, modified
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

package com.t8rin.imagetoolbox.core.data.json

import com.shifenmiao.model.ModelProvider
import com.t8rin.imagetoolbox.core.domain.json.JsonParser
import com.t8rin.logger.makeLog
import java.lang.reflect.Type
import javax.inject.Inject
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

/**
 * [JsonParser] 的 kotlinx.serialization 实现(Moshi 收编,阶段④)。
 *
 * 多态判别:Moshi 的 PolymorphicJsonAdapterFactory 用 label key "Quality",
 * 这里 classDiscriminator 取同值,Quality 子类的 @SerialName 与 Moshi label
 * (PngLossy/Tiff/Base)一致,DataStore 里 Moshi 时代的旧数据可直接读。
 */
internal class KotlinxJsonParser @Inject constructor() : JsonParser {

    // classDiscriminator="Quality" 是解析器全局设置:为兼容 Moshi 时代
    // PolymorphicJsonAdapterFactory.of(Quality, "Quality") 写入 DataStore 的旧数据;
    // 经此解析器的其它 sealed 类型(ShapeType/FilenameBehavior)判别 key 也会是
    // "Quality"(值仍为各自子类名,语义错位但互不影响,这两类当前无存量数据)
    private val json = Json(ModelProvider.AppJson) {
        classDiscriminator = "Quality"
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> toJson(
        obj: T,
        type: Type,
    ): String? = runCatching {
        json.encodeToString(serializer(type) as KSerializer<T>, obj)
    }.onFailure { it.makeLog("KotlinxJsonParser toJson") }.getOrNull()

    @Suppress("UNCHECKED_CAST")
    override fun <T> fromJson(
        json: String,
        type: Type,
    ): T? = runCatching {
        this.json.decodeFromString(serializer(type) as KSerializer<T>, json)
    }.onFailure { it.makeLog("KotlinxJsonParser fromJson") }.getOrNull()

}
