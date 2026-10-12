/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2024 T8RIN (Malik Mukhametzyanov)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program. If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.t8rin.imagetoolbox.core.resources.icons.line

import androidx.compose.material.icons.Icons as MaterialIcons
import androidx.compose.material.icons.outlined.FilterNone
import androidx.compose.ui.graphics.vector.ImageVector
import com.t8rin.imagetoolbox.core.resources.Icons

/**
 * 「标签页」图标：自绘命名入口，内容用系统图标。
 *
 * 浏览器底栏的标签页按钮、标签页列表的每一行、以及空标签页提示都用它。
 * 换掉之前误用的 `Icons.Rounded.Language`（地球）——那是「语言/网址」的语义，
 * 放在多标签按钮上完全看不出是标签页。
 *
 * 与 `LineMore` 同一套收口约定：名字保持 Line* 前缀、调用点不动，
 * 内容指向 Material 的 `Icons.Outlined.FilterNone`（两个叠放的方框）。
 * 选它而不是单个页签轮廓：叠框在 24dp 下轮廓更清楚，右上角的标签数角标
 * 压上来时也不容易把主体形状遮糊。以后要摘掉 `material-icons-extended`
 * 时**只改这一个文件**即可。
 *
 * 注意：必须保持静态直接引用，不要改成 `IconRegistry.resolve(...)` 之类
 * 按 key 查找的写法 —— R8 靠直接引用判定可达性，隔着注册表会把这个图标裁掉。
 */
val Icons.Outlined.LineTab: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
    MaterialIcons.Outlined.FilterNone
}
