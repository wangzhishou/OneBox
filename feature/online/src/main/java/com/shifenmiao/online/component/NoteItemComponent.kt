package com.shifenmiao.online.component

import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.base.auth.AuthorizationCodeStateHolder
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.database.AppDatabase
import com.shifenmiao.database.item.entity.ItemWithRelation
import com.shifenmiao.database.item.entity.toModel
import com.shifenmiao.model.item.ItemDataUiState
import com.shifenmiao.model.item.ItemEntityParams
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.core.ui.utils.navigation.ScreenCallback
import com.t8rin.imagetoolbox.core.ui.utils.navigation.ScreenCallbackResult
import com.t8rin.logger.makeLog
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull


class NoteItemComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val itemEntityParams: ItemEntityParams?,
    @Assisted val onResult: ScreenCallback? = null,
    dispatchersHolder: DispatchersHolder,
    appDatabase: AppDatabase,
    private val authCodeStateHolder: AuthorizationCodeStateHolder,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val itemDao = appDatabase.itemEntityDao()
    private val itemDataDao = appDatabase.itemDataDao()

    private val _noteUIState = MutableStateFlow(ItemDataUiState())
    val noteUIState = _noteUIState.asStateFlow()

    init {
        componentScope.launch(ioDispatcher) {
            try {
                itemEntityParams?.let { params ->
                    // 如果有 id，从数据库加载最新数据
                    if (params.id != null) {
                        loadItem(itemId = params.id!!, autoRequestAuth = true)
                    } else {
                        // 如果没有 id，使用 params 中的数据（兼容旧逻辑）
                        _noteUIState.emit(
                            ItemDataUiState(
                                itemId = 0,
                                title = params.title,
                                description = params.description,
                                data = params.data.orEmpty(),
                                listType = params.listType,
                                isLoading = false
                            )
                        )
                        // 调用回调
                        onResult?.invoke(
                            ScreenCallbackResult.opened(
                                message = "笔记详情页已打开"
                            )
                        )
                    }
                } ?: run {
                    _noteUIState.emit(ItemDataUiState(isLoading = false))
                }
            } catch (e: Exception) {
                makeLog {
                    "加载 NoteItem 数据失败: ${e.message}"
                }
                _noteUIState.emit(
                    ItemDataUiState(
                        isLoading = false,
                        errorMessage = e.message ?: "加载失败"
                    )
                )
            }
        }
    }

    /**
     * 加锁笔记的解锁入口:先过全局授权码,成功后重新加载正文。
     * 取消或失败保持锁定占位,正文始终不落状态。
     */
    fun unlock() {
        val itemId = _noteUIState.value.itemId.takeIf { it > 0 } ?: return
        ActionUtils.showAuthCode(
            source = "note_item_unlock_$itemId",
            onSuccess = {
                componentScope.launch(ioDispatcher) {
                    loadItem(itemId = itemId, autoRequestAuth = false)
                }
            },
        )
    }

    /**
     * 刷新数据 - 从数据库重新加载最新数据。
     * 仍处于锁定状态时不重复弹授权框,也不加载正文。
     */
    fun refreshData() {
        componentScope.launch(ioDispatcher) {
            val itemId = itemEntityParams?.id
            if (itemId == null) {
                _noteUIState.emit(_noteUIState.value.copy(isLoading = false))
                return@launch
            }
            if (!_noteUIState.value.isLocked) {
                _noteUIState.emit(_noteUIState.value.copy(isLoading = true, errorMessage = null))
            }
            loadItem(itemId = itemId, autoRequestAuth = false)
        }
    }

    /**
     * 统一的加载收口:
     * - requiresAuth = true 且当前未授权 → 只落标题等公开信息,正文置空并标记 isLocked
     * - [autoRequestAuth] 仅在首次进入时为 true,自动拉起一次授权码;刷新路径不再打扰
     */
    private suspend fun loadItem(itemId: Int, autoRequestAuth: Boolean) {
        try {
            val itemWithRelation = itemDao.getItemById(itemId).firstOrNull()
            if (itemWithRelation == null) {
                _noteUIState.emit(ItemDataUiState(isLoading = false))
                return
            }
            val locked = itemWithRelation.isLocked()
            if (locked) {
                _noteUIState.emit(itemWithRelation.toLockedUiState())
                onResult?.invoke(
                    ScreenCallbackResult.opened(
                        id = itemWithRelation.item.id,
                        message = "笔记详情页已打开"
                    )
                )
                if (autoRequestAuth) {
                    ActionUtils.showAuthCode(
                        source = "note_item_open_$itemId",
                        onSuccess = {
                            componentScope.launch(ioDispatcher) {
                                loadItem(itemId = itemId, autoRequestAuth = false)
                            }
                        },
                    )
                }
                return
            }
            // 从 item_data 表按需加载大文本
            val itemData = itemDataDao.getByItemId(itemWithRelation.item.id)?.data ?: ""
            _noteUIState.emit(
                itemWithRelation.toUiState(itemData = itemData)
            )
            // 调用回调
            onResult?.invoke(
                ScreenCallbackResult.opened(
                    id = itemWithRelation.item.id,
                    message = "笔记详情页已打开"
                )
            )
        } catch (e: Exception) {
            makeLog {
                "加载 NoteItem 数据失败: ${e.message}"
            }
            _noteUIState.emit(
                _noteUIState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "加载失败"
                )
            )
        }
    }

    private fun ItemWithRelation.isLocked(): Boolean =
        userState?.requiresAuth == true && !authCodeStateHolder.isAuthorized

    private fun ItemWithRelation.toUiState(itemData: String) = ItemDataUiState(
        itemId = item.id,
        title = item.title,
        description = item.description,
        data = itemData,
        listType = item.listType,
        iconName = item.iconName ?: "",
        selectedCategories = categories.map { it.toModel() }.toSet(),
        isLoading = false,
        isLocked = false
    )

    private fun ItemWithRelation.toLockedUiState() = ItemDataUiState(
        itemId = item.id,
        title = item.title,
        description = item.description,
        data = "",
        listType = item.listType,
        iconName = item.iconName ?: "",
        selectedCategories = categories.map { it.toModel() }.toSet(),
        isLoading = false,
        isLocked = true
    )

    @AssistedFactory
    interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            itemEntityParams: ItemEntityParams?,
            onResult: ScreenCallback? = null
        ): NoteItemComponent
    }
}
