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

package com.wanbaohe.decisionwheel.data

import android.content.Context
import androidx.annotation.StringRes
import com.wanbaohe.decisionwheel.R

/**
 * Provides decision wheel preset titles and option names from Android resources.
 *
 * This class exists to satisfy the requirement that *all user-visible texts* come from
 * `strings.xml`, while keeping resource access out of Compose-only APIs.
 *
 * Important notes:
 * - This provider uses [Context.getString] so it can be called from non-Compose layers.
 * - Returned text is localized using the current [Context] configuration.
 */
class DecisionWheelPresetsProvider(
    private val appContext: Context
) {

    /**
     * Returns a localized string for the given resource id.
     */
    fun getString(@StringRes id: Int): String = appContext.getString(id)

    /**
     * Returns the localized default title for a new wheel.
     */
    fun defaultNewWheelTitle(): String = getString(R.string.new_wheel_name)

    /**
     * Returns the localized default option names for a new wheel.
     */
    fun defaultNewWheelOptionNames(): List<String> = listOf(
        getString(R.string.option_1),
        getString(R.string.option_2)
    )

    /**
     * Returns the localized suffix used when duplicating a wheel.
     */
    fun copyTitleSuffix(): String = getString(R.string.wheel_copy_suffix)

    /**
     * Returns the full list of preset wheel definitions as localized strings.
     */
    /**
     * 预置列表第一项的标题，用于决定"默认选中哪个转盘"。
     *
     * 老用户库里那批预置是按旧顺序建的，光改 [presets] 的顺序对他们无效 ——
     * 这里给一个显式口径，新建和已存在的情况都能落到同一个转盘上。
     */
    fun defaultPresetTitle(): String = getString(R.string.preset_drink_penalty)

    /**
     * 预置转盘。
     *
     * 选品口径:只留"真会拿它做决定"的场景 —— 要么天天要选(吃什么),要么一选就有乐子(喝酒惩罚)。
     * 骰子、幸运数字这类别的模块已经有了,纯粹凑数的转盘不占位置。
     *
     * 列表顺序 = 创建顺序 = 默认选中项:第一个位置给"喝酒惩罚"。这是个情绪型场景,
     * 打开就转、转完就有人起哄,比"吃什么"更能说明这个转盘是干嘛的。
     */
    fun presets(): List<PresetDefinitionLocalized> = listOf(
        // Drink penalty —— 默认转盘
        PresetDefinition(
            titleRes = R.string.preset_drink_penalty,
            optionRes = listOf(
                R.string.penalty_one,
                R.string.penalty_two,
                R.string.penalty_half,
                R.string.penalty_bottoms_up,
                R.string.penalty_substitute,
                R.string.penalty_everyone,
                R.string.penalty_spared,
                R.string.penalty_double
            )
        ),
        // Food
        PresetDefinition(
            titleRes = R.string.preset_food,
            optionRes = listOf(
                R.string.food_hotpot,
                R.string.food_bbq,
                R.string.food_japanese,
                R.string.food_western,
                R.string.food_chinese,
                R.string.food_fastfood,
                R.string.food_snacks,
                R.string.food_dessert
            )
        ),
        // Truth or dare
        PresetDefinition(
            titleRes = R.string.preset_truth_dare,
            optionRes = listOf(
                R.string.td_truth,
                R.string.td_dare,
                R.string.td_truth_hard,
                R.string.td_dare_hard,
                R.string.td_pick_someone,
                R.string.td_you_choose,
                R.string.td_pass,
                R.string.td_free_pass
            )
        ),
        // Blame game
        PresetDefinition(
            titleRes = R.string.preset_blame,
            optionRes = listOf(
                R.string.blame_requirements,
                R.string.blame_design,
                R.string.blame_backend,
                R.string.blame_test_env,
                R.string.blame_boss,
                R.string.blame_network,
                R.string.blame_legacy
            )
        ),
        // Drink
        PresetDefinition(
            titleRes = R.string.preset_drink,
            optionRes = listOf(
                R.string.drink_coffee,
                R.string.drink_milk_tea,
                R.string.drink_juice,
                R.string.drink_water,
                R.string.drink_soda,
                R.string.drink_tea
            )
        ),
        // Who Pays
        PresetDefinition(
            titleRes = R.string.preset_who_pays,
            optionRes = listOf(
                R.string.pay_me,
                R.string.pay_you,
                R.string.pay_aa,
                R.string.pay_boss,
                R.string.pay_next_time,
                R.string.pay_luck
            )
        ),
        // Date Night
        PresetDefinition(
            titleRes = R.string.preset_date_night,
            optionRes = listOf(
                R.string.date_movie,
                R.string.date_dinner,
                R.string.date_walk,
                R.string.date_game,
                R.string.date_cook,
                R.string.date_travel
            )
        ),
        // Household Chores
        PresetDefinition(
            titleRes = R.string.preset_chores,
            optionRes = listOf(
                R.string.chore_dishes,
                R.string.chore_floor,
                R.string.chore_laundry,
                R.string.chore_trash,
                R.string.chore_cooking,
                R.string.chore_groceries
            )
        )
    ).map { it.localize(appContext) }

    /**
     * Preset definition holding resource ids.
     */
    data class PresetDefinition(
        @StringRes val titleRes: Int,
        val optionRes: List<Int>
    ) {
        /**
         * Converts [titleRes]/[optionRes] into localized strings.
         */
        fun localize(context: Context): PresetDefinitionLocalized {
            return PresetDefinitionLocalized(
                title = context.getString(titleRes),
                options = optionRes.map(context::getString)
            )
        }
    }

    /**
     * Localized preset definition.
     */
    data class PresetDefinitionLocalized(
        val title: String,
        val options: List<String>
    )
}
