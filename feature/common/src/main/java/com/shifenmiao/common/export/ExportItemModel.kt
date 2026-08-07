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
