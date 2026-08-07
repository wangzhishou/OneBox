package com.shifenmiao.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.shifenmiao.database.altitude.dao.AltitudeRecordDao
import com.shifenmiao.database.altitude.entity.AltitudeRecordEntity
import com.shifenmiao.database.bookkeeping.dao.BookkeepingCategoryDao
import com.shifenmiao.database.bookkeeping.dao.BookkeepingRecordDao
import com.shifenmiao.database.bookkeeping.entity.BookkeepingCategoryEntity
import com.shifenmiao.database.bookkeeping.entity.BookkeepingRecordEntity
import com.shifenmiao.database.blessing.dao.BlessingRecordDao
import com.shifenmiao.database.blessing.dao.BlessingTabConfigDao
import com.shifenmiao.database.blessing.dao.BlessingWishDao
import com.shifenmiao.database.blessing.entity.BlessingRecordEntity
import com.shifenmiao.database.blessing.entity.BlessingTabConfigEntity
import com.shifenmiao.database.blessing.entity.BlessingWishEntity
import com.shifenmiao.database.data_draft.dao.DataDraftDao
import com.shifenmiao.database.data_draft.entity.DataDraftEntity
import com.shifenmiao.database.decision_wheel.dao.WheelDao
import com.shifenmiao.database.decision_wheel.entity.WheelEntity
import com.shifenmiao.database.decision_wheel.entity.WheelHistoryEntity
import com.shifenmiao.database.decision_wheel.entity.WheelOptionEntity
import com.shifenmiao.database.docconvert.dao.DocConvertTaskDao
import com.shifenmiao.database.docconvert.entity.DocConvertTaskEntity
import com.shifenmiao.database.habit.dao.HabitCheckInDao
import com.shifenmiao.database.habit.dao.HabitDao
import com.shifenmiao.database.habit.entity.HabitCheckInEntity
import com.shifenmiao.database.habit.entity.HabitEntity
import com.shifenmiao.database.idphoto.dao.IdPhotoSizeDao
import com.shifenmiao.database.idphoto.entity.IdPhotoSizeEntity
import com.shifenmiao.database.lifetime.dao.CountdownEventDao
import com.shifenmiao.database.lifetime.dao.FrequencyEventDao
import com.shifenmiao.database.lifetime.dao.MilestoneAiInsightDao
import com.shifenmiao.database.lifetime.dao.PersonalMilestoneDao
import com.shifenmiao.database.lifetime.entity.CountdownEventEntity
import com.shifenmiao.database.lifetime.entity.FrequencyEventEntity
import com.shifenmiao.database.lifetime.entity.MilestoneAiInsightEntity
import com.shifenmiao.database.lifetime.entity.PersonalMilestoneEntity
import com.shifenmiao.database.marktodo.MarkTodoTypeConverters
import com.shifenmiao.database.marktodo.dao.MarkTodoCategoryDao
import com.shifenmiao.database.marktodo.dao.MarkTodoDashboardDao
import com.shifenmiao.database.marktodo.dao.MarkTodoTaskDao
import com.shifenmiao.database.marktodo.entity.MarkTodoCategoryEntity
import com.shifenmiao.database.marktodo.entity.MarkTodoTaskEntity
import com.shifenmiao.database.ocr.dao.PaddleOcrTaskDao
import com.shifenmiao.database.ocr.entity.PaddleOcrTaskEntity
import com.shifenmiao.database.recent_access.dao.RecentAccessDao
import com.shifenmiao.database.recent_access.entity.RecentAccessEntity
import com.shifenmiao.database.schedule.dao.ScheduleEventDao
import com.shifenmiao.database.schedule.dao.ScheduleProviderBindingDao
import com.shifenmiao.database.schedule.dao.ScheduleSyncStateDao
import com.shifenmiao.database.schedule.entity.ScheduleEventEntity
import com.shifenmiao.database.schedule.entity.ScheduleProviderBindingEntity
import com.shifenmiao.database.schedule.entity.ScheduleSyncStateEntity
import com.shifenmiao.database.speedtest.dao.SpeedTestConfigDao
import com.shifenmiao.database.speedtest.dao.SpeedTestRecordDao
import com.shifenmiao.database.speedtest.entity.SpeedTestConfigEntity
import com.shifenmiao.database.speedtest.entity.SpeedTestRecordEntity
import com.shifenmiao.database.teleprompter.dao.TeleprompterScriptDao
import com.shifenmiao.database.teleprompter.entity.TeleprompterScriptEntity
import com.shifenmiao.database.transfer.ChatMessageDao
import com.shifenmiao.database.transfer.ChatMessageEntity
import com.shifenmiao.database.transfer.ChatSessionDao
import com.shifenmiao.database.transfer.ChatSessionEntity
import com.shifenmiao.database.watermark.dao.WatermarkTemplateDao
import com.shifenmiao.database.watermark.entity.WatermarkTemplateEntity
import com.shifenmiao.database.xiangqi.dao.XiangqiAiTaskDao
import com.shifenmiao.database.xiangqi.dao.XiangqiGameDao
import com.shifenmiao.database.xiangqi.dao.XiangqiPlyDao
import com.shifenmiao.database.xiangqi.entity.XiangqiAiTaskEntity
import com.shifenmiao.database.xiangqi.entity.XiangqiGameEntity
import com.shifenmiao.database.xiangqi.entity.XiangqiPlyEntity
import com.t8rin.imagetoolbox.core.utils.LocaleUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Feature-only database.
 *
 * IMPORTANT: This is intentionally separated from [AppDatabase] so that feature schemas can evolve
 * independently without affecting the main "one_box" database.
 */
@Database(
    entities = [
        MarkTodoCategoryEntity::class,
        MarkTodoTaskEntity::class,
        FrequencyEventEntity::class,
        PersonalMilestoneEntity::class,
        CountdownEventEntity::class,
        MilestoneAiInsightEntity::class,
        WatermarkTemplateEntity::class,
        IdPhotoSizeEntity::class,
        PaddleOcrTaskEntity::class,
        DocConvertTaskEntity::class,
        ScheduleEventEntity::class,
        ScheduleProviderBindingEntity::class,
        ScheduleSyncStateEntity::class,
        AltitudeRecordEntity::class,
        SpeedTestRecordEntity::class,
        SpeedTestConfigEntity::class,
        BookkeepingCategoryEntity::class,
        BookkeepingRecordEntity::class,
        TeleprompterScriptEntity::class,
        DataDraftEntity::class,
        XiangqiGameEntity::class,
        XiangqiPlyEntity::class,
        XiangqiAiTaskEntity::class,
        WheelEntity::class,
        WheelOptionEntity::class,
        WheelHistoryEntity::class,
        ChatMessageEntity::class,
        ChatSessionEntity::class,
        RecentAccessEntity::class,
        BlessingRecordEntity::class,
        BlessingWishEntity::class,
        BlessingTabConfigEntity::class,
        HabitEntity::class,
        HabitCheckInEntity::class,
    ],
    version = 3,
    exportSchema = true
)
@TypeConverters(MarkTodoTypeConverters::class)
abstract class FeatureDatabase : RoomDatabase() {

    abstract fun paddleOcrTaskDao(): PaddleOcrTaskDao
    abstract fun docConvertTaskDao(): DocConvertTaskDao

    abstract fun markTodoCategoryDao(): MarkTodoCategoryDao

    abstract fun markTodoTaskDao(): MarkTodoTaskDao

    abstract fun markTodoDashboardDao(): MarkTodoDashboardDao

    abstract fun scheduleEventDao(): ScheduleEventDao

    abstract fun scheduleProviderBindingDao(): ScheduleProviderBindingDao

    abstract fun scheduleSyncStateDao(): ScheduleSyncStateDao

    abstract fun frequencyEventDao(): FrequencyEventDao

    abstract fun personalMilestoneDao(): PersonalMilestoneDao

    abstract fun countdownEventDao(): CountdownEventDao

    abstract fun milestoneAiInsightDao(): MilestoneAiInsightDao

    abstract fun watermarkTemplateDao(): WatermarkTemplateDao

    abstract fun idPhotoSizeDao(): IdPhotoSizeDao

    abstract fun altitudeRecordDao(): AltitudeRecordDao

    abstract fun speedTestRecordDao(): SpeedTestRecordDao

    abstract fun speedTestConfigDao(): SpeedTestConfigDao

    abstract fun bookkeepingCategoryDao(): BookkeepingCategoryDao

    abstract fun bookkeepingRecordDao(): BookkeepingRecordDao

    abstract fun teleprompterScriptDao(): TeleprompterScriptDao

    abstract fun dataDraftDao(): DataDraftDao

    abstract fun xiangqiGameDao(): XiangqiGameDao

    abstract fun xiangqiPlyDao(): XiangqiPlyDao

    abstract fun xiangqiAiTaskDao(): XiangqiAiTaskDao

    abstract fun wheelDao(): WheelDao

    abstract fun chatMessageDao(): ChatMessageDao

    abstract fun chatSessionDao(): ChatSessionDao

    abstract fun recentAccessDao(): RecentAccessDao

    abstract fun blessingRecordDao(): BlessingRecordDao
    abstract fun blessingWishDao(): BlessingWishDao
    abstract fun blessingTabConfigDao(): BlessingTabConfigDao

    abstract fun habitDao(): HabitDao

    abstract fun habitCheckInDao(): HabitCheckInDao

    companion object {
        private val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `blessing_record` (
                        `id` TEXT NOT NULL,
                        `date` TEXT NOT NULL,
                        `type` TEXT NOT NULL,
                        `count` INTEGER NOT NULL DEFAULT 0,
                        `updated_at` INTEGER NOT NULL DEFAULT 0,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_blessing_record_date` ON `blessing_record` (`date`)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_blessing_record_date_type` ON `blessing_record` (`date`, `type`)")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `blessing_wish` (
                        `date` TEXT NOT NULL,
                        `type` TEXT NOT NULL,
                        `content` TEXT NOT NULL,
                        `updated_at` INTEGER NOT NULL,
                        PRIMARY KEY(`date`, `type`)
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_blessing_wish_date` ON `blessing_wish` (`date`)")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `blessing_tab_config` (
                        `date` TEXT NOT NULL,
                        `type` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `subtitle` TEXT NOT NULL,
                        `updated_at` INTEGER NOT NULL,
                        PRIMARY KEY(`date`, `type`)
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `habit` (
                        `id` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `icon_key` TEXT NOT NULL DEFAULT 'waterdrop',
                        `color_argb` INTEGER,
                        `repeat_type` TEXT NOT NULL DEFAULT 'DAILY',
                        `repeat_target` INTEGER NOT NULL DEFAULT 1,
                        `weekdays_mask` INTEGER NOT NULL DEFAULT 0,
                        `remind_minutes` INTEGER,
                        `note` TEXT,
                        `stats_enabled` INTEGER NOT NULL DEFAULT 1,
                        `sort_order` INTEGER NOT NULL DEFAULT 0,
                        `is_archived` INTEGER NOT NULL DEFAULT 0,
                        `created_at` INTEGER NOT NULL DEFAULT 0,
                        `updated_at` INTEGER NOT NULL DEFAULT 0,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `habit_check_in` (
                        `id` TEXT NOT NULL,
                        `habit_id` TEXT NOT NULL,
                        `date_epoch_day` INTEGER NOT NULL,
                        `checked_at` INTEGER NOT NULL DEFAULT 0,
                        PRIMARY KEY(`id`),
                        FOREIGN KEY(`habit_id`) REFERENCES `habit`(`id`)
                            ON UPDATE CASCADE ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_habit_check_in_habit_id_date_epoch_day` ON `habit_check_in` (`habit_id`, `date_epoch_day`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_habit_check_in_date_epoch_day` ON `habit_check_in` (`date_epoch_day`)")
            }
        }

        const val DB_NAME_PREFIX: String = "feature"

        @Volatile
        private var INSTANCE: FeatureDatabase? = null

        @Volatile
        private var INSTANCE_LOCALE: String? = null

        fun dbNameForLocale(locale: String = LocaleUtils.getCurrentLocaleTag()): String {
            return "${DB_NAME_PREFIX}_${locale}.db"
        }

        fun closeInstance() {
            synchronized(this) {
                try {
                    INSTANCE?.close()
                } catch (_: Exception) {
                    // ignore
                } finally {
                    INSTANCE = null
                    INSTANCE_LOCALE = null
                }
            }
        }

        fun getInstanceOrCreate(@ApplicationContext context: Context): FeatureDatabase {
            val currentLocale = LocaleUtils.getCurrentLocaleTag()
            val currentDbName = dbNameForLocale(currentLocale)

            val existing = INSTANCE
            if (existing != null && INSTANCE_LOCALE == currentLocale) {
                return existing
            }

            return synchronized(this) {
                if (INSTANCE != null && INSTANCE_LOCALE == currentLocale) {
                    return@synchronized INSTANCE!!
                }

                closeInstance()

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FeatureDatabase::class.java,
                    currentDbName
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .fallbackToDestructiveMigration(true)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            try {
                                val inputStream =
                                    context.resources.openRawResource(R.raw.marktodo_presets)
                                val reader = BufferedReader(InputStreamReader(inputStream))
                                val sql = StringBuilder()
                                var line: String?
                                db.beginTransaction()
                                try {
                                    while (reader.readLine().also { line = it } != null) {
                                        val trimmedLine = line!!.trim()
                                        if (trimmedLine.isEmpty() || trimmedLine.startsWith("--")) {
                                            continue
                                        }
                                        sql.append(line).append("\n")
                                        if (trimmedLine.endsWith(";")) {
                                            db.execSQL(sql.toString())
                                            sql.setLength(0)
                                        }
                                    }
                                    db.setTransactionSuccessful()
                                } finally {
                                    db.endTransaction()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }

                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            // Ensure foreign keys are enforced.
                            db.execSQL("PRAGMA foreign_keys=ON")
                        }
                    })
                    .build()

                INSTANCE = instance
                INSTANCE_LOCALE = currentLocale
                instance
            }
        }
    }
}
