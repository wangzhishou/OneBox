package com.wanbaohe.app.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.material.icons.Icons
import androidx.compose.material3.DrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.shifenmiao.core.R
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.feature.root.presentation.screenLogic.RootComponent
import kotlin.time.Duration.Companion.milliseconds
import com.t8rin.imagetoolbox.core.resources.icons.line.LineExitToApp

@Composable
fun MainBackHandler(
    rootComponent: RootComponent,
    drawerState: DrawerState,
    currentScreen: Screen
) {
    var backPressedOnce by rememberSaveable { mutableStateOf(false) }
    val activity = LocalActivity.current
    val coroutineScope = rememberCoroutineScope()

    BackHandler {
        if (drawerState.isOpen) {
            coroutineScope.launch {
                drawerState.close()
            }
            return@BackHandler
        }
        if (rootComponent.startEntry() == currentScreen) {
            if (backPressedOnce) {
                activity?.finishAffinity()
            } else {
                backPressedOnce = true
                AppToastHost.showToast(
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineExitToApp,
                    message = activity?.getString(R.string.back_prressed_once) ?: "",
                )
                coroutineScope.launch {
                    delay(2000.milliseconds) // Reset the flag after 2 seconds
                    backPressedOnce = false
                }
            }
        } else {
            rootComponent.navigateBack()
        }
    }
}