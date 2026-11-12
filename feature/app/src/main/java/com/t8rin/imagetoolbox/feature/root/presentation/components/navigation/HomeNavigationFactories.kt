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

package com.t8rin.imagetoolbox.feature.root.presentation.components.navigation

import javax.inject.Inject

import com.shifenmiao.ai.component.AIChatComponent
import com.shifenmiao.ai.component.AgentComponent
import com.shifenmiao.ai.component.AgentDetailComponent
import com.shifenmiao.common.components.category.ReorderableComponent
import com.shifenmiao.marquee.screenLogic.MarqueeComponent
import com.shifenmiao.online.component.CreateHtmlComponent
import com.shifenmiao.online.component.CreateNoteComponent
import com.shifenmiao.online.component.EditPromptComponent
import com.shifenmiao.online.component.ItemListComponent
import com.shifenmiao.online.component.NoteItemComponent
import com.shifenmiao.online.component.PlaygroundComponent
import com.shifenmiao.search.logic.SearchComponent
import com.shifenmiao.webview.WebViewComponent
import com.t8rin.imagetoolbox.feature.settings.presentation.screenLogic.SettingsComponent
import com.wanbaohe.app.component.ActivityLogComponent
import com.wanbaohe.app.component.FavoriteComponent
import com.wanbaohe.blog.logic.BlogComponent
import com.wanbaohe.blog.logic.CreateFeedbackComponent
import com.wanbaohe.decisionwheel.component.DecisionWheelRouterComponent
import com.wanbaohe.file.browser.screenLogic.FileBrowserComponent
import com.wanbaohe.file_transfer.screenLogic.FileTransferComponent
import com.wanbaohe.profile.viewmodel.PayComponent

/**
 * 启动期按需解析的工厂集合——Home 组。
 *
 * 由 [ChildProvider] 通过 `Provider<HomeNavigationFactories>` 持有，
 * 仅当用户首次进入本组对应 screen 时才触发 Hilt 解析本类字段，
 * 避免冷启时一次性解析全部 110 个 binding 节点。
 */
class HomeNavigationFactories @Inject constructor(
    val itemListComponentFactory: ItemListComponent.Factory,
    val playgroundComponentFactory: PlaygroundComponent.Factory,
    val activityLogComponentFactory: ActivityLogComponent.Factory,
    val favoriteComponentFactory: FavoriteComponent.Factory,
    val settingsComponentFactory: SettingsComponent.Factory,
    val searchComponentFactory: SearchComponent.Factory,
    val createNoteComponentFactory: CreateNoteComponent.Factory,
    val createHtmlComponentFactory: CreateHtmlComponent.Factory,
    val editPromptComponentFactory: EditPromptComponent.Factory,
    val noteItemComponentFactory: NoteItemComponent.Factory,
    val decisionWheelRouterComponentFactory: DecisionWheelRouterComponent.Factory,
    val fileTransferComponentFactory: FileTransferComponent.Factory,
    val reorderableComponentFactory: ReorderableComponent.Factory,
    val fileBrowserComponentFactory: FileBrowserComponent.Factory,
    val blogComponentFactory: BlogComponent.Factory,
    val feedbackComponentFactory: CreateFeedbackComponent.Factory,
    val payComponentFactory: PayComponent.Factory,
    val marqueeComponentFactory: MarqueeComponent.Factory,
    val agentDetailComponentFactory: AgentDetailComponent.Factory,
    val aiChatComponentFactory: AIChatComponent.Factory,
    val webViewComponentFactory: WebViewComponent.Factory,
    val agentComponentFactory: AgentComponent.Factory,
)
