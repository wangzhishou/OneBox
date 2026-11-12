/*
 * OneBox is a free AI agent and toolbox for Android
 * This file is part of OneBox, a derivative work based on
 * ImageToolbox (https://github.com/T8RIN/ImageToolbox) by T8RIN.
 *
 * Copyright (c) 2025 T8RIN (Malik Mukhametzyanov)
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

@file:Suppress("NOTHING_TO_INLINE")

package com.t8rin.imagetoolbox.core.domain.image

import com.t8rin.imagetoolbox.core.domain.image.model.MetadataTag

interface Metadata {
    fun saveAttributes(): Metadata

    fun getAttribute(tag: MetadataTag): String?

    fun setAttribute(
        tag: MetadataTag,
        value: String?
    ): Metadata
}

inline operator fun Metadata.get(
    tag: MetadataTag
): String? = getAttribute(tag)

inline operator fun Metadata.set(
    tag: MetadataTag,
    value: String?
): Metadata = setAttribute(tag, value)

inline fun Metadata.clearAttribute(
    tag: MetadataTag
) = apply {
    setAttribute(
        tag = tag,
        value = null
    )
}

inline fun Metadata.clearAttributes(
    attributes: List<MetadataTag>
): Metadata = apply {
    attributes.forEach(::clearAttribute)
}

inline fun Metadata.clearAllAttributes(): Metadata =
    clearAttributes(attributes = MetadataTag.entries)

inline fun Metadata.toMap(): Map<MetadataTag, String> = mutableMapOf<MetadataTag, String>().apply {
    MetadataTag.entries.forEach { tag ->
        getAttribute(tag)?.let { put(tag, it) }
    }
}

inline fun Metadata.copyTo(metadata: Metadata): Metadata {
    MetadataTag.entries.forEach { attr ->
        getAttribute(attr).let { metadata.setAttribute(attr, it) }
    }
    metadata.saveAttributes()

    return metadata
}

fun interface MetadataProvider {
    suspend fun readMetadata(imageUri: String): Metadata?
}