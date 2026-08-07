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
     * Returns the full list of preset wheel definitions as localized strings.
     */
    fun presets(): List<PresetDefinitionLocalized> = listOf(
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
        // Dice
        PresetDefinition(
            titleRes = R.string.preset_dice,
            optionRes = listOf(
                R.string.dice_1,
                R.string.dice_2,
                R.string.dice_3,
                R.string.dice_4,
                R.string.dice_5,
                R.string.dice_6
            )
        ),
        // Activity
        PresetDefinition(
            titleRes = R.string.preset_activity,
            optionRes = listOf(
                R.string.activity_movie,
                R.string.activity_park,
                R.string.activity_shopping,
                R.string.activity_game,
                R.string.activity_sleep,
                R.string.activity_read
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
        // Work task
        PresetDefinition(
            titleRes = R.string.preset_work_task,
            optionRes = listOf(
                R.string.task_coding,
                R.string.task_meeting,
                R.string.task_review,
                R.string.task_document,
                R.string.task_testing,
                R.string.task_break
            )
        ),
        // Exercise
        PresetDefinition(
            titleRes = R.string.preset_exercise,
            optionRes = listOf(
                R.string.exercise_run,
                R.string.exercise_yoga,
                R.string.exercise_swim,
                R.string.exercise_basketball,
                R.string.exercise_badminton,
                R.string.exercise_walk
            )
        ),
        // Lucky
        PresetDefinition(
            titleRes = R.string.preset_lucky_number,
            optionRes = listOf(
                R.string.lucky_1,
                R.string.lucky_2,
                R.string.lucky_3,
                R.string.lucky_4,
                R.string.lucky_5,
                R.string.lucky_6,
                R.string.lucky_7,
                R.string.lucky_8,
                R.string.lucky_9
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
        // Movie Genre
        PresetDefinition(
            titleRes = R.string.preset_movie_genre,
            optionRes = listOf(
                R.string.movie_action,
                R.string.movie_comedy,
                R.string.movie_romance,
                R.string.movie_scifi,
                R.string.movie_horror,
                R.string.movie_animation,
                R.string.movie_documentary
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
        ),
        // Music Vibe
        PresetDefinition(
            titleRes = R.string.preset_music,
            optionRes = listOf(
                R.string.music_pop,
                R.string.music_rock,
                R.string.music_jazz,
                R.string.music_classical,
                R.string.music_electronic,
                R.string.music_folk,
                R.string.music_hiphop
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
