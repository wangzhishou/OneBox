/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * This file was newly written for OneBox and is not derived
 * from ImageToolbox source. The com.t8rin.* package name is retained
 * only for compatibility with the existing upstream module layout.
 *
 * Origin: OneBox original work (upstream package name retained)
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

package com.t8rin.imagetoolbox.feature.root.presentation.components

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.model.ScreenParams
import com.shifenmiao.model.blog.BlogDetailState
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.feature.root.presentation.screenLogic.RootComponent
import com.shifenmiao.common.components.blog.BlogHeader
import com.wanbaohe.blog.ui.BlogContent


@Composable
fun BlogModalBottomSheet(
    appComponent: AppComponent,
    rootComponent: RootComponent,
    blogId: Int
) {
    val feedbackComponent = remember(blogId) {
        rootComponent.childProvider.homeFactories.blogComponentFactory(
            componentContext = rootComponent.componentContext,
            screenParams = ScreenParams(
                id = blogId
            )
        )
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val blogDetailState by feedbackComponent.blogDetailState.collectAsState()
    val currentTitle = remember(blogDetailState) {
        mutableStateOf(
            (blogDetailState as? BlogDetailState.Success)?.blog?.title ?: ""
        )
    }

    ModalBottomSheet(
        modifier = Modifier
            .statusBarsPadding()
            .navigationBarsPadding(),
        onDismissRequest = { appComponent.hideBlogModalBottomSheet() },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        BlogHeader(
            modifier = Modifier.padding(horizontal = AppTheme.dimens.spaceNormal),
            title = currentTitle.value, isFixed = false
        )
        BlogContent(
            blogComponent = feedbackComponent,
            appComponent = appComponent,
            onGoBack = {
                appComponent.hideBlogModalBottomSheet()
            },
        )
    }
}