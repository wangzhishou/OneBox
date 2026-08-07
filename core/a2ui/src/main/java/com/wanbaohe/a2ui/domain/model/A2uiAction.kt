package com.wanbaohe.a2ui.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class A2uiAction(
    val event: A2uiActionEvent? = null,
    val functionCall: A2uiFunctionCall? = null,
) {
    init {
        require(event != null || functionCall != null) {
            "A2uiAction must contain either event or functionCall"
        }
    }
}

@Serializable
data class A2uiActionEvent(
    val name: String,
    val context: JsonObject? = null,
    val wantResponse: Boolean = false,
    val responsePath: String? = null,
)

@Serializable
data class A2uiFunctionCall(
    val call: String,
    val args: JsonObject? = null,
)
