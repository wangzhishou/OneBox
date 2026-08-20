package com.t8rin.imagetoolbox.feature.root.presentation.components.navigation

import javax.inject.Inject

import com.shifenmiao.ai.component.AgentJsonEditorComponent
import com.shifenmiao.lifetime.component.LifeTimeAddCountdownComponent
import com.shifenmiao.lifetime.component.LifeTimeAddEventComponent
import com.shifenmiao.lifetime.component.LifeTimeAddMilestoneComponent
import com.shifenmiao.lifetime.component.LifeTimeComponent
import com.shifenmiao.lifetime.component.LifeTimeCountdownDetailComponent
import com.shifenmiao.lifetime.component.LifeTimeMilestoneDetailComponent
import com.shifenmiao.lifetime.component.LifeTimeSettingsComponent
import com.shifenmiao.lifetime.component.LifeTimeWelcomeComponent
import com.shifenmiao.marktodo.screenLogic.MarkTodoRouterComponent
import com.shifenmiao.webview.browser.BrowserComponent
import com.wanbaohe.a2ui.viewModel.A2uiComponent
import com.wanbaohe.altitude.component.AltitudeComponent
import com.wanbaohe.blessingwall.component.BlessingRecordComponent
import com.wanbaohe.blessingwall.component.BlessingWallComponent
import com.wanbaohe.poem.component.PoemComponent
import com.wanbaohe.poem.component.PoemSearchComponent
import com.wanbaohe.bookkeeping.router.screenLogic.BookkeepingRouterComponent
import com.wanbaohe.habittracker.router.screenLogic.HabitTrackerRouterComponent
import com.wanbaohe.calendar.router.screenLogic.CalendarRouterComponent
import com.wanbaohe.camera.watermark.presentation.screenLogic.CameraWatermarkComponent
import com.wanbaohe.cloud.storage.screenLogic.CloudStorageComponent
import com.wanbaohe.compass.component.CompassComponent
import com.wanbaohe.measurement.component.MeasurementComponent
import com.wanbaohe.deadpixeltest.component.DeadPixelTestComponent
import com.wanbaohe.diceroller.component.DiceRollerComponent
import com.wanbaohe.game2048.component.Game2048Component
import com.wanbaohe.xiangqi.router.screenLogic.XiangqiRouterComponent
import com.wanbaohe.idphoto.presentation.screenLogic.IdPhotoComponent
import com.wanbaohe.markdown.edit.component.MarkdownEditorComponent
import com.wanbaohe.passwordvault.router.screenLogic.PasswordVaultRouterComponent
import com.wanbaohe.schedule.component.ScheduleComponent
import com.wanbaohe.setting.router.screenLogic.SettingRouterComponent
import com.wanbaohe.dsh.component.DshRootComponent
import com.wanbaohe.speedtest.component.SpeedTestComponent
import com.wanbaohe.survive30s.component.Survive30sComponent
import com.wanbaohe.teleprompter.component.TeleprompterComponent
import com.wanbaohe.unitconverter.component.UnitConverterComponent

/**
 * 启动期按需解析的工厂集合——Life 组。
 *
 * 由 [ChildProvider] 通过 `Provider<LifeNavigationFactories>` 持有，
 * 仅当用户首次进入本组对应 screen 时才触发 Hilt 解析本类字段，
 * 避免冷启时一次性解析全部 110 个 binding 节点。
 */
class LifeNavigationFactories @Inject constructor(
    val lifeTimeComponentFactory: LifeTimeComponent.Factory,
    val lifeTimeSettingsComponentFactory: LifeTimeSettingsComponent.Factory,
    val lifeTimeAddEventComponentFactory: LifeTimeAddEventComponent.Factory,
    val lifeTimeAddMilestoneComponentFactory: LifeTimeAddMilestoneComponent.Factory,
    val lifeTimeAddCountdownComponentFactory: LifeTimeAddCountdownComponent.Factory,
    val lifeTimeMilestoneDetailComponentFactory: LifeTimeMilestoneDetailComponent.Factory,
    val lifeTimeCountdownDetailComponentFactory: LifeTimeCountdownDetailComponent.Factory,
    val lifeTimeWelcomeComponentFactory: LifeTimeWelcomeComponent.Factory,
    val bookkeepingRouterComponentFactory: BookkeepingRouterComponent.Factory,
    val habitTrackerRouterComponentFactory: HabitTrackerRouterComponent.Factory,
    val calendarRouterComponentFactory: CalendarRouterComponent.Factory,
    val scheduleComponentFactory: ScheduleComponent.Factory,
    val markTodoRouterComponentFactory: MarkTodoRouterComponent.Factory,
    val unitConverterComponentFactory: UnitConverterComponent.Factory,
    val diceRollerComponentFactory: DiceRollerComponent.Factory,
    val game2048ComponentFactory: Game2048Component.Factory,
    val xiangqiRouterComponentFactory: XiangqiRouterComponent.Factory,
    val deadPixelTestComponentFactory: DeadPixelTestComponent.Factory,
    val minesweeperComponentFactory: com.wanbaohe.minesweeper.component.MinesweeperComponent.Factory,
    val altitudeComponentFactory: AltitudeComponent.Factory,
    val compassComponentFactory: CompassComponent.Factory,
    val speedTestComponentFactory: SpeedTestComponent.Factory,
    val dshRootComponentFactory: DshRootComponent.Factory,
    val measurementComponentFactory: MeasurementComponent.Factory,
    val teleprompterComponentFactory: TeleprompterComponent.Factory,
    val cloudStorageComponentFactory: CloudStorageComponent.Factory,
    val cameraWatermarkComponentFactory: CameraWatermarkComponent.Factory,
    val idPhotoComponentFactory: IdPhotoComponent.Factory,
    val markdownEditorComponentFactory: MarkdownEditorComponent.Factory,
    val plainTextCodeEditorComponentFactory: com.wanbaohe.code.editor.component.CodeEditorComponent.Factory,
    val survive30sComponentFactory: Survive30sComponent.Factory,
    val browserComponentFactory: BrowserComponent.Factory,
    val dataSyncComponentFactory: com.shifenmiao.common.export.DataSyncComponent.Factory,
    val codeEditorComponentFactory: AgentJsonEditorComponent.Factory,
    val settingRouterComponentFactory: SettingRouterComponent.Factory,
    val passwordVaultRouterComponentFactory: PasswordVaultRouterComponent.Factory,
    val a2uiComponentFactory: A2uiComponent.Factory,
    val blessingWallComponentFactory: BlessingWallComponent.Factory,
    val blessingRecordComponentFactory: BlessingRecordComponent.Factory,
    val poemComponentFactory: PoemComponent.Factory,
    val poemSearchComponentFactory: PoemSearchComponent.Factory,
)
