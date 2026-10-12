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
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.ui.graphics.vector.ImageVector
import com.t8rin.imagetoolbox.core.resources.Icons

/**
 * 「更多」图标：保留自绘入口，内容改用系统图标。
 *
 * 全项目取这个图标的地方（浏览器底栏、WebView 顶栏、物品分类图标，
 * 以及图标注册表里 "MoreVert" 这个 key）都写的是 `Icons.Outlined.LineMore`，
 * 所以这里名字与签名保持不变、只把里面的矢量内容换成 Material 的
 * `Icons.Outlined.MoreVert`（与象棋/五子棋/待办页面上的实心三点完全一致）。
 *
 * 这么做是为了收口：以后要摘掉 `material-icons-extended` 依赖时，
 * **只改这一个文件**即可，所有调用点零改动。
 *
 * 注意：这里必须保持一次静态直接引用（`MaterialIcons.Outlined.MoreVert`），
 * 不要换成 `IconRegistry.resolve("MoreVert")` 之类的按 key 查找 ——
 * R8 靠直接引用判定可达性，隔着注册表找会把这个图标类裁掉。
 */
val Icons.Outlined.LineMore: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
    MaterialIcons.Outlined.MoreVert
}
