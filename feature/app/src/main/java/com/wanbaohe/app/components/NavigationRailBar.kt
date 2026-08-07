package com.wanbaohe.app.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSurface
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDesignSystem
import com.t8rin.imagetoolbox.feature.root.presentation.screenLogic.RootComponent


@Composable
fun NavigationRailBar(
    rootComponent: RootComponent,
    screenList: List<Screen>,
    currentTabPageIndex: Int,
    modifier: Modifier,
    footer: @Composable (() -> Unit)? = null,
) {
    AnimatedVisibility(
        visible = true,
        enter = slideInHorizontally(initialOffsetX = { -it }),
        exit = slideOutHorizontally(targetOffsetX = { -it })
    ) {
        GlassSurface(
            modifier = modifier.navigationBarsPadding().statusBarsPadding()
                .fillMaxHeight(),
            style = OneBoxDesignSystem.drawerGlassStyle,
            shape = OneBoxDesignSystem.sectionCardShape,
            color = MaterialTheme.colorScheme.surface,
            borderWidth = 0.dp,
        ) {
            NavigationRail(
                modifier = Modifier.padding(vertical = OneBoxDesignSystem.compactSpacing),
                containerColor = androidx.compose.ui.graphics.Color.Transparent
            ) {
                Spacer(modifier = Modifier.height(OneBoxDesignSystem.compactSpacing))
                screenList.forEachIndexed { index, screen ->
                    val isSelected = index == currentTabPageIndex
                    NavigationRailItem(
                        selected = isSelected,
                        onClick = {
                            rootComponent.navigateTo(screen)
                        },
                        icon = {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.microSpacing),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                val imageVector: ImageVector? =
                                    if (isSelected) screen.twoToneIcon else screen.icon
                                if (imageVector != null) {
                                    Icon(
                                        imageVector = imageVector,
                                        contentDescription = stringResource(screen.subtitle),
                                        modifier = Modifier.size(24.dp),
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        label = {
                            Text(
                                stringResource(screen.title),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                footer?.invoke()
                Spacer(modifier = Modifier.height(OneBoxDesignSystem.compactSpacing))
            }
        }
    }
}
