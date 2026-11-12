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

package com.wanbaohe.unitconverter.domain

import androidx.annotation.StringRes
import com.wanbaohe.unitconverter.R

enum class KinshipStep(
    val route: String,
    @StringRes val labelRes: Int,
) {
    Father("father", R.string.unit_kin_step_father),
    Mother("mother", R.string.unit_kin_step_mother),
    Spouse("spouse", R.string.unit_kin_step_spouse),
    Brother("brother", R.string.unit_kin_step_brother),
    Sister("sister", R.string.unit_kin_step_sister),
    Son("son", R.string.unit_kin_step_son),
    Daughter("daughter", R.string.unit_kin_step_daughter);

    companion object {
        fun fromRoute(route: String): KinshipStep? {
            return values().firstOrNull { it.route == route }
        }
    }
}
