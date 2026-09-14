package com.wanbaohe.a2ui.state

import com.wanbaohe.a2ui.A2uiContentParser
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class A2uiSurfaceHolder @Inject constructor() {

    private val surfaces = mutableMapOf<String, A2uiSurfaceState>()

    @Synchronized
    fun getOrCreate(surfaceId: String): A2uiSurfaceState =
        surfaces.getOrPut(surfaceId) { A2uiSurfaceState(surfaceId) }

    /**
     * 从 A2UI JSON 直接创建 surface 并应用 createSurface,供模块外(如 ask_user 表单)同步建面。
     * JSON 非法时返回 null,调用方应跳过渲染。
     */
    fun applyCreateSurfaceJson(jsonString: String, surfaceId: String): A2uiSurfaceState? {
        val message = A2uiContentParser.parse(jsonString, surfaceId) ?: return null
        return getOrCreate(surfaceId).also { it.applyCreateSurface(message) }
    }

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
