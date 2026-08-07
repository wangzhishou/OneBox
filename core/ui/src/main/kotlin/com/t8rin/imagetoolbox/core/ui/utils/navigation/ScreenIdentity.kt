package com.t8rin.imagetoolbox.core.ui.utils.navigation

import java.util.Locale

internal fun Screen.routeKey(): String {
    val base = simpleName.ifBlank { this::class.simpleName.orEmpty().ifBlank { "screen_$id" } }
    return base
        .trim()
        .replace(Regex("([a-z0-9])([A-Z])"), "$1_$2")
        .replace(Regex("[^A-Za-z0-9]+"), "_")
        .trim('_')
        .lowercase(Locale.ROOT)
        .ifBlank { "screen_$id" }
}

internal fun Screen.canonicalName(): String = "screen.${routeKey()}"

internal fun Screen.slug(): String = canonicalName().replace('.', '-')
