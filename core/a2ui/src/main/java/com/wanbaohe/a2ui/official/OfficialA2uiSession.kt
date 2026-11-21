package com.wanbaohe.a2ui.official

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

interface OfficialA2uiSession : AutoCloseable {
    val surfaceIds: StateFlow<List<String>>
    val events: StateFlow<List<A2uiSessionEvent>>
    suspend fun submit(message: String)

    @Composable
    fun Surface(surfaceId: String, modifier: Modifier = Modifier)
}

sealed interface A2uiSessionEvent {
    data class Action(
        val name: String,
        val surfaceId: String,
        val componentId: String,
        val context: Map<String, Any?>,
        val dataModel: Map<String, Any?>?,
    ) : A2uiSessionEvent

    data class Error(
        val code: String,
        val surfaceId: String?,
        val detail: String,
        val path: String? = null,
    ) : A2uiSessionEvent

    data class UrlRequested(val url: String) : A2uiSessionEvent
}

typealias A2uiImageContent = @Composable (
    String, String?, ContentScale, Modifier, (Throwable?) -> Unit,
) -> Unit
typealias A2uiVideoContent = @Composable (String, Modifier, (Throwable?) -> Unit) -> Unit
typealias A2uiAudioContent = @Composable (String, String?, Modifier, (Throwable?) -> Unit) -> Unit

data class A2uiHostMedia(
    val image: A2uiImageContent,
    val video: A2uiVideoContent,
    val audio: A2uiAudioContent,
)

interface OfficialA2uiSessionFactory {
    val basicCatalogId: String
    fun create(scope: CoroutineScope, media: A2uiHostMedia): OfficialA2uiSession
}
