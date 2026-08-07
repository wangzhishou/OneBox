package com.wanbaohe.blessingwall.component

import android.graphics.Bitmap
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.storage.RemoteConfigStorage
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.image.ImageShareProvider
import com.t8rin.imagetoolbox.core.domain.image.model.ImageFormat
import com.t8rin.imagetoolbox.core.domain.image.model.ImageInfo
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.wanbaohe.blessingwall.model.BlessingType
import com.wanbaohe.blessingwall.service.BlessingService
import com.wanbaohe.blessingwall.service.BlessingSoundService
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class BlessingWallComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted("date") date: String?,
    @Assisted("initialType") initialType: String?,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigate: (Screen) -> Unit,
    private val blessingService: BlessingService,
    private val soundService: BlessingSoundService,
    private val shareProvider: ImageShareProvider<Bitmap>,
    dispatchersHolder: DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext) {

    /** 当前展示的日期（yyyy-MM-dd）。传入非法日期时回退到今天。 */
    val targetDate: String = parseDateOrToday(date, blessingService)

    /** 历史模式：展示过去日期的数据，页面只读。 */
    val isHistoryMode: Boolean = targetDate != blessingService.todayString()

    /** 初始展示的 tab 页下标。 */
    val initialPage: Int = BlessingType.fromKey(initialType.orEmpty())?.ordinal ?: 0

    private val _uiState = MutableStateFlow(BlessingWallUiState())
    val uiState = _uiState.asStateFlow()

    init {
        val countsFlow = if (isHistoryMode) {
            blessingService.observeCounts(targetDate)
        } else {
            blessingService.observeTodayCounts()
        }
        val wishesFlow = if (isHistoryMode) {
            blessingService.observeWishes(targetDate)
        } else {
            blessingService.observeWishes()
        }
        val customizationsFlow = if (isHistoryMode) {
            blessingService.observeEffectiveTabCustomizations(targetDate)
        } else {
            blessingService.observeTodayEffectiveTabCustomizations()
        }
        componentScope.launch {
            countsFlow.collect { counts ->
                _uiState.update { it.copy(todayCounts = counts) }
            }
        }
        componentScope.launch {
            wishesFlow.collect { wishes ->
                _uiState.update { it.copy(wishes = wishes) }
            }
        }
        componentScope.launch {
            customizationsFlow.collect { customizations ->
                _uiState.update { it.copy(tabCustomizations = customizations) }
            }
        }
        componentScope.launch {
            RemoteConfigStorage.rulesChanged.collect {
                val remoteTabTexts = RemoteConfigStorage.getRemoteConfig()
                    .blessingWallTabTexts
                    .orEmpty()
                    .mapNotNull { tabText ->
                        BlessingType.fromKey(tabText.type.orEmpty())?.let { it to tabText }
                    }
                    .toMap()
                _uiState.update { it.copy(remoteTabTexts = remoteTabTexts) }
            }
        }
    }

    fun onPageChanged(page: Int) {
        _uiState.update { it.copy(currentPage = page) }
    }

    val soundEnabled: StateFlow<Boolean> = soundService.soundEnabled

    fun toggleSoundEnabled() {
        soundService.setSoundEnabled(!soundEnabled.value)
    }

    /** 截图分享：将页面截图 Bitmap 通过公共 ImageShareProvider 分享 */
    fun shareBitmap(bitmap: Bitmap, onComplete: () -> Unit = {}) {
        componentScope.launch {
            shareProvider.shareImage(
                imageInfo = ImageInfo(
                    width = bitmap.width,
                    height = bitmap.height,
                    imageFormat = ImageFormat.Png.Lossless,
                ),
                image = bitmap,
                onComplete = onComplete,
            )
        }
    }

    fun onBless(type: BlessingType, title: String) {
        if (isHistoryMode) return
        if (type == BlessingType.WOODEN_FISH) {
            soundService.playWoodenFish()
        } else {
            componentScope.launch { soundService.playBlessingAudio(type, title) }
        }
        componentScope.launch(ioDispatcher) {
            blessingService.bless(type)
        }
    }

    fun saveWish(type: BlessingType, content: String) {
        if (isHistoryMode) return
        componentScope.launch(ioDispatcher) {
            blessingService.saveWish(type = type, content = content)
        }
    }

    fun saveTabCustomization(type: BlessingType, title: String, subtitle: String) {
        if (isHistoryMode) return
        componentScope.launch(ioDispatcher) {
            blessingService.saveTabCustomization(
                type = type,
                title = title,
                subtitle = subtitle,
            )
        }
    }

    fun navigateToRecord() {
        onNavigate(Screen.BlessingWallRecord)
    }

    @AssistedFactory
    interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            @Assisted("date") date: String?,
            @Assisted("initialType") initialType: String?,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
        ): BlessingWallComponent
    }
}

private fun parseDateOrToday(date: String?, blessingService: BlessingService): String {
    val today = blessingService.todayString()
    if (date.isNullOrBlank()) return today
    return runCatching {
        LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        date
    }.getOrDefault(today)
}
