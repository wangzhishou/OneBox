package com.wanbaohe.app.provider


import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import com.shifenmiao.base.utils.RateLimiter
import com.shifenmiao.common.handle.LocalUrlNavigator
import com.shifenmiao.common.handle.UrlNavigator
import com.shifenmiao.model.login.LoginState
import com.shifenmiao.model.snackbar.LocalSnackBarHost
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.settings.domain.SimpleSettingsInteractor
import com.t8rin.imagetoolbox.core.settings.presentation.model.UiSettingsState
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalEditPresetsController
import com.t8rin.imagetoolbox.core.ui.utils.provider.LocalLoginState
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsManager
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSimpleSettingsInteractor
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalThemeRepository
import com.t8rin.imagetoolbox.core.settings.presentation.provider.rememberEditPresetsController
import com.t8rin.imagetoolbox.core.ui.theme.ImageToolboxThemeSurface
import com.t8rin.imagetoolbox.core.ui.utils.helper.LocalFilterPreviewModelProvider
import com.t8rin.imagetoolbox.core.ui.utils.helper.rememberFilterPreviewProvider
import com.shifenmiao.base.provider.LocalDataDraftHelper
import com.shifenmiao.base.ui.picker.LocalCityPickerDataHolder
import com.t8rin.imagetoolbox.core.ui.utils.navigation.LocalOnNavigate
import com.t8rin.imagetoolbox.core.ui.utils.provider.LocalImageShareProvider
import com.t8rin.imagetoolbox.core.ui.utils.provider.LocalScreenSize
import com.t8rin.imagetoolbox.core.ui.utils.provider.rememberScreenSize
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.rememberEnhancedHapticFeedback
import com.t8rin.imagetoolbox.core.ui.widget.other.ToastHost
import com.t8rin.imagetoolbox.feature.root.presentation.screenLogic.RootComponent
import com.t8rin.imagetoolbox.core.ui.utils.confetti.ConfettiHost
import com.t8rin.imagetoolbox.core.ui.utils.blessing.BlessingEffectHost
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost

@Composable
fun ImageToolboxCompositionLocals(
    settingsState: UiSettingsState,
    simpleSettingsInteractor: SimpleSettingsInteractor? = null,
    component: RootComponent,
    content: @Composable () -> Unit
) {
    val editPresetsController = rememberEditPresetsController()
    val snackBarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val customHapticFeedback = rememberEnhancedHapticFeedback(settingsState.hapticsStrength)
    val screenSize = rememberScreenSize()

    val previewProvider = rememberFilterPreviewProvider(
        preview = component.filterPreviewModel,
        canSetDynamicFilterPreview = component.canSetDynamicFilterPreview
    )

    // 创建统一的URL导航器
    val urlNavigator = remember(context, component) {
        UrlNavigator(
            context = context,
            onNavigate = { destination ->
                if (!RateLimiter.isFastClick()) {
                    when (destination) {
                        else -> component.navigateTo(destination)
                    }
                }
            },
            contentRouter = component.contentRouter,
        )
    }

    val values = remember(
        settingsState,
        simpleSettingsInteractor,
        editPresetsController,
        customHapticFeedback,
        screenSize,
        previewProvider,
        snackBarHostState,
        urlNavigator
    ) {
        listOfNotNull(
            LocalSettingsState provides settingsState,
            LocalSimpleSettingsInteractor providesOrNull simpleSettingsInteractor,
            LocalSettingsManager provides component.settingsManager,
            LocalThemeRepository provides component.themeRepository,
            LocalUrlNavigator provides urlNavigator,
            LocalOnNavigate provides urlNavigator.onNavigate,
            LocalEditPresetsController provides editPresetsController,
            LocalFilterPreviewModelProvider providesOrNull previewProvider,
            LocalHapticFeedback provides customHapticFeedback,
            LocalScreenSize provides screenSize,
            LocalSnackBarHost provides snackBarHostState,
            LocalDataDraftHelper provides component.dataDraftHelper,
            LocalCityPickerDataHolder provides component.cityPickerDataHolder,
            LocalImageShareProvider provides component.imageShareProvider
        ).toTypedArray()
    }

    CompositionLocalProvider(
        *values,
        content = {
            ImageToolboxThemeSurface {
                AppTheme.colorScheme = MaterialTheme.colorScheme
                CompositionLocalProvider(
                    LocalContentColor provides MaterialTheme.colorScheme.onSurface,
                    LocalTextStyle provides MaterialTheme.typography.bodyLarge,
                ) {
                    content()
                    ToastHost()
                    ConfettiHost()
                    BlessingEffectHost(AppToastHost.blessingEffectState)
                }
            }
        }
    )
}

@Composable
fun LoginStateCompositionLocals(
    loginState: LoginState,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalLoginState provides loginState,
        content = content
    )
}

private infix fun <T : Any> ProvidableCompositionLocal<T>.providesOrNull(
    value: T?
): ProvidedValue<T>? = if (value != null) provides(value) else null

