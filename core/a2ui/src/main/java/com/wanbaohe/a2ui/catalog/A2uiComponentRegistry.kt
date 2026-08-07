package com.wanbaohe.a2ui.catalog

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class A2uiComponentRegistry @Inject constructor(
    renderers: Set<@JvmSuppressWildcards A2uiComponentRenderer>,
) {
    private val rendererMap: MutableMap<String, A2uiComponentRenderer> =
        renderers.associateBy { it.componentType }.toMutableMap()

    fun get(type: String): A2uiComponentRenderer? = rendererMap[type]

    fun register(renderer: A2uiComponentRenderer) {
        rendererMap[renderer.componentType] = renderer
    }

    fun allTypes(): Set<String> = rendererMap.keys.toSet()
}
