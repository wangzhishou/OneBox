package com.wanbaohe.a2ui.state

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class A2uiSurfaceHolder @Inject constructor() {

    private val surfaces = mutableMapOf<String, A2uiSurfaceState>()

    @Synchronized
    fun getOrCreate(surfaceId: String): A2uiSurfaceState =
        surfaces.getOrPut(surfaceId) { A2uiSurfaceState(surfaceId) }

    @Synchronized
    fun get(surfaceId: String): A2uiSurfaceState? = surfaces[surfaceId]

    @Synchronized
    fun remove(surfaceId: String) {
        surfaces.remove(surfaceId)?.clear()
    }

    @Synchronized
    fun clearAll() {
        surfaces.values.forEach { it.clear() }
        surfaces.clear()
    }
}
