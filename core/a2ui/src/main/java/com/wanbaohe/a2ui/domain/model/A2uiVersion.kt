package com.wanbaohe.a2ui.domain.model

object A2uiVersion {
    const val V1_0 = "v1.0"

    val supported: Set<String> = setOf(V1_0)

    fun isSupported(version: String): Boolean = version in supported
}
