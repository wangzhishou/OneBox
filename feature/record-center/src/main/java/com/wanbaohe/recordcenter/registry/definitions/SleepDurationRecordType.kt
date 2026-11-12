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

package com.wanbaohe.recordcenter.registry.definitions

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.ui.graphics.vector.ImageVector
import com.wanbaohe.recordcenter.R
import com.wanbaohe.recordcenter.registry.RecordField
import com.wanbaohe.recordcenter.registry.RecordTypeDefinition

/** 睡眠记录 */
object SleepDurationRecordType : RecordTypeDefinition {
    override val key: String = "sleep_duration"
    override val titleRes: Int = R.string.record_center_type_sleep_duration_title
    override val descriptionRes: Int = R.string.record_center_type_sleep_duration_desc
    override val icon: ImageVector = Icons.Filled.Bedtime
    override val fields: List<RecordField> = listOf(
        RecordField(
            key = "value",
            labelRes = R.string.record_center_field_sleep_duration_value,
            unit = "h",
            min = 0f,
            max = 24f,
        ),
    )
    override val referenceRangeRes: Int = R.string.record_center_ref_sleep_duration
    override val chartFieldKeys: List<String> = listOf("value")
    override val sortOrder: Int = 110
}
