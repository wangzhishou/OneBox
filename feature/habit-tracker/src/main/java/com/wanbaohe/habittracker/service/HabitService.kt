package com.wanbaohe.habittracker.service

import com.shifenmiao.database.activity.ActivityLogRecorder
import com.shifenmiao.database.habit.entity.HabitEntity
import com.shifenmiao.database.habit.repo.HabitRepository
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.storage.AppSharedStorage
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.logger.makeLog
import com.wanbaohe.habittracker.R
import com.wanbaohe.habittracker.model.HabitIcons
import com.wanbaohe.habittracker.model.HabitRepeat
import com.wanbaohe.habittracker.reminder.HabitReminderScheduler
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

/**
 * 习惯业务门面 — UI 层与 Agent 层共用的唯一写入入口。
 *
 * 职责:
 *  - 入参校验(名称、频率)
 *  - 调用 [HabitRepository] 写库
 *  - 调用 [ActivityLogRecorder] 写活动日志
 *  - 调用 [HabitReminderScheduler] 维护每日提醒闹钟
 *
 * 不做的事:
 *  - 不持有 UI 状态
 *  - 不暴露 Flow(订阅仍走 Repository)
 */
@Singleton
class HabitService @Inject constructor(
    private val repository: HabitRepository,
    private val activityLogRecorder: ActivityLogRecorder,
    private val reminderScheduler: HabitReminderScheduler,
) {

    data class HabitInput(
        val name: String,
        val iconKey: String = HabitIcons.DEFAULT_KEY,
        /** null = 自动主题色 */
        val colorArgb: Long? = null,
        val repeatType: String = HabitRepeat.DAILY,
        val repeatTarget: Int = 1,
        val weekdaysMask: Int = 0,
        /** 一天内分钟数,null = 不提醒 */
        val remindMinutes: Int? = null,
        val note: String? = null,
        val statsEnabled: Boolean = true,
    )

    // ── 写入 ─────────────────────────────────────────

    suspend fun createHabit(input: HabitInput, actor: String): Result<String> = runCatching {
        validate(input)
        val id = UUID.randomUUID().toString()
        val entity = HabitEntity(
            id = id,
            name = input.name.trim(),
            iconKey = input.iconKey.takeIf { HabitIcons.isValidKey(it) } ?: HabitIcons.DEFAULT_KEY,
            colorArgb = input.colorArgb,
            repeatType = input.repeatType,
            repeatTarget = input.repeatTarget.coerceIn(1, 30),
            weekdaysMask = normalizeWeekdaysMask(input),
            remindMinutes = input.remindMinutes,
            note = input.note?.takeIf { it.isNotBlank() }?.trim(),
            statsEnabled = input.statsEnabled,
            sortOrder = Int.MAX_VALUE,
        )
        repository.upsertHabit(entity)
        logCreatedSafe(entity, actor)
        rescheduleReminderSafe(entity)
        makeLog { "HabitService.createHabit: ${entity.name} by $actor" }
        id
    }

    suspend fun updateHabit(habitId: String, input: HabitInput, actor: String): Result<Unit> =
        runCatching {
            validate(input)
            val previous = repository.getHabit(habitId) ?: error("habit_not_found")
            val entity = previous.copy(
                name = input.name.trim(),
                iconKey = input.iconKey.takeIf { HabitIcons.isValidKey(it) } ?: HabitIcons.DEFAULT_KEY,
                colorArgb = input.colorArgb,
                repeatType = input.repeatType,
                repeatTarget = input.repeatTarget.coerceIn(1, 30),
                weekdaysMask = normalizeWeekdaysMask(input),
                remindMinutes = input.remindMinutes,
                note = input.note?.takeIf { it.isNotBlank() }?.trim(),
                statsEnabled = input.statsEnabled,
                updatedAt = System.currentTimeMillis(),
            )
            repository.upsertHabit(entity)
            rescheduleReminderSafe(entity)
            makeLog { "HabitService.updateHabit: ${entity.name} by $actor" }
        }

    suspend fun deleteHabit(habitId: String, actor: String): Result<Unit> = runCatching {
        repository.deleteHabit(habitId)
        reminderScheduler.cancel(habitId)
        makeLog { "HabitService.deleteHabit: $habitId by $actor" }
    }

    /** 打卡。同一天重复打卡返回 false(未真正写入)。 */
    suspend fun checkIn(habitId: String, epochDay: Long, actor: String): Result<Boolean> =
        runCatching {
            val habit = repository.getHabit(habitId) ?: error("habit_not_found")
            val inserted = repository.checkIn(habitId = habitId, epochDay = epochDay)
            if (inserted) {
                logCheckInSafe(habit, epochDay, actor)
                makeLog { "HabitService.checkIn: ${habit.name} @$epochDay by $actor" }
            }
            inserted
        }

    suspend fun uncheckIn(habitId: String, epochDay: Long): Result<Unit> = runCatching {
        repository.removeCheckIn(habitId = habitId, epochDay = epochDay)
        makeLog { "HabitService.uncheckIn: $habitId @$epochDay" }
    }

    // ── 预置习惯播种 ─────────────────────────────

    /**
     * 首次进入播种 6 个预置习惯。
     *
     * 双条件:持久化 flag 未置 + 习惯表为空;flag 一旦置位,
     * 即使用户之后删光全部习惯也不再播种。名称走模块字符串资源,随系统语言。
     */
    suspend fun seedPresetHabitsIfNeeded() {
        if (AppSharedStorage.loadHabitPresetsSeeded()) return
        if (repository.observeHabits().first().isNotEmpty()) {
            // 老用户已有数据:不播种,仅标记避免重复检查
            AppSharedStorage.saveHabitPresetsSeeded(true)
            return
        }
        runCatching {
            PRESET_HABITS.forEachIndexed { index, preset ->
                repository.upsertHabit(
                    HabitEntity(
                        id = UUID.randomUUID().toString(),
                        name = AppContext.getString(preset.nameRes),
                        iconKey = preset.iconKey,
                        colorArgb = null,
                        repeatType = preset.repeatType,
                        repeatTarget = preset.repeatTarget,
                        weekdaysMask = 0,
                        remindMinutes = null,
                        note = null,
                        statsEnabled = true,
                        sortOrder = index,
                    )
                )
            }
            AppSharedStorage.saveHabitPresetsSeeded(true)
            makeLog { "HabitService.seedPresetHabitsIfNeeded: seeded ${PRESET_HABITS.size} presets" }
        }.onFailure { it.makeLog(TAG) }
    }

    /** 预置习惯定义(名称取模块字符串资源) */
    private data class PresetHabit(
        val nameRes: Int,
        val iconKey: String,
        val repeatType: String,
        val repeatTarget: Int = 1,
    )

    private val PRESET_HABITS: List<PresetHabit> = listOf(
        PresetHabit(R.string.habit_preset_drink_water, "waterdrop", HabitRepeat.DAILY),
        PresetHabit(R.string.habit_preset_wake_early, "sunrise", HabitRepeat.DAILY),
        PresetHabit(R.string.habit_preset_exercise, "running", HabitRepeat.WEEKLY_TIMES, repeatTarget = 4),
        PresetHabit(R.string.habit_preset_reading, "book", HabitRepeat.DAILY),
        PresetHabit(R.string.habit_preset_meditation, "meditation", HabitRepeat.WEEKLY_TIMES, repeatTarget = 3),
        PresetHabit(R.string.habit_preset_no_stay_up, "moon", HabitRepeat.DAILY),
    )

    // ── 只读查询(供 AgentTool 使用) ─────────────────

    suspend fun listHabits(): List<HabitEntity> {
        return repository.observeHabits().first()
    }

    suspend fun getHabit(habitId: String): HabitEntity? {
        return repository.getHabit(habitId)
    }

    /** 名称模糊匹配:优先完全相等(忽略大小写),其次包含匹配 */
    suspend fun findHabitByName(name: String): HabitEntity? {
        val target = name.trim().lowercase()
        if (target.isEmpty()) return null
        val all = repository.observeHabits().first()
        return all.firstOrNull { it.name.lowercase() == target }
            ?: all.firstOrNull { it.name.lowercase().contains(target) }
    }

    // ── 内部 ─────────────────────────────────────────

    private fun validate(input: HabitInput) {
        require(input.name.isNotBlank()) { "habit_name_blank" }
    }

    /** 自定义星期但未选任何一天时按每天处理 */
    private fun normalizeWeekdaysMask(input: HabitInput): Int {
        if (input.repeatType != HabitRepeat.CUSTOM_WEEKDAYS) return input.weekdaysMask
        return if (input.weekdaysMask == 0) HabitRepeat.ALL_WEEKDAYS_MASK else input.weekdaysMask
    }

    /** 日志失败不弄崩主流程 */
    private suspend fun logCreatedSafe(entity: HabitEntity, actor: String) {
        runCatching {
            activityLogRecorder.recordHabitCreated(
                habitId = entity.id,
                habitName = entity.name,
                actor = actor,
                screenRoute = Screen.HabitTracker().id.toString(),
            )
        }.onFailure { it.makeLog(TAG) }
    }

    /** 日志失败不弄崩主流程 */
    private suspend fun logCheckInSafe(habit: HabitEntity, epochDay: Long, actor: String) {
        runCatching {
            activityLogRecorder.recordHabitCheckIn(
                habitId = habit.id,
                habitName = habit.name,
                epochDay = epochDay,
                actor = actor,
                screenRoute = Screen.HabitTracker().id.toString(),
            )
        }.onFailure { it.makeLog(TAG) }
    }

    /** 提醒调度失败不影响写库结果 */
    private fun rescheduleReminderSafe(entity: HabitEntity) {
        runCatching {
            val minutes = entity.remindMinutes
            if (minutes != null && !entity.isArchived) {
                reminderScheduler.schedule(entity.id, entity.name, minutes)
            } else {
                reminderScheduler.cancel(entity.id)
            }
        }.onFailure { it.makeLog(TAG) }
    }

    companion object {
        const val ACTOR_USER = "USER"
        const val ACTOR_AGENT = "AGENT"

        const val SOURCE_UI = "ui:habit_tracker"
        const val SOURCE_AGENT_ADD = "agent_tool:add_habit"
        const val SOURCE_AGENT_CHECK_IN = "agent_tool:check_in_habit"

        private const val TAG = "HabitService"
    }
}
