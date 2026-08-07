package com.wanbaohe.passwordvault.router.screenLogic

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.wanbaohe.passwordvault.detail.screenLogic.PasswordVaultDetailComponent
import com.wanbaohe.passwordvault.edit.screenLogic.PasswordVaultEditComponent
import com.wanbaohe.passwordvault.list.screenLogic.PasswordVaultListComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 密码保险箱路由组件
 *
 * 通过内部 [currentType] 状态在 List / Detail / Add / Edit 子页面之间切换，
 * 参考 [com.wanbaohe.calendar.router.screenLogic.CalendarRouterComponent] 实现。
 */
class PasswordVaultRouterComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted initialType: Screen.PasswordVault.Type?,
    @Assisted val onGoBack: () -> Unit,
    private val listFactory: PasswordVaultListComponent.Factory,
    private val detailFactory: PasswordVaultDetailComponent.Factory,
    private val editFactory: PasswordVaultEditComponent.Factory,
    dispatchersHolder: DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _currentType = MutableStateFlow<Screen.PasswordVault.Type>(Screen.PasswordVault.Type.List)
    val currentType: StateFlow<Screen.PasswordVault.Type> = _currentType.asStateFlow()

    init {
        // 支持外部深链直接打开 Detail / Add / Edit 等子页面
        initialType?.let { _currentType.value = it }
    }

    fun navigateTo(type: Screen.PasswordVault.Type) {
        if (type is Screen.PasswordVault.Type.Add || type is Screen.PasswordVault.Type.Edit) {
            // 每次进入新建/编辑都重建表单组件,避免残留上一次的表单数据
            cachedEditComponent = null
        }
        _currentType.value = type
    }

    fun navigateBack() {
        when (val type = currentType.value) {
            is Screen.PasswordVault.Type.List -> onGoBack()
            is Screen.PasswordVault.Type.Detail -> navigateTo(Screen.PasswordVault.Type.List)
            is Screen.PasswordVault.Type.Add -> {
                cachedEditComponent = null
                navigateTo(Screen.PasswordVault.Type.List)
            }
            // 编辑页返回回到对应详情页,而不是直接回列表
            is Screen.PasswordVault.Type.Edit -> {
                cachedEditComponent = null
                navigateTo(Screen.PasswordVault.Type.Detail(type.entryId))
            }
        }
    }

    private val listCtx = componentContext.childContext("password_vault_list")
    private val detailCtx = componentContext.childContext("password_vault_detail")
    private val editCtx = componentContext.childContext("password_vault_edit")

    val listComponent: PasswordVaultListComponent by lazy {
        listFactory(
            componentContext = listCtx,
            onGoBack = onGoBack,
            onNavigateToType = ::navigateTo,
        )
    }

    private var cachedDetailComponent: PasswordVaultDetailComponent? = null
    private var cachedEditComponent: PasswordVaultEditComponent? = null

    fun detailComponent(entryId: String): PasswordVaultDetailComponent {
        return cachedDetailComponent?.takeIf { it.entryId == entryId }
            ?: detailFactory(
                componentContext = detailCtx,
                entryId = entryId,
                onGoBack = ::navigateBack,
                onNavigateToType = ::navigateTo,
            ).also { cachedDetailComponent = it }
    }

    fun editComponent(entryId: String?): PasswordVaultEditComponent {
        return cachedEditComponent?.takeIf { it.entryId == entryId }
            ?: editFactory(
                componentContext = editCtx,
                entryId = entryId,
                onGoBack = ::navigateBack,
                onNavigateToType = ::navigateTo,
            ).also { cachedEditComponent = it }
    }

    @AssistedFactory
    interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            initialType: Screen.PasswordVault.Type?,
            onGoBack: () -> Unit,
        ): PasswordVaultRouterComponent
    }
}
