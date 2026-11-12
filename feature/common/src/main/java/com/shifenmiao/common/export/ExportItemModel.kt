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

package com.shifenmiao.common.export

import kotlinx.serialization.Serializable

@Serializable
data class ExportItemModel(
    val title: String? = null,
    val name: String? = null,
    val description: String? = null,
    val category: List<String> = emptyList(),
    val listType: Int? = null,
    val url: String? = null,
    val miniProgramId: String? = null,
    val iconPath: String? = null,
    val iconName: String? = null,
    val source: String? = null,
    val placeholder: String? = null,
    val agent: List<ExportAgentModel> = emptyList(),
    val data: List<ExportDataModel> = emptyList(),
    val prompt: List<ExportPromptModel> = emptyList()
)

@Serializable
data class ExportAgentModel(
    val title: String? = null,
    val description: String? = null,
    val header: String? = null,
    val body: String? = null,
    val prompt: String? = null
)

@Serializable
data class ExportDataModel(
    val data: String? = null,
    val url: String? = null,
    val extra: String? = null
)

@Serializable
data class ExportPromptModel(
    val title: String? = null,
    val description: String? = null,
    val prompt: String? = null,
    val placeholder: String? = null,
    val templates: String? = null
)
