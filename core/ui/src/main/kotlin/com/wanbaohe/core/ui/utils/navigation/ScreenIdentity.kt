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

package com.wanbaohe.core.ui.utils.navigation

import java.util.Locale

internal fun Screen.routeKey(): String {
    val base = simpleName.ifBlank { this::class.simpleName.orEmpty().ifBlank { "screen_$id" } }
    return base
        .trim()
        .replace(Regex("([a-z0-9])([A-Z])"), "$1_$2")
        .replace(Regex("[^A-Za-z0-9]+"), "_")
        .trim('_')
        .lowercase(Locale.ROOT)
        .ifBlank { "screen_$id" }
}

internal fun Screen.canonicalName(): String = "screen.${routeKey()}"

internal fun Screen.slug(): String = canonicalName().replace('.', '-')
