/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2024 T8RIN (Malik Mukhametzyanov)
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

package com.t8rin.imagetoolbox.core.domain.utils

import java.text.CharacterIterator
import java.text.StringCharacterIterator
import java.util.Locale


fun humanFileSize(bytes: Long): String {
    var tempBytes = bytes
    if (-1024 < tempBytes && tempBytes < 1024) {
        return ltrIsolated("$tempBytes B")
    }
    val ci: CharacterIterator = StringCharacterIterator("kMGTPE")
    while (tempBytes <= -999950 || tempBytes >= 999950) {
        tempBytes /= 1024
        ci.next()
    }
    val formatted = java.lang.String.format(
        Locale.getDefault(),
        "%.1f %cB",
        tempBytes / 1024.0,
        ci.current()
    ).replace(",0", "")
    return ltrIsolated(formatted)
}

/** 数字和单位绑在一起,避免从右往左排时 MB 跑到数字前面。 */
private fun ltrIsolated(text: String): String = "\u2066$text\u2069"