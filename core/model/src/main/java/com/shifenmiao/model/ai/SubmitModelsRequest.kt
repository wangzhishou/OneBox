package com.shifenmiao.model.ai

import kotlinx.serialization.Serializable

@Serializable
data class SubmitModelsRequest(
    val engineName: String,
    val models: List<ModelDraft>
)

@Serializable
data class ModelDraft(
    val name: String,
    val title: String
)
