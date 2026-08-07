package com.wanbaohe.app.screen

import androidx.compose.runtime.Composable
import com.shifenmiao.common.logic.AppComponent
import com.wanbaohe.app.navigation.AdaptiveNavigationLayout
import com.t8rin.imagetoolbox.feature.root.presentation.components.utils.ResetThemeOnGoBack
import com.t8rin.imagetoolbox.feature.root.presentation.screenLogic.RootComponent

@Composable
fun ScreenSelector(
    rootComponent: RootComponent,
    appComponent: AppComponent,
) {
    ResetThemeOnGoBack(rootComponent)
    AdaptiveNavigationLayout(rootComponent, appComponent)
}
