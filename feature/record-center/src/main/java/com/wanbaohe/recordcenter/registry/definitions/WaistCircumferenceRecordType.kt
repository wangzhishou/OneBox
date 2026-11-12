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
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.ui.graphics.vector.ImageVector
import com.wanbaohe.recordcenter.R
import com.wanbaohe.recordcenter.registry.RecordField
import com.wanbaohe.recordcenter.registry.RecordTypeDefinition

/** 腰围记录 */
object WaistCircumferenceRecordType : RecordTypeDefinition {
    override val key: String = "waist_circumference"
    override val titleRes: Int = R.string.record_center_type_waist_circumference_title
    override val descriptionRes: Int = R.string.record_center_type_waist_circumference_desc
    override val icon: ImageVector = Icons.Filled.AccessibilityNew
    override val fields: List<RecordField> = listOf(
        RecordField(
            key = "value",
            labelRes = R.string.record_center_field_waist_circumference_value,
            unit = "cm",
            min = 30f,
            max = 300f,
        ),
    )
    override val referenceRangeRes: Int = R.string.record_center_ref_waist_circumference
    override val chartFieldKeys: List<String> = listOf("value")
    override val sortOrder: Int = 120
}
