package com.shifenmiao.common.export

import android.content.Context
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.common.logic.CommonComponent
import com.shifenmiao.database.AppDatabase
import com.shifenmiao.database.agent.entity.ItemAgentEntity
import com.shifenmiao.database.chat_prompt.entity.PromptEntity
import com.shifenmiao.database.item.entity.ItemAgentLink
import com.shifenmiao.database.item.entity.ItemCategoryCrossRef
import com.shifenmiao.database.item.entity.ItemDataEntity
import com.shifenmiao.database.item.entity.ItemDataKind
import com.shifenmiao.database.item.entity.ItemDataLink
import com.shifenmiao.database.item.entity.ItemEntity
import com.shifenmiao.database.item.entity.ItemPromptLink
import com.shifenmiao.database.item.entity.ItemUserState
import com.shifenmiao.database.item.entity.ItemWithRelation
import com.shifenmiao.model.Source
import com.shifenmiao.database.item.entity.Category
import com.shifenmiao.model.ListItemType
import com.shifenmiao.network.api.ApiService
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.saving.FileController
import com.t8rin.imagetoolbox.core.settings.domain.SettingsManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

class DataSyncComponent @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    @ApplicationContext private val context: Context,
    settingsManager: SettingsManager,
    dispatchersHolder: DispatchersHolder,
    appDatabase: AppDatabase,
    apiService: ApiService,
    fileController: FileController
) : CommonComponent(
    settingsManager,
    dispatchersHolder,
    componentContext,
    appDatabase,
    apiService,
    fileController
) {

    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()

    private val _isImporting = MutableStateFlow(false)
    val isImporting: StateFlow<Boolean> = _isImporting.asStateFlow()

    @OptIn(ExperimentalSerializationApi::class)
    private val jsonFormat = Json { ignoreUnknownKeys = true; encodeDefaults = true; explicitNulls = false }

    private val _exportableItems = MutableStateFlow<List<ItemWithRelation>>(emptyList())
    val exportableItems: StateFlow<List<ItemWithRelation>> = _exportableItems.asStateFlow()

    private val _parsedImportItems = MutableStateFlow<List<ExportItemModel>>(emptyList())
    val parsedImportItems: StateFlow<List<ExportItemModel>> = _parsedImportItems.asStateFlow()

    init {
        loadExportableItems()
    }

    private fun loadExportableItems() {
        componentScope.launch(Dispatchers.IO) {
            val items = appDatabase.itemEntityDao().getExportableItems()
            _exportableItems.value = items
        }
    }

    fun parseImportData(jsonString: String, onError: (String) -> Unit) {
        componentScope.launch(Dispatchers.IO) {
            try {
                val importList = jsonFormat.decodeFromString<List<ExportItemModel>>(jsonString)
                _parsedImportItems.value = importList
            } catch (e: Exception) {
                onError(e.message ?: "解析失败")
            }
        }
    }

    fun clearParsedImportData() {
        _parsedImportItems.value = emptyList()
    }

    fun exportData(selectedIds: Set<Int>, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        componentScope.launch(Dispatchers.IO) {
            try {
                _isExporting.value = true
                val exportableItems = appDatabase.itemEntityDao().getExportableItems().filter { it.item.id in selectedIds }
                val exportList = mutableListOf<ExportItemModel>()

                for (itemWithRel in exportableItems) {
                    val item = itemWithRel.item
                    val dataList = mutableListOf<ExportDataModel>()

                    itemWithRel.data?.let { itemData ->
                        dataList.add(
                            ExportDataModel(
                                data = itemData.data,
                                url = itemData.url,
                                extra = itemData.extra
                            )
                        )
                    }

                    val agentList = mutableListOf<ExportAgentModel>()
                    itemWithRel.agent?.let {
                        agentList.add(
                            ExportAgentModel(
                                title = it.title,
                                description = it.description,
                                header = it.header,
                                body = it.body,
                                prompt = it.prompt
                            )
                        )
                    }

                    val promptList = mutableListOf<ExportPromptModel>()
                    itemWithRel.prompt?.let {
                        promptList.add(
                            ExportPromptModel(
                                title = it.title,
                                description = it.description,
                                prompt = it.prompt,
                                placeholder = it.placeholder,
                                templates = it.templates
                            )
                        )
                    }

                    val exportModel = ExportItemModel(
                        name = item.title,
                        description = item.description,
                        category = itemWithRel.categories.map { it.name },
                        listType = item.listType,
                        url = item.url,
                        miniProgramId = item.miniProgramId,
                        iconPath = item.iconPath,
                        iconName = item.iconName,
                        source = item.source.value.toString(),
                        placeholder = item.placeholder,
                        agent = agentList,
                        data = dataList,
                        prompt = promptList
                    )
                    exportList.add(exportModel)
                }

                val jsonString = jsonFormat.encodeToString(exportList)
                onSuccess(jsonString)
            } catch (e: Exception) {
                onError(e.message ?: "Export failed")
            } finally {
                _isExporting.value = false
            }
        }
    }

    fun importData(selectedItems: List<ExportItemModel>, onSuccess: () -> Unit, onError: (String) -> Unit) {
        componentScope.launch(Dispatchers.IO) {
            try {
                _isImporting.value = true
                val importList = selectedItems

                for (model in importList) {
                    if (model.name.isNullOrBlank()) continue

                    val existing = appDatabase.itemEntityDao().getItemByTitle(model.name)
                    if (existing != null) continue

                    val now = System.currentTimeMillis()
                    val itemEntity = ItemEntity(
                        id = 0,
                        remoteId = null,
                        source = Source.LOCAL,
                        listType = model.listType ?: 0,
                        title = model.name,
                        description = model.description.orEmpty(),
                        url = model.url.orEmpty(),
                        miniProgramId = model.miniProgramId.orEmpty(),
                        iconPath = model.iconPath,
                        iconName = model.iconName,
                        placeholder = model.placeholder.orEmpty(),
                        createdAt = now,
                        updatedAt = now,
                    )
                    val itemId = appDatabase.itemEntityDao().insertItem(itemEntity).toInt()

                    appDatabase.itemEntityDao().upsertUserState(
                        ItemUserState(
                            itemId = itemId,
                            isPinned = false,
                            canEdit = true,
                            updatedAt = now,
                        )
                    )

                    if (model.agent.isNotEmpty()) {
                        val agentModel = model.agent.first()
                        val agentEntity = ItemAgentEntity(
                            id = 0,
                            title = agentModel.title.orEmpty(),
                            description = agentModel.description,
                            header = agentModel.header,
                            body = agentModel.body,
                            prompt = agentModel.prompt,
                            source = Source.LOCAL,
                            updatedAt = now,
                        )
                        val agentId = appDatabase.agentDao().insertAgent(agentEntity).toInt()
                        appDatabase.agentDao().insertAgentLink(ItemAgentLink(itemId = itemId, agentId = agentId))
                    }

                    if (model.prompt.isNotEmpty()) {
                        val promptModel = model.prompt.first()
                        val promptEntity = PromptEntity(
                            id = 0,
                            title = promptModel.title.orEmpty(),
                            description = promptModel.description,
                            prompt = promptModel.prompt,
                            placeholder = promptModel.placeholder,
                            templates = promptModel.templates,
                            updatedAt = now,
                            source = Source.LOCAL
                        )
                        val promptId = appDatabase.chatPromptDao().insertPrompt(promptEntity).toInt()
                        appDatabase.chatPromptDao().insertPromptLink(ItemPromptLink(itemId = itemId, promptId = promptId))
                    }

                    if (model.data.isNotEmpty()) {
                        val dataModel = model.data.first()
                        val dataEntity = ItemDataEntity(
                            title = model.name,
                            kind = inferKind(model.listType),
                            data = dataModel.data,
                            url = dataModel.url,
                            extra = dataModel.extra,
                            source = Source.LOCAL,
                            createdAt = now,
                            updatedAt = now,
                        )
                        val dataId = appDatabase.itemDataDao().upsert(dataEntity)
                        appDatabase.itemDataDao().insertLink(ItemDataLink(itemId = itemId, dataId = dataId))
                    }

                    for (catName in model.category) {
                        if (catName.isBlank()) continue
                        var cat = appDatabase.categoryDao().getCategoryByName(catName)
                        if (cat == null) {
                            cat = Category(
                                id = 0,
                                name = catName,
                                canEdit = true,
                                source = Source.LOCAL,
                            )
                            val catId = appDatabase.categoryDao().insert(cat).toInt()
                            cat = cat.copy(id = catId)
                        }
                        appDatabase.itemEntityDao().insertItemCategoryCrossRef(
                            ItemCategoryCrossRef(itemId = itemId, categoryId = cat.id)
                        )
                    }
                }
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Import failed")
            } finally {
                _isImporting.value = false
            }
        }
    }

    private fun inferKind(listType: Int?): ItemDataKind {
        return when (ListItemType.fromId(listType)) {
            ListItemType.NOTE, ListItemType.BLOG -> ItemDataKind.MARKDOWN
            else -> ItemDataKind.HTML
        }
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit
        ): DataSyncComponent
    }
}
