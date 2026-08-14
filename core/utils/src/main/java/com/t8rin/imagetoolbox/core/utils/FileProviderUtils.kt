/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2026 T8RIN (Malik Mukhametzyanov)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.t8rin.imagetoolbox.core.utils

import android.content.Context

/**
 * FileProvider authority,与 app manifest 中 ${fileProviderAuthority} 占位值保持一致。
 * 由 applicationId 派生(debug 带 .debug 后缀),
 * 不用字符串资源是为了避免资源按配置(如 zh-rCN)覆盖导致与 manifest 注册值不匹配。
 */
val Context.fileProviderAuthority: String
    get() = "$packageName.fileprovider"
