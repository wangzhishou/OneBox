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

package com.wanbaohe.textcard.di

import com.wanbaohe.textcard.data.canvas.PrefsCustomCanvasStore
import com.wanbaohe.textcard.data.export.AndroidTextCardExportService
import com.wanbaohe.textcard.data.paper.RemotePaperRepository
import com.wanbaohe.textcard.data.project.AndroidTextCardProjectStore
import com.wanbaohe.textcard.data.render.AndroidTextCardExportRenderer
import com.wanbaohe.textcard.data.render.AndroidTextCardLayoutEngine
import com.wanbaohe.textcard.data.template.PrefsCustomTextCardTemplateStore
import com.wanbaohe.textcard.data.textimport.AndroidTextCardImportService
import com.wanbaohe.textcard.domain.CustomCanvasStore
import com.wanbaohe.textcard.domain.CustomTextCardTemplateStore
import com.wanbaohe.textcard.domain.TextCardImportService
import com.wanbaohe.textcard.domain.TextCardExportRenderer
import com.wanbaohe.textcard.domain.TextCardExportService
import com.wanbaohe.textcard.domain.TextCardPaperRepository
import com.wanbaohe.textcard.domain.TextCardProjectStore
import com.wanbaohe.textcard.domain.render.TextCardLayoutEngine
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal interface TextCardModule {

    @Binds
    fun exportRenderer(
        impl: AndroidTextCardExportRenderer
    ): TextCardExportRenderer

    @Binds
    fun paperRepository(
        impl: RemotePaperRepository
    ): TextCardPaperRepository

    @Binds
    fun customCanvasStore(
        impl: PrefsCustomCanvasStore
    ): CustomCanvasStore

    @Binds
    fun customTemplateStore(
        impl: PrefsCustomTextCardTemplateStore
    ): CustomTextCardTemplateStore

    @Binds
    fun importService(
        impl: AndroidTextCardImportService
    ): TextCardImportService

    @Binds
    fun projectStore(
        impl: AndroidTextCardProjectStore
    ): TextCardProjectStore

    @Binds
    fun layoutEngine(
        impl: AndroidTextCardLayoutEngine
    ): TextCardLayoutEngine

    @Binds
    fun exportService(
        impl: AndroidTextCardExportService
    ): TextCardExportService
}
