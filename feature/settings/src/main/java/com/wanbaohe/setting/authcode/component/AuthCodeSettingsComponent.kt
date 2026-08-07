package com.wanbaohe.setting.authcode.component

import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.base.authcode.AuthCodeService
import com.shifenmiao.common.handle.ItemResourceRef
import com.shifenmiao.common.handle.ItemResourceResolver
import com.shifenmiao.database.AppDatabase
import com.shifenmiao.database.item.entity.ItemWithCategories
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * 授权码设置 / 修改页业务组件。
 *
 * 业务动作封装在 [AuthCodeService] 中(由 Hilt 注入,基于 Room 持久化);
 * item 关联资源解析由本组件直接调 [ItemResourceResolver] 编排(避免在
 * `core:base` 上引入对 `feature:common` 的反向依赖)。
 */
class AuthCodeSettingsComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    private val authCodeService: AuthCodeService,
    private val appDatabase: AppDatabase,
    dispatchersHolder: com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext) {

    suspend fun hasCode(): Boolean = withContext(ioDispatcher) {
        authCodeService.hasCode()
    }

    suspend fun verifyCode(code: String): Boolean = withContext(ioDispatcher) {
        authCodeService.verify(code)
    }

    suspend fun setCode(code: String) = withContext(ioDispatcher) {
        authCodeService.setCode(code)
    }

    suspend fun clearCode() = withContext(ioDispatcher) {
        authCodeService.clear()
    }

    suspend fun clearCodeAndAllProtection() = withContext(ioDispatcher) {
        authCodeService.clearCodeAndAllProtection()
    }

    fun observeProtectedItems(): Flow<List<ItemWithCategories>> =
        authCodeService.observeProtectedItems()

    suspend fun disableProtection(itemId: Int) = withContext(ioDispatcher) {
        authCodeService.disableProtection(itemId)
    }

    suspend fun loadItemResource(itemId: Int, listType: Int?): ItemResourceRef =
        withContext(ioDispatcher) {
            ItemResourceResolver.resolve(appDatabase, itemId, listType)
        }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit,
        ): AuthCodeSettingsComponent
    }
}
