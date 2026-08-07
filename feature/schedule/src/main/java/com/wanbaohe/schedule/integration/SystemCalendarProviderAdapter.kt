package com.wanbaohe.schedule.integration

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import com.wanbaohe.schedule.model.ScheduleEvent

/**
 * 系统日历 Provider 适配器（第二阶段）。
 *
 * 相比仅唤起系统日历插入页，这里会直接：
 * 1. 查询可写日历
 * 2. 由用户选择目标日历
 * 3. 直接写入 Calendar Provider
 *
 * 当前职责只覆盖单向写入，不做双向同步。
 */
object SystemCalendarProviderAdapter {

    data class WritableCalendar(
        val id: Long,
        val displayName: String,
        val accountName: String?,
        val ownerAccount: String?,
        val isPrimary: Boolean,
    ) {
        val summary: String
            get() = buildString {
                append(displayName)
                accountName?.takeIf { it.isNotBlank() }?.let {
                    append(" · ").append(it)
                }
            }
    }

    fun hasCalendarPermissions(context: Context): Boolean {
        val readGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CALENDAR,
        ) == PackageManager.PERMISSION_GRANTED
        val writeGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_CALENDAR,
        ) == PackageManager.PERMISSION_GRANTED
        return readGranted && writeGranted
    }

    fun queryWritableCalendars(context: Context): List<WritableCalendar> {
        if (!hasCalendarPermissions(context)) return emptyList()

        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Calendars.ACCOUNT_NAME,
            CalendarContract.Calendars.OWNER_ACCOUNT,
            CalendarContract.Calendars.IS_PRIMARY,
            CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL,
            CalendarContract.Calendars.VISIBLE,
            CalendarContract.Calendars.SYNC_EVENTS,
        )

        val selection = buildString {
            append(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL)
            append(" >= ? AND ")
            append(CalendarContract.Calendars.VISIBLE)
            append(" = 1")
        }
        val selectionArgs = arrayOf(CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR.toString())

        return buildList {
            context.contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                "${CalendarContract.Calendars.IS_PRIMARY} DESC, ${CalendarContract.Calendars.CALENDAR_DISPLAY_NAME} ASC",
            )?.use { cursor ->
                val idIndex = cursor.getColumnIndexOrThrow(CalendarContract.Calendars._ID)
                val displayNameIndex = cursor.getColumnIndexOrThrow(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)
                val accountNameIndex = cursor.getColumnIndexOrThrow(CalendarContract.Calendars.ACCOUNT_NAME)
                val ownerAccountIndex = cursor.getColumnIndexOrThrow(CalendarContract.Calendars.OWNER_ACCOUNT)
                val primaryIndex = cursor.getColumnIndexOrThrow(CalendarContract.Calendars.IS_PRIMARY)
                val syncEventsIndex = cursor.getColumnIndexOrThrow(CalendarContract.Calendars.SYNC_EVENTS)

                while (cursor.moveToNext()) {
                    val syncEvents = cursor.getInt(syncEventsIndex) == 1
                    if (!syncEvents) continue

                    add(
                        WritableCalendar(
                            id = cursor.getLong(idIndex),
                            displayName = cursor.getString(displayNameIndex).orEmpty(),
                            accountName = cursor.getString(accountNameIndex),
                            ownerAccount = cursor.getString(ownerAccountIndex),
                            isPrimary = cursor.getInt(primaryIndex) == 1,
                        )
                    )
                }
            }
        }
    }

    fun insertEvent(
        context: Context,
        calendarId: Long,
        event: ScheduleEvent,
    ): Result<Long> = runCatching {
        require(hasCalendarPermissions(context)) { "calendar permission denied" }

        val values = ContentValues().apply {
            put(CalendarContract.Events.CALENDAR_ID, calendarId)
            put(CalendarContract.Events.TITLE, event.title)
            put(CalendarContract.Events.DESCRIPTION, event.description)
            put(CalendarContract.Events.EVENT_LOCATION, event.location)
            put(CalendarContract.Events.DTSTART, event.startUtcMillis)
            put(CalendarContract.Events.DTEND, event.endUtcMillis)
            put(CalendarContract.Events.ALL_DAY, event.isAllDay)
            put(CalendarContract.Events.EVENT_TIMEZONE, event.timeZoneId)
            put(CalendarContract.Events.HAS_ALARM, 0)
            event.recurrenceRule?.let {
                put(CalendarContract.Events.RRULE, it)
            }
        }

        val insertedUri = context.contentResolver.insert(
            CalendarContract.Events.CONTENT_URI,
            values,
        ) ?: error("insert returned null")

        insertedUri.lastPathSegment?.toLongOrNull() ?: error("invalid inserted event id")
    }

    fun updateEvent(
        context: Context,
        remoteEventId: Long,
        calendarId: Long,
        event: ScheduleEvent,
    ): Result<Unit> = runCatching {
        require(hasCalendarPermissions(context)) { "calendar permission denied" }

        val values = ContentValues().apply {
            put(CalendarContract.Events.CALENDAR_ID, calendarId)
            put(CalendarContract.Events.TITLE, event.title)
            put(CalendarContract.Events.DESCRIPTION, event.description)
            put(CalendarContract.Events.EVENT_LOCATION, event.location)
            put(CalendarContract.Events.DTSTART, event.startUtcMillis)
            put(CalendarContract.Events.DTEND, event.endUtcMillis)
            put(CalendarContract.Events.ALL_DAY, event.isAllDay)
            put(CalendarContract.Events.EVENT_TIMEZONE, event.timeZoneId)
            put(CalendarContract.Events.HAS_ALARM, 0)
            put(CalendarContract.Events.RRULE, event.recurrenceRule)
        }

        val affected = context.contentResolver.update(
            CalendarContract.Events.CONTENT_URI,
            values,
            "${CalendarContract.Events._ID} = ?",
            arrayOf(remoteEventId.toString()),
        )
        require(affected > 0) { "system calendar event not found" }
    }

    fun deleteEvent(
        context: Context,
        remoteEventId: Long,
    ): Result<Unit> = runCatching {
        require(hasCalendarPermissions(context)) { "calendar permission denied" }

        context.contentResolver.delete(
            CalendarContract.Events.CONTENT_URI,
            "${CalendarContract.Events._ID} = ?",
            arrayOf(remoteEventId.toString()),
        )
    }
}

