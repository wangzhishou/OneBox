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

package com.wanbaohe.textcard.presentation.editor.panels

import com.wanbaohe.textcard.R
import com.wanbaohe.textcard.domain.model.TextCardRole

fun TextCardRole.labelRes(): Int = when (this) {
    TextCardRole.Title -> R.string.textcard_role_title
    TextCardRole.Body -> R.string.textcard_role_body
    TextCardRole.Author -> R.string.textcard_role_author
    TextCardRole.Source -> R.string.textcard_role_source
}
