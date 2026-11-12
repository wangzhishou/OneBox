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

package com.shifenmiao.model.prompt

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * DTO stands for Data Transfer Object.
 */
@Parcelize
@Serializable
data class PromptWithGroupsDTO(
    val prompt: PromptDTO,
    val groups: List<GroupDTO>
) : Parcelable

@Parcelize
@Serializable
data class PromptDTO(
    val id: Int,
    val name: String,
    val emoji: String,
    val prompt: String,
    val description: String,
    val templates: String = "",
    val placeholder: String = "",
    // 原 @Contextual Date:该类无调用方,kotlinx 无 Date serializer,降为 epoch millis
    val updateTime: Long,
    val canEdit: Boolean
) : Parcelable

@Parcelize
@Serializable
data class GroupDTO(
    val groupId: Int,
    val name: String
) : Parcelable