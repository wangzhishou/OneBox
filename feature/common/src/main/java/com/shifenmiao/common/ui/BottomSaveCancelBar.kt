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

package com.shifenmiao.common.ui

import androidx.compose.foundation.layout.RowScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.shifenmiao.core.R
import com.wanbaohe.core.ui.widget.system.OneBoxBottomActionBar

@Composable
fun BottomSaveCancelBar(
    onCancel: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
    saveText: String? = null,
    cancelEnabled: Boolean = true,
    saveEnabled: Boolean = true,
    extraActions: (@Composable RowScope.() -> Unit)? = null,
    dangerText: String? = null,
    onDangerClick: (() -> Unit)? = null,
    dangerEnabled: Boolean = true,
    primaryIcon: ImageVector? = null,
    secondaryIcon: ImageVector? = null,
    /** BaseScreen 里使用时传 false，避免和内容区的导航栏内边距叠加 */
    applyNavigationBarsPadding: Boolean = true,
) {
    val resolvedSaveText = saveText ?: stringResource(R.string.settings_confirm)

    OneBoxBottomActionBar(
        modifier = modifier,
        primaryText = resolvedSaveText,
        onPrimaryClick = onSave,
        primaryEnabled = saveEnabled,
        secondaryText = stringResource(R.string.button_cancel),
        onSecondaryClick = onCancel,
        secondaryEnabled = cancelEnabled,
        extraActions = extraActions,
        dangerText = dangerText,
        onDangerClick = onDangerClick,
        dangerEnabled = dangerEnabled,
        primaryIcon = primaryIcon,
        secondaryIcon = secondaryIcon,
        applyNavigationBarsPadding = applyNavigationBarsPadding,
    )
}
