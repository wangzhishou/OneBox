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

package com.shifenmiao.model

import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.core.R


enum class ListItemType(val id: Int, val title: String, val description: String) {
    NORMAL(
        1,
        AppContext.getString(R.string.type_default),
        "Default Description"
    ),
    HTML(
        2,
        AppContext.getString(R.string.type_web),
        "Web Description"
    ),
    WECHAT(
        5,
        AppContext.getString(R.string.type_wechat_miniprogram),
        "WeChat Description"
    ),
    VIDEO(
        3,
        AppContext.getString(R.string.type_wechat_video),
        "Video Description"
    ),
    PROMPT(
        4,
        AppContext.getString(R.string.type_prompt),
        "Prompt Description"
    ),
    AGENT(
        6,
        AppContext.getString(R.string.type_agent),
        "Agent Description"
    ),
    BLOG(
        7,
        AppContext.getString(R.string.type_blog),
        "Blog Description"
    ),
    NOTE(
        8,
        AppContext.getString(R.string.item_note_title),
        "Note Description"
    );
    companion object {
        fun fromId(id: Int?): ListItemType? {
            return entries.find { it.id == id }
        }
    }
}