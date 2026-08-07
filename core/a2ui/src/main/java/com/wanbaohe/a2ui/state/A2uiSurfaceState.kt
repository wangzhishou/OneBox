package com.wanbaohe.a2ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.wanbaohe.a2ui.domain.A2uiJsonPointerResolver
import com.wanbaohe.a2ui.domain.model.A2uiComponent
import com.wanbaohe.a2ui.domain.model.A2uiMessage
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

class A2uiSurfaceState internal constructor(
    val surfaceId: String,
) {
    var catalogId: String = "basic"
        internal set

    var rootComponentId: String? by mutableStateOf(null)
        internal set

    var sendDataModel: Boolean = false
        internal set

    var surfaceProperties: JsonObject = JsonObject(emptyMap())
        internal set

    val components: SnapshotStateMap<String, A2uiComponent> = mutableStateMapOf()

    var dataModel: JsonObject by mutableStateOf(JsonObject(emptyMap()))
        internal set

    internal fun applyCreateSurface(message: A2uiMessage.CreateSurface) {
        catalogId = message.catalogId
        sendDataModel = message.sendDataModel
        surfaceProperties = message.surfaceProperties
        components.clear()
        message.components.forEach { component ->
            components[component.id] = component
        }
        rootComponentId = message.components.find { it.id == "root" }?.id
        dataModel = message.dataModel
    }

    internal fun applyUpdateComponents(message: A2uiMessage.UpdateComponents) {
        message.components.forEach { component ->
            components[component.id] = component
        }
        if (rootComponentId == null) {
            rootComponentId = message.components.find { it.id == "root" }?.id
        }
    }

    internal fun applyUpdateDataModel(message: A2uiMessage.UpdateDataModel) {
        dataModel = A2uiJsonPointerResolver.upsert(
            path = message.path,
            value = message.value,
            dataModel = dataModel,
        )
    }

    internal fun updateDataModelLocal(path: String, value: JsonElement) {
        dataModel = A2uiJsonPointerResolver.upsert(
            path = path,
            value = value,
            dataModel = dataModel,
        )
    }

    internal fun clear() {
        components.clear()
        rootComponentId = null
        dataModel = JsonObject(emptyMap())
        catalogId = "basic"
        sendDataModel = false
        surfaceProperties = JsonObject(emptyMap())
    }
}
