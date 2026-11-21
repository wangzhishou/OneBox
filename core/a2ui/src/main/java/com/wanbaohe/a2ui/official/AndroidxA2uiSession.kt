package com.wanbaohe.a2ui.official

import android.content.Context
import androidx.a2ui.compose.runtime.A2uiMessageParser
import androidx.a2ui.compose.ui.A2uiMessageProcessor
import androidx.a2ui.compose.ui.catalog.A2uiBasicCatalogV1
import androidx.a2ui.model.processor.processInput
import androidx.a2ui.model.protocol.A2uiClientErrorMessage
import androidx.a2ui.model.protocol.A2uiClientEventMessage
import androidx.a2ui.model.protocol.A2uiExecutionContext
import androidx.compose.material3.a2ui.A2uiSurface
import androidx.compose.material3.a2ui.catalog.MaterialA2uiBasicCatalogV1Defaults
import androidx.compose.material3.a2ui.catalog.materialA2uiBasicCatalogV1
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Module
@InstallIn(SingletonComponent::class)
internal interface OfficialA2uiModule {
    @Binds
    fun bindFactory(impl: AndroidxA2uiSessionFactory): OfficialA2uiSessionFactory
}

internal class AndroidxA2uiSessionFactory @Inject constructor(
    @ApplicationContext private val context: Context,
) : OfficialA2uiSessionFactory {
    override val basicCatalogId get() = A2uiBasicCatalogV1.CatalogId

    override fun create(scope: CoroutineScope, media: A2uiHostMedia): OfficialA2uiSession =
        AndroidxA2uiSession(scope, context, media)
}

private class AndroidxA2uiSession(
    parentScope: CoroutineScope,
    context: Context,
    media: A2uiHostMedia,
) : OfficialA2uiSession {
    private val job = Job(parentScope.coroutineContext[Job])
    private val scope = CoroutineScope(parentScope.coroutineContext + job + Dispatchers.Default)
    private val parser = A2uiMessageParser()
    private val eventState = MutableStateFlow<List<A2uiSessionEvent>>(emptyList())
    private val functions = object : A2uiBasicCatalogV1.Functions by
        A2uiBasicCatalogV1.Functions(context) {
        override fun openUrl(url: String, executionContext: A2uiExecutionContext) {
            record(A2uiSessionEvent.UrlRequested(url))
        }
    }
    private val processor = A2uiMessageProcessor(
        catalogs = listOf(
            materialA2uiBasicCatalogV1(
                image = MaterialA2uiBasicCatalogV1Defaults.image { url, description, scale, modifier, error ->
                    media.image(url, description, scale, modifier, error)
                },
                video = MaterialA2uiBasicCatalogV1Defaults.video { url, modifier, error ->
                    media.video(url, modifier, error)
                },
                audioPlayer = MaterialA2uiBasicCatalogV1Defaults.audioPlayer { url, description, modifier, error ->
                    media.audio(url, description, modifier, error)
                },
                functions = functions,
                card = SessionCard,
                modal = SessionModal,
                button = SessionButton,
                textField = SessionTextField,
                checkBox = SessionCheckBox,
                choicePicker = SessionChoicePicker,
                slider = SessionSlider,
            )
        )
    )

    override val events: kotlinx.coroutines.flow.StateFlow<List<A2uiSessionEvent>> = eventState
    override val surfaceIds = processor.activeSurfaces.map { models -> models.map { it.id } }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    init {
        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            processor.outboundEvents.collect { event ->
                record(
                    when (event) {
                        is A2uiClientEventMessage -> A2uiSessionEvent.Action(
                            event.type, event.surfaceId, event.componentId, event.context,
                            event.clientDataModel?.surfaces,
                        )
                        is A2uiClientErrorMessage -> A2uiSessionEvent.Error(
                            event.code, event.surfaceId, event.message, event.context["path"] as? String,
                        )
                    }
                )
            }
        }
        scope.launch { processor.collectMessages() }
    }

    override suspend fun submit(message: String) = withContext(Dispatchers.Default) {
        job.ensureActive()
        processor.processInput(parser, message)
    }

    private fun record(event: A2uiSessionEvent) {
        eventState.update { (it + event).takeLast(60) }
    }

    @Composable
    override fun Surface(surfaceId: String, modifier: Modifier) {
        val surface = processor.activeSurfaces.collectAsState().value
            .firstOrNull { it.id == surfaceId } ?: return
        A2uiSurface(surfaceModel = surface, modifier = modifier)
    }

    override fun close() = job.cancel()
}
