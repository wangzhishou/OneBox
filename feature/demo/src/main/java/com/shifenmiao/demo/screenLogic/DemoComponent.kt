package com.shifenmiao.demo.screenLogic

import android.content.Context
import android.net.Uri
import com.arkivanov.decompose.ComponentContext
import com.t8rin.imagetoolbox.core.domain.image.ImageGetter
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.saving.FileController
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import android.graphics.Bitmap
import com.google.gson.Gson
import com.shifenmiao.common.handle.ItemScreenAction
import com.shifenmiao.common.handle.ItemScreenResolver
import com.shifenmiao.common.file.AigcFileMetadataReader
import com.shifenmiao.common.ai.aigc.AigcInfoGenerator
import com.shifenmiao.database.AppDatabase
import com.shifenmiao.database.schedule.entity.ScheduleEventEntity
import com.shifenmiao.database.schedule.repo.ScheduleRepository
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.model.ListItemType
import com.shifenmiao.model.ai.AIGCInfo
import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.model.ai.AiModel
import com.wanbaohe.a2ui.catalog.A2uiComponentRegistry
import com.wanbaohe.a2ui.catalog.A2uiThemeMapper
import com.wanbaohe.a2ui.state.A2uiActionBus
import com.wanbaohe.a2ui.state.A2uiSurfaceHolder
import com.wanbaohe.a2ui.transport.A2uiMessageHandler
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.catch

class DemoComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @ApplicationContext private val context: Context,
    private val appDatabase: AppDatabase,
    private val scheduleRepository: ScheduleRepository,
    private val fileController: FileController,
    private val imageGetter: ImageGetter<Bitmap>,
    val a2uiSurfaceHolder: A2uiSurfaceHolder,
    val a2uiActionBus: A2uiActionBus,
    val a2uiRegistry: A2uiComponentRegistry,
    val a2uiThemeMapper: A2uiThemeMapper,
    val a2uiMessageHandler: A2uiMessageHandler,
    dispatchersHolder: DispatchersHolder
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _state = MutableStateFlow(DemoState())
    val state: StateFlow<DemoState> = _state.asStateFlow()

    init {
        loadItemDeeplinkExamples()
        observeScheduleEvents()
    }

    private fun observeScheduleEvents() {
        componentScope.launch(ioDispatcher) {
            scheduleRepository.observeEvents()
                .catch {
                    _state.update { state ->
                        state.copy(scheduleLoadError = true)
                    }
                }
                .collect { events ->
                    _state.update { state ->
                        state.copy(
                            scheduleEvents = events.map { event -> event.toPreview() },
                            scheduleLoadError = false,
                        )
                    }
                }
        }
    }

    private fun loadItemDeeplinkExamples() {
        componentScope.launch(ioDispatcher) {
            val items = appDatabase.itemEntityDao().getItemsByCategoryIdFlow().firstOrNull().orEmpty()
            val rankedItems = items
                .filter { ItemScreenResolver.resolveForOpen(it).isSupported }
                .sortedBy { candidate ->
                    when (ListItemType.fromId(candidate.item.listType)) {
                        ListItemType.NOTE -> 0
                        ListItemType.HTML -> 1
                        ListItemType.PROMPT -> 2
                        else -> 3
                    }
                }

            val examples = buildList {
                val usedIds = mutableSetOf<Int>()

                fun addCandidate(type: ListItemType?) {
                    val candidate = rankedItems.firstOrNull {
                        it.item.id !in usedIds && ListItemType.fromId(it.item.listType) == type
                    } ?: return
                    usedIds += candidate.item.id
                    add(candidate.toItemDeeplinkExample())
                }

                addCandidate(ListItemType.NOTE)
                addCandidate(ListItemType.HTML)
                addCandidate(ListItemType.PROMPT)

                rankedItems
                    .firstOrNull { it.item.id !in usedIds }
                    ?.let { fallback ->
                        usedIds += fallback.item.id
                        add(fallback.toItemDeeplinkExample())
                    }
            }

            _state.update { it.copy(itemDeeplinkExamples = examples) }
        }
    }

    fun onPickFile(uri: Uri) {
        val uriString = uri.toString()
        _state.update {
            it.copy(
                selectedUri = uriString,
                selectedFileName = resolveDisplayName(uri),
                sourceKind = classifySource(uriString),
                isLoading = true,
                aigcInfo = null,
                parsedAigcInfo = null,
                error = null
            )
        }

        componentScope.launch {
            val mime = runCatching { context.contentResolver.getType(uri) }.getOrNull()
            val aigc = when {
                isPdf(mime, uriString) ->
                    AigcFileMetadataReader.readFromPdf(uriString, fileController)

                isImage(mime, uriString) ->
                    AigcFileMetadataReader.readFromPngImageDescription(uriString, imageGetter)

                isHtml(mime, uriString) ->
                    AigcFileMetadataReader.readFromHtml(uriString, fileController)
                        ?: run {
                            // HTML 头部没有 AIGC_INFO 时,再尝试从 PDF/PNG 兜底,
                            // 处理用户选错文件类型/后缀错的场景
                            AigcFileMetadataReader.readFromPdf(uriString, fileController)
                                ?: AigcFileMetadataReader.readFromPngImageDescription(uriString, imageGetter)
                        }

                else -> AigcFileMetadataReader.readFromPdf(uriString, fileController)
                    ?: AigcFileMetadataReader.readFromPngImageDescription(uriString, imageGetter)
                    ?: AigcFileMetadataReader.readFromHtml(uriString, fileController)
            }

            val parsed = aigc?.raw
                ?.takeIf { it.isNotBlank() }
                ?.let { runCatching { Gson().fromJson(it, AIGCInfo::class.java) }.getOrNull() }

            _state.update {
                it.copy(
                    isLoading = false,
                    aigcInfo = aigc?.raw,
                    parsedAigcInfo = parsed,
                    error = if (aigc?.raw.isNullOrBlank()) emptyAigcMessage() else null
                )
            }
        }
    }

    fun onPickFileFailed() {
        _state.update {
            it.copy(
                isLoading = false,
                error = pickFailedMessage()
            )
        }
    }

    private fun emptyAigcMessage(): String =
        AppContext.getContext().getString(com.shifenmiao.demo.R.string.demo_aigc_empty)

    private fun pickFailedMessage(): String =
        AppContext.getContext().getString(com.shifenmiao.demo.R.string.demo_aigc_pick_failed)

    private fun demoParseFailedMessage(): String =
        AppContext.getContext().getString(com.shifenmiao.demo.R.string.demo_aigc_demo_parse_failed)

    /**
     * 不依赖文件,直接生成一条演示用的 AIGC 信息,用于核对字段映射与序列化。
     */
    fun loadDemoAigcInfo() {
        _state.update {
            it.copy(
                selectedUri = null,
                selectedFileName = null,
                sourceKind = SourceKind.DEMO,
                isLoading = false,
                error = null
            )
        }
        val raw = AigcInfoGenerator.generateJson(
            engine = AiEngine(
                name = "DemoEngine",
                title = "Demo Engine",
                requestUrl = "https://example.com",
                model = AiModel(name = "demo-model", title = "Demo-Model")
            ),
            model = AiModel(name = "demo-model", title = "Demo-Model"),
            completionId = "demo-completion-0001",
            conversationId = "demo-conversation-0001",
            contentId = "demo-message-0001",
            entryTypeName = "DEMO",
            entryRefId = "demo-ref-0001"
        )
        val parsed = runCatching { Gson().fromJson(raw, AIGCInfo::class.java) }.getOrNull()
        _state.update {
            it.copy(
                aigcInfo = raw,
                parsedAigcInfo = parsed,
                error = if (parsed == null) demoParseFailedMessage() else null
            )
        }
    }

    fun clearSelection() {
        _state.update {
            it.copy(
                selectedUri = null,
                selectedFileName = null,
                sourceKind = SourceKind.NONE,
                isLoading = false,
                aigcInfo = null,
                parsedAigcInfo = null,
                error = null
            )
        }
    }

    private fun isPdf(mime: String?, uriString: String): Boolean =
        mime == "application/pdf" || uriString.endsWith(".pdf", ignoreCase = true)

    private fun isImage(mime: String?, uriString: String): Boolean =
        mime?.startsWith("image/") == true ||
            uriString.endsWith(".png", ignoreCase = true) ||
            uriString.endsWith(".jpg", ignoreCase = true) ||
            uriString.endsWith(".jpeg", ignoreCase = true) ||
            uriString.endsWith(".webp", ignoreCase = true)

    private fun isHtml(mime: String?, uriString: String): Boolean =
        mime == "text/html" || uriString.endsWith(".html", ignoreCase = true) ||
            uriString.endsWith(".htm", ignoreCase = true)

    private fun classifySource(uriString: String): SourceKind = when {
        uriString.endsWith(".pdf", ignoreCase = true) -> SourceKind.PDF
        uriString.endsWith(".png", ignoreCase = true) ||
            uriString.endsWith(".jpg", ignoreCase = true) ||
            uriString.endsWith(".jpeg", ignoreCase = true) ||
            uriString.endsWith(".webp", ignoreCase = true) -> SourceKind.IMAGE
        uriString.endsWith(".html", ignoreCase = true) ||
            uriString.endsWith(".htm", ignoreCase = true) -> SourceKind.HTML
        else -> SourceKind.UNKNOWN
    }

    private fun resolveDisplayName(uri: Uri): String {
        return runCatching {
            context.contentResolver.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val idx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        if (idx >= 0) cursor.getString(idx) else null
                    } else null
                }
        }.getOrNull() ?: uri.lastPathSegment ?: uri.toString()
    }

    data class DemoState(
        val selectedUri: String? = null,
        val selectedFileName: String? = null,
        val sourceKind: SourceKind = SourceKind.NONE,
        val isLoading: Boolean = false,
        val aigcInfo: String? = null,
        val parsedAigcInfo: AIGCInfo? = null,
        val error: String? = null,
        val itemDeeplinkExamples: List<ItemDeeplinkExample> = emptyList(),
        val scheduleEvents: List<ScheduleEventPreview> = emptyList(),
        val scheduleLoadError: Boolean = false,
    )

    enum class SourceKind {
        NONE, PDF, IMAGE, HTML, DEMO, UNKNOWN
    }

    data class ScheduleEventPreview(
        val id: String,
        val title: String,
        val description: String?,
        val providerType: String,
        val syncStatus: String,
        val linkedTaskId: String?,
        val startUtcMillis: Long,
        val endUtcMillis: Long,
        val isAllDay: Boolean,
    )

    data class ItemDeeplinkExample(
        val itemId: Int,
        val title: String,
        val typeLabel: String,
        val openDeeplink: String,
        val editDeeplink: String? = null
    )

    private fun com.shifenmiao.database.item.entity.ItemWithCategoriesAndStats.toItemDeeplinkExample(): ItemDeeplinkExample {
        val type = ListItemType.fromId(item.listType)
        val canEdit = type == ListItemType.NOTE || type == ListItemType.HTML || type == ListItemType.PROMPT
        return ItemDeeplinkExample(
            itemId = item.id,
            title = item.title.ifBlank { "Item ${item.id}" },
            typeLabel = type?.name ?: "UNKNOWN",
            openDeeplink = ItemScreenResolver.buildDeeplink(item, ItemScreenAction.OPEN),
            editDeeplink = if (canEdit) {
                ItemScreenResolver.buildDeeplink(item, ItemScreenAction.EDIT)
            } else {
                null
            }
        )
    }

    private fun ScheduleEventEntity.toPreview(): ScheduleEventPreview {
        return ScheduleEventPreview(
            id = id,
            title = title,
            description = description,
            providerType = providerType,
            syncStatus = syncStatus,
            linkedTaskId = linkedTaskId,
            startUtcMillis = startUtcMillis,
            endUtcMillis = endUtcMillis,
            isAllDay = isAllDay,
        )
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext
        ): DemoComponent
    }

}
