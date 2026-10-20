package com.wanbaohe.recordcenter.router.screenLogic

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.core.ui.utils.BaseComponent
import com.wanbaohe.core.ui.utils.navigation.Screen
import com.wanbaohe.recordcenter.component.RecordCenterComponent
import com.wanbaohe.recordcenter.component.RecordEditorComponent
import com.wanbaohe.recordcenter.component.RecordListComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

/**
 * 记录中心路由 Component — 按 [Screen.RecordCenter.Type] 分发到聚合页 / 列表页 / 录入页。
 */
class RecordCenterRouterComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val type: Screen.RecordCenter.Type?,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigate: (Screen) -> Unit,
    private val recordCenterComponentFactory: RecordCenterComponent.Factory,
    private val recordListComponentFactory: RecordListComponent.Factory,
    private val recordEditorComponentFactory: RecordEditorComponent.Factory,
    dispatchersHolder: DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext) {

    val child: RecordCenterChild = when (type) {
        null -> RecordCenterChild.Main(
            recordCenterComponentFactory(
                componentContext = componentContext.childContext("record_center_main"),
                onGoBack = onGoBack,
                onNavigate = onNavigate,
            )
        )

        is Screen.RecordCenter.Type.RecordList -> RecordCenterChild.RecordList(
            recordListComponentFactory(
                componentContext = componentContext.childContext("record_center_list_${type.recordType}"),
                recordType = type.recordType,
                onGoBack = onGoBack,
                onNavigate = onNavigate,
            )
        )

        is Screen.RecordCenter.Type.AddRecord -> RecordCenterChild.AddRecord(
            recordEditorComponentFactory(
                componentContext = componentContext.childContext("record_center_add_${type.recordType}"),
                recordType = type.recordType,
                editingRecordId = type.editingRecordId,
                onGoBack = onGoBack,
            )
        )
    }

    sealed interface RecordCenterChild {
        class Main(val component: RecordCenterComponent) : RecordCenterChild
        class RecordList(val component: RecordListComponent) : RecordCenterChild
        class AddRecord(val component: RecordEditorComponent) : RecordCenterChild
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            type: Screen.RecordCenter.Type?,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
        ): RecordCenterRouterComponent
    }
}
