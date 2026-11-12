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
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.ui.graphics.vector.ImageVector
import com.wanbaohe.recordcenter.R
import com.wanbaohe.recordcenter.registry.RecordField
import com.wanbaohe.recordcenter.registry.RecordTypeDefinition

/** 血脂记录(四项,低密度/高密度脂蛋白为选填) */
object BloodLipidRecordType : RecordTypeDefinition {
    override val key: String = "blood_lipid"
    override val titleRes: Int = R.string.record_center_type_blood_lipid_title
    override val descriptionRes: Int = R.string.record_center_type_blood_lipid_desc
    override val icon: ImageVector = Icons.Filled.Biotech
    override val fields: List<RecordField> = listOf(
        RecordField(
            key = "total_cholesterol",
            labelRes = R.string.record_center_field_lipid_total_cholesterol,
            unit = "mmol/L",
            min = 0.1f,
            max = 30.0f,
        ),
        RecordField(
            key = "triglyceride",
            labelRes = R.string.record_center_field_lipid_triglyceride,
            unit = "mmol/L",
            min = 0.1f,
            max = 30.0f,
        ),
        RecordField(
            key = "ldl",
            labelRes = R.string.record_center_field_lipid_ldl,
            unit = "mmol/L",
            required = false,
            min = 0.1f,
            max = 30.0f,
        ),
        RecordField(
            key = "hdl",
            labelRes = R.string.record_center_field_lipid_hdl,
            unit = "mmol/L",
            required = false,
            min = 0.1f,
            max = 30.0f,
        ),
    )
    override val referenceRangeRes: Int = R.string.record_center_ref_blood_lipid
    override val chartFieldKeys: List<String> =
        listOf("total_cholesterol", "triglyceride", "ldl", "hdl")
    override val sortOrder: Int = 80
}
