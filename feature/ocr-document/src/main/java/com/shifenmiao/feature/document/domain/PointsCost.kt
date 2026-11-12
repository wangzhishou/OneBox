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

package com.shifenmiao.feature.document.domain

import com.shifenmiao.core.constants.Constants.FILE_POINTS_CONSUME_NUM
import kotlin.math.ceil

fun computeRequiredPoints(fileSizeBytes: Long): Int {
    if (fileSizeBytes <= 0) return FILE_POINTS_CONSUME_NUM
    val mbUnits = ceil(fileSizeBytes.toDouble() / (1024.0 * 1024.0))
        .toLong()
        .coerceAtLeast(1L)
    val points = mbUnits * FILE_POINTS_CONSUME_NUM.toLong()
    return points.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
}

