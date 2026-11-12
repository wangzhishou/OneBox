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

package com.t8rin.imagetoolbox.core.domain.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

import com.t8rin.imagetoolbox.core.domain.model.HashingType.Companion.registerSecurityMessageDigests


@Serializable(HashingTypeKSerializer::class)
@ConsistentCopyVisibility
/**
 * [HashingType] multiplatform domain wrapper for java MessageDigest, in order to add custom digests, you need to call [registerSecurityMessageDigests] when process created
 **/
data class HashingType private constructor(
    val digest: String,
    val name: String = digest
) {
    companion object {
        val MD5 = HashingType("MD5")
        val SHA_1 = HashingType("SHA-1")
        val SHA_224 = HashingType("SHA-224")
        val SHA_256 = HashingType("SHA-256")
        val SHA_384 = HashingType("SHA-384")
        val SHA_512 = HashingType("SHA-512")

        private var securityMessageDigests: List<String>? = null

        fun registerSecurityMessageDigests(digests: List<String>) {
            if (!securityMessageDigests.isNullOrEmpty()) {
                throw IllegalArgumentException("SecurityMessageDigests already registered")
            }
            securityMessageDigests = digests.distinctBy { it.replace("OID.", "").uppercase() }
        }

        val entries: List<HashingType> by lazy {
            val available = securityMessageDigests?.mapNotNull { messageDigest ->
                if (messageDigest.isEmpty()) null
                else {
                    val digest = messageDigest.replace("OID.", "")
                    SecureAlgorithmsMapping.findMatch(digest)?.let { mapping ->
                        HashingType(
                            digest = messageDigest,
                            name = mapping.algorithm
                        )
                    } ?: HashingType(digest = messageDigest)
                }
            }?.sortedBy { it.digest } ?: emptyList()

            listOf(
                MD5,
                SHA_1,
                SHA_224,
                SHA_256,
                SHA_384,
                SHA_512,
            ).let {
                it + available
            }.distinctBy { it.name.replace("-", "") }
        }

        fun fromString(
            digest: String?
        ): HashingType? = digest?.let {
            entries.find {
                it.digest == digest
            }
        }
    }
}
/**
 * [HashingType] 的 kotlinx 序列化器(Gson/Moshi → kotlinx 收编):
 * 写出与 Moshi KotlinJsonAdapterFactory 相同的对象形状 {"digest","name"};
 * 读取经 [HashingType.entries] 注册表匹配,未知值抛异常由上层按默认值兜底
 * (与 Moshi 时代私有构造导致解析失败 → 默认值的最终行为一致)。
 */
object HashingTypeKSerializer : KSerializer<HashingType> {

    @Serializable
    private data class Surrogate(
        val digest: String,
        val name: String = digest
    )

    override val descriptor: SerialDescriptor = Surrogate.serializer().descriptor

    override fun serialize(encoder: Encoder, value: HashingType) {
        encoder.encodeSerializableValue(Surrogate.serializer(), Surrogate(value.digest, value.name))
    }

    override fun deserialize(decoder: Decoder): HashingType {
        val surrogate = decoder.decodeSerializableValue(Surrogate.serializer())
        return HashingType.entries.firstOrNull {
            it.digest.equals(surrogate.digest, ignoreCase = true) ||
                it.name.equals(surrogate.name, ignoreCase = true)
        } ?: throw SerializationException("Unknown HashingType: ${surrogate.digest}")
    }
}
