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

package com.wanbaohe.dynamicui.di

import com.wanbaohe.dynamicui.DynamicUiEnv
import com.wanbaohe.dynamicui.action.ActionEngine
import com.wanbaohe.dynamicui.modifier.ModifierPipeline
import com.wanbaohe.dynamicui.parser.DualFormatParser
import com.wanbaohe.dynamicui.renderer.ComponentRegistry
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DynamicUiInitializer – bootstraps [DynamicUiEnv] from Hilt-managed singletons.
 *
 * Call [initialize] once from your Application class (or Hilt EntryPoint):
 *
 * ```kotlin
 * @AndroidEntryPoint
 * class MyApp : Application() {
 *     @Inject lateinit var dynamicUiInit: DynamicUiInitializer
 *
 *     override fun onCreate() {
 *         super.onCreate()
 *         dynamicUiInit.initialize()
 *     }
 * }
 * ```
 *
 * Or without @AndroidEntryPoint using the companion factory:
 * ```kotlin
 * DynamicUiInitializer.from(applicationContext).initialize()
 * ```
 */
@Singleton
class DynamicUiInitializer @Inject constructor(
    private val components: ComponentRegistry,
    private val modifierPipeline: ModifierPipeline,
    private val actionEngine: ActionEngine,
    private val parser: DualFormatParser,
) {
    fun initialize() {
        DynamicUiEnv.initWithHilt(
            components = components,
            modifierPipeline = modifierPipeline,
            actionEngine = actionEngine,
            parser = parser,
        )
    }

    companion object {
        /** Non-inject alternative using Hilt EntryPoint API. */
        fun from(appContext: android.content.Context): DynamicUiInitializer {
            return EntryPoints.get(appContext, DynamicUiEntryPoint::class.java)
                .dynamicUiInitializer()
        }
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface DynamicUiEntryPoint {
    fun dynamicUiInitializer(): DynamicUiInitializer
}

