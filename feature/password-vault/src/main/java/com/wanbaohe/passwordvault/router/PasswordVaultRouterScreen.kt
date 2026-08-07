package com.wanbaohe.passwordvault.router

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.wanbaohe.passwordvault.detail.PasswordVaultDetailScreen
import com.wanbaohe.passwordvault.edit.PasswordVaultEditScreen
import com.wanbaohe.passwordvault.list.PasswordVaultListScreen
import com.wanbaohe.passwordvault.router.screenLogic.PasswordVaultRouterComponent

@Composable
fun PasswordVaultRouterScreen(
    component: PasswordVaultRouterComponent,
) {
    val currentType by component.currentType.collectAsState()

    AnimatedContent(
        targetState = currentType,
        transitionSpec = {
            val direction = if (targetState isForwardFrom(initialState)) 1 else -1
            (fadeIn(animationSpec = tween(250)) +
                slideInHorizontally(animationSpec = tween(300)) { it / 5 * direction })
                .togetherWith(
                    fadeOut(animationSpec = tween(200)) +
                        slideOutHorizontally(animationSpec = tween(300)) { -it / 5 * direction }
                )
        },
        label = "password_vault_router"
    ) { type ->
        when (type) {
            is Screen.PasswordVault.Type.List -> PasswordVaultListScreen(
                component = component.listComponent
            )

            is Screen.PasswordVault.Type.Detail -> PasswordVaultDetailScreen(
                component = component.detailComponent(type.entryId)
            )

            is Screen.PasswordVault.Type.Add -> PasswordVaultEditScreen(
                component = component.editComponent(entryId = null)
            )

            is Screen.PasswordVault.Type.Edit -> PasswordVaultEditScreen(
                component = component.editComponent(type.entryId)
            )
        }
    }
}

private infix fun Screen.PasswordVault.Type.isForwardFrom(other: Screen.PasswordVault.Type): Boolean {
    return ordinal(this) > ordinal(other)
}

private fun ordinal(type: Screen.PasswordVault.Type): Int = when (type) {
    is Screen.PasswordVault.Type.List -> 0
    is Screen.PasswordVault.Type.Detail -> 1
    is Screen.PasswordVault.Type.Add -> 2
    is Screen.PasswordVault.Type.Edit -> 3
}
