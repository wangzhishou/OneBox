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

package com.shifenmiao.ai.image.screen

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.core.graphics.applyCanvas
import com.shifenmiao.ai.image.components.model.BackgroundBehavior
import com.shifenmiao.ai.image.controllers.AIImageComponent
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.core.R
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.core.ui.utils.helper.Clipboard
import com.wanbaohe.core.ui.utils.helper.isPortraitOrientationAsState
import com.wanbaohe.core.ui.widget.buttons.ShareButton
import com.wanbaohe.core.ui.widget.sheets.ProcessImagesPreferenceSheet

@Composable
fun AIGCImageScreen(
    appComponent: AppComponent,
    aiImageComponent: AIImageComponent
) {
    val showConfetti: () -> Unit = AppToastHost::showConfetti

    BaseScreen(
        title = stringResource(id = R.string.ai_aigc_image_title),
        onGoBack = appComponent.onGoBack,
        actions = {
            var editSheetData by remember {
                mutableStateOf(listOf<Uri>())
            }
            ShareButton(
                enabled = aiImageComponent.backgroundBehavior !is BackgroundBehavior.None,
                onShare = {
                    aiImageComponent.shareBitmap(showConfetti)
                },
                onCopy = {
                    aiImageComponent.cacheCurrentImage(Clipboard::copy)
                },
                onEdit = {
                    aiImageComponent.cacheCurrentImage { uri ->
                        editSheetData = listOf(uri)
                    }
                }
            )
            ProcessImagesPreferenceSheet(
                uris = editSheetData,
                visible = editSheetData.isNotEmpty(),
                onDismiss = {
                    editSheetData = emptyList()
                },
                onNavigate = appComponent.onNavigate
            )
        }
    ) {
        AIGCImageContent(
            appComponent,
            aiImageComponent
        )
    }
}

@Composable
fun AIGCImageContent(
    appComponent: AppComponent,
    aiImageComponent: AIImageComponent
) {
    val configuration = LocalConfiguration.current
    val isPortrait by isPortraitOrientationAsState()

    val bitmap = aiImageComponent.bitmap ?: (aiImageComponent.backgroundBehavior as? BackgroundBehavior.Color)?.run {
            remember {
                ImageBitmap(width, height).asAndroidBitmap()
                    .applyCanvas { drawColor(color) }
            }
        } ?: remember {
            ImageBitmap(
                configuration.screenWidthDp,
                configuration.screenHeightDp
            ).asAndroidBitmap()
        }

}
