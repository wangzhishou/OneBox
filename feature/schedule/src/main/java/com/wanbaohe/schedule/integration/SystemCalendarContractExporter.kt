package com.wanbaohe.schedule.integration

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import com.wanbaohe.schedule.model.ScheduleEvent

/**
 * 系统日历导出器（第一阶段）。
 *
 * 当前实现采用 `ACTION_INSERT + CalendarContract.Events.CONTENT_URI`：
 * - 不直接写系统日历数据库
 * - 不需要 `WRITE_CALENDAR` 运行时权限
 * - 由系统日历应用承接最终保存动作
 *
 * 这适合作为 provider adapter 的第一步，后续再升级为真正的双向同步。
 */
object SystemCalendarContractExporter {

    fun exportEvent(context: Context, event: ScheduleEvent): Boolean {
        val intent = buildInsertIntent(event)
        return if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
            true
        } else {
            false
        }
    }

    fun buildInsertIntent(event: ScheduleEvent): Intent {
        return Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, event.title)
            putExtra(CalendarContract.Events.DESCRIPTION, event.description)
            putExtra(CalendarContract.Events.EVENT_LOCATION, event.location)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, event.startUtcMillis)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, event.endUtcMillis)
            putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, event.isAllDay)
            putExtra(CalendarContract.Events.EVENT_TIMEZONE, event.timeZoneId)
            event.recurrenceRule?.let {
                putExtra(CalendarContract.Events.RRULE, it)
            }
        }
    }
}

