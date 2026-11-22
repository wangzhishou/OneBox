/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.wanbaohe.app.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wanbaohe.core.ui.utils.navigation.Screen
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.core.ui.widget.glass.GlassSurface
import com.wanbaohe.core.ui.widget.glass.glassSelectionBackground
import com.wanbaohe.core.ui.widget.system.OneBoxDesignSystem
import com.t8rin.imagetoolbox.feature.root.presentation.screenLogic.RootComponent
import com.wanbaohe.app.update.rememberUpdateAvailable


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
            modifier = modifier
                .displayCutoutPadding()
                .navigationBarsPadding()
                .statusBarsPadding()
                .fillMaxHeight(),
            style = OneBoxDesignSystem.drawerGlassStyle,
            shape = OneBoxDesignSystem.sectionCardShape,
            color = MaterialTheme.colorScheme.surface,
            borderWidth = 0.dp,
        ) {
            NavigationRail(
                // 固定宽度: 自绘 item 的 fillMaxWidth 在没有宽度约束时会把 rail 撑满全屏。
                // 104dp 才能放下「Agregar / Configuración」这类标签，80dp 会截成 Agre / Con。
                modifier = Modifier
                    .width(104.dp)
                    .padding(vertical = OneBoxDesignSystem.compactSpacing),
                containerColor = Color.Transparent
            ) {
                Spacer(modifier = Modifier.height(OneBoxDesignSystem.compactSpacing))
                val hasUpdateAvailable = rememberUpdateAvailable()
                screenList.forEachIndexed { index, screen ->
                    val isSelected = index == currentTabPageIndex
                    RailNavigationItem(
                        selected = isSelected,
                        icon = if (isSelected) screen.twoToneIcon else screen.icon,
                        label = stringResource(screen.title),
                        contentDescription = stringResource(screen.subtitle),
                        showBadge = hasUpdateAvailable && screen is Screen.Profile,
                        onClick = { rootComponent.navigateTo(screen) },
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                footer?.invoke()
                Spacer(modifier = Modifier.height(OneBoxDesignSystem.compactSpacing))
            }
        }
    }
}

/**
 * 与首页 Tab 和竖屏底栏共用统一选择态背景。
 */
@Composable
private fun RailNavigationItem(
    selected: Boolean,
    icon: ImageVector?,
    label: String,
    contentDescription: String,
    showBadge: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(12.dp)
    val selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp)
            .clip(shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .semantics {
                role = Role.Tab
                this.selected = selected
            },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .clip(shape)
                .indication(interactionSource, ripple(bounded = true))
                .then(
                    if (selected) {
                        Modifier.glassSelectionBackground(
                            selected = true,
                            style = GlassStyle.Regular,
                            shape = shape,
                            color = selectedContainerColor,
                        )
                    } else {
                        Modifier
                    }
                )
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            if (icon != null) {
                BadgedBox(
                    badge = {
                        if (showBadge) {
                            Badge(containerColor = MaterialTheme.colorScheme.errorContainer)
                        }
                    }
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = contentDescription,
                        modifier = Modifier.size(22.dp),
                        tint = contentColor,
                    )
                }
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    hyphens = Hyphens.Auto,
                ),
                color = contentColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
    }
}
