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

package com.shifenmiao.base.provider

import androidx.compose.runtime.compositionLocalOf
import com.shifenmiao.database.data_draft.DataDraftHelper

/**
 * Composition local for DataDraftHelper.
 * Used by composable call-sites that need to create drafts before navigating.
 *
 * Placed in core:base because it depends on both core:ui (Compose) and core:database (DataDraftHelper).
 * core:ui cannot directly depend on core:database due to circular dependency constraints.
 */
val LocalDataDraftHelper = compositionLocalOf<DataDraftHelper> {
    error("No DataDraftHelper provided")
}

