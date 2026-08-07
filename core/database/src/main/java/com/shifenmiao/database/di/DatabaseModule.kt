package com.shifenmiao.database.di

import android.content.Context
import com.shifenmiao.database.AppDatabase
import com.shifenmiao.database.ClearDatabaseHelper
import com.shifenmiao.database.FeatureDatabase
import com.shifenmiao.database.activity.ActivityLogRecorder
import com.shifenmiao.database.activity.dao.ActivityLogDao
import com.shifenmiao.database.authcode.dao.AuthCodeDao
import com.shifenmiao.database.authcode.repo.AuthCodeRepository
import com.shifenmiao.database.agent.dao.ItemAgentDao
import com.shifenmiao.database.data_draft.DataDraftHelper
import com.shifenmiao.database.data_draft.dao.DataDraftDao
import com.shifenmiao.database.ai.dao.ConversationDao
import com.shifenmiao.database.ai.dao.MessageDao
import com.shifenmiao.database.ai.dao.ConversationToolPolicyDao
import com.shifenmiao.database.ai.dao.ToolBindingDao
import com.shifenmiao.database.ai.dao.ToolCallTaskDao
import com.shifenmiao.database.ai.dao.ToolCatalogDao
import com.shifenmiao.database.bookkeeping.dao.BookkeepingCategoryDao
import com.shifenmiao.database.bookkeeping.dao.BookkeepingRecordDao
import com.shifenmiao.database.bookkeeping.repo.BookkeepingRepository
import com.shifenmiao.database.blessing.dao.BlessingRecordDao
import com.shifenmiao.database.blessing.dao.BlessingTabConfigDao
import com.shifenmiao.database.blessing.dao.BlessingWishDao
import com.shifenmiao.database.blessing.repo.BlessingRepository
import com.shifenmiao.database.chat_prompt.dao.PromptDao
import com.shifenmiao.database.idphoto.dao.IdPhotoSizeDao
import com.shifenmiao.database.image.dao.ImageDao
import com.shifenmiao.interfaces.logging.ImageSaveLogger
import com.shifenmiao.database.item.dao.CategoryDao
import com.shifenmiao.database.item.dao.ItemDataDao
import com.shifenmiao.database.item.dao.ItemEntityDao
import com.shifenmiao.database.lifetime.dao.CountdownEventDao
import com.shifenmiao.database.lifetime.dao.FrequencyEventDao
import com.shifenmiao.database.lifetime.dao.MilestoneAiInsightDao
import com.shifenmiao.database.marktodo.dao.MarkTodoCategoryDao
import com.shifenmiao.database.marktodo.dao.MarkTodoDashboardDao
import com.shifenmiao.database.marktodo.dao.MarkTodoTaskDao
import com.shifenmiao.database.marktodo.repo.MarkTodoRepository
import com.shifenmiao.database.passwordvault.dao.PasswordVaultCategoryDao
import com.shifenmiao.database.passwordvault.dao.PasswordVaultEntryDao
import com.shifenmiao.database.passwordvault.repo.PasswordVaultRepository
import com.shifenmiao.database.recent_access.dao.RecentAccessDao
import com.shifenmiao.database.docconvert.dao.DocConvertTaskDao
import com.shifenmiao.database.habit.dao.HabitCheckInDao
import com.shifenmiao.database.habit.dao.HabitDao
import com.shifenmiao.database.habit.repo.HabitRepository
import com.shifenmiao.database.ocr.dao.PaddleOcrTaskDao
import com.shifenmiao.database.schedule.dao.ScheduleEventDao
import com.shifenmiao.database.schedule.dao.ScheduleProviderBindingDao
import com.shifenmiao.database.schedule.dao.ScheduleSyncStateDao
import com.shifenmiao.database.schedule.repo.ScheduleRepository
import com.shifenmiao.database.watermark.dao.WatermarkTemplateDao
import com.shifenmiao.database.teleprompter.dao.TeleprompterScriptDao
import com.shifenmiao.database.teleprompter.repo.TeleprompterRepository
import com.shifenmiao.database.theme.dao.ThemePresetDao
import com.shifenmiao.database.xiangqi.dao.XiangqiAiTaskDao
import com.shifenmiao.database.xiangqi.dao.XiangqiGameDao
import com.shifenmiao.database.xiangqi.dao.XiangqiPlyDao
import com.shifenmiao.database.tts.dao.TTSAudioEntryDao
import com.shifenmiao.database.decision_wheel.dao.WheelDao
import com.shifenmiao.database.transfer.ChatMessageDao
import com.shifenmiao.database.transfer.ChatSessionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstanceOrCreate(context)
    }

    @Provides
    @Singleton
    fun provideFeatureDatabase(@ApplicationContext context: Context): FeatureDatabase {
        return FeatureDatabase.getInstanceOrCreate(context)
    }

    @Provides
    fun provideMessageDao(database: AppDatabase): MessageDao {
        return database.messageDao()
    }

    @Provides
    fun provideImageDao(database: AppDatabase): ImageDao {
        return database.imageDao()
    }

    @Provides
    fun provideItemEntityDao(database: AppDatabase): ItemEntityDao {
        return database.itemEntityDao()
    }

    @Provides
    fun provideItemDataDao(database: AppDatabase): ItemDataDao {
        return database.itemDataDao()
    }

    @Provides
    fun provideAgentDao(database: AppDatabase): ItemAgentDao {
        return database.agentDao()
    }

    @Provides
    fun providePromptDao(database: AppDatabase): PromptDao {
        return database.chatPromptDao()
    }

    @Provides
    fun provideConversationDao(database: AppDatabase): ConversationDao {
        return database.conversationDao()
    }

    @Provides
    fun provideToolCallTaskDao(database: AppDatabase): ToolCallTaskDao {
        return database.toolCallTaskDao()
    }

    @Provides
    fun provideToolCatalogDao(database: AppDatabase): ToolCatalogDao {
        return database.toolCatalogDao()
    }

    @Provides
    fun provideConversationToolPolicyDao(database: AppDatabase): ConversationToolPolicyDao {
        return database.conversationToolPolicyDao()
    }

    @Provides
    fun provideToolBindingDao(database: AppDatabase): ToolBindingDao {
        return database.toolBindingDao()
    }

    @Provides
    fun provideThemePresetDao(database: AppDatabase): ThemePresetDao {
        return database.themePresetDao()
    }


    @Provides
    fun provideActivityLogDao(database: AppDatabase): ActivityLogDao {
        return database.activityLogDao()
    }

    @Provides
    @Singleton
    fun provideImageSaveLogger(recorder: ActivityLogRecorder): ImageSaveLogger {
        return recorder
    }

    @Provides
    @Singleton
    fun provideClearDatabaseHelper(database: AppDatabase): ClearDatabaseHelper {
        return ClearDatabaseHelper(database)
    }

    @Provides
    @Singleton
    fun provideCategoryDao(database: AppDatabase): CategoryDao {
        return database.categoryDao()
    }

    // MarkTodo feature DAOs
    @Provides
    fun provideMarkTodoCategoryDao(database: FeatureDatabase): MarkTodoCategoryDao {
        return database.markTodoCategoryDao()
    }

    @Provides
    fun provideMarkTodoTaskDao(database: FeatureDatabase): MarkTodoTaskDao {
        return database.markTodoTaskDao()
    }

    @Provides
    fun provideMarkTodoDashboardDao(database: FeatureDatabase): MarkTodoDashboardDao {
        return database.markTodoDashboardDao()
    }

    @Provides
    @Singleton
    fun provideMarkTodoRepository(
        dashboardDao: MarkTodoDashboardDao,
        categoryDao: MarkTodoCategoryDao,
        taskDao: MarkTodoTaskDao,
    ): MarkTodoRepository {
        return MarkTodoRepository(
            dashboardDao = dashboardDao,
            categoryDao = categoryDao,
            taskDao = taskDao,
        )
    }

    @Provides
    fun provideScheduleEventDao(database: FeatureDatabase): ScheduleEventDao {
        return database.scheduleEventDao()
    }

    @Provides
    fun provideScheduleProviderBindingDao(database: FeatureDatabase): ScheduleProviderBindingDao {
        return database.scheduleProviderBindingDao()
    }

    @Provides
    fun provideScheduleSyncStateDao(database: FeatureDatabase): ScheduleSyncStateDao {
        return database.scheduleSyncStateDao()
    }

    @Provides
    @Singleton
    fun provideScheduleRepository(
        eventDao: ScheduleEventDao,
        providerBindingDao: ScheduleProviderBindingDao,
        syncStateDao: ScheduleSyncStateDao,
    ): ScheduleRepository {
        return ScheduleRepository(
            eventDao = eventDao,
            providerBindingDao = providerBindingDao,
            syncStateDao = syncStateDao,
        )
    }

    @Provides
    fun provideFrequencyEventDao(database: FeatureDatabase): FrequencyEventDao {
        return database.frequencyEventDao()
    }

    @Provides
    fun providePersonalMilestoneDao(database: FeatureDatabase): com.shifenmiao.database.lifetime.dao.PersonalMilestoneDao {
        return database.personalMilestoneDao()
    }

    @Provides
    fun provideCountdownEventDao(database: FeatureDatabase): CountdownEventDao {
        return database.countdownEventDao()
    }

    @Provides
    fun provideMilestoneAiInsightDao(database: FeatureDatabase): MilestoneAiInsightDao {
        return database.milestoneAiInsightDao()
    }

    @Provides
    fun provideWatermarkTemplateDao(database: FeatureDatabase): WatermarkTemplateDao {
        return database.watermarkTemplateDao()
    }

    @Provides
    fun provideIdPhotoSizeDao(database: FeatureDatabase): IdPhotoSizeDao {
        return database.idPhotoSizeDao()
    }

    @Provides
    fun providePaddleOcrTaskDao(database: FeatureDatabase): PaddleOcrTaskDao {
        return database.paddleOcrTaskDao()
    }

    @Provides
    fun provideDocConvertTaskDao(database: FeatureDatabase): DocConvertTaskDao {
        return database.docConvertTaskDao()
    }

    @Provides
    fun provideAltitudeRecordDao(database: FeatureDatabase): com.shifenmiao.database.altitude.dao.AltitudeRecordDao {
        return database.altitudeRecordDao()
    }

    @Provides
    fun provideSpeedTestRecordDao(database: FeatureDatabase): com.shifenmiao.database.speedtest.dao.SpeedTestRecordDao {
        return database.speedTestRecordDao()
    }

    @Provides
    fun provideSpeedTestConfigDao(database: FeatureDatabase): com.shifenmiao.database.speedtest.dao.SpeedTestConfigDao {
        return database.speedTestConfigDao()
    }

    @Provides
    fun provideBookkeepingCategoryDao(database: FeatureDatabase): BookkeepingCategoryDao {
        return database.bookkeepingCategoryDao()
    }

    @Provides
    fun provideBookkeepingRecordDao(database: FeatureDatabase): BookkeepingRecordDao {
        return database.bookkeepingRecordDao()
    }

    @Provides
    @Singleton
    fun provideBookkeepingRepository(
        database: FeatureDatabase,
        categoryDao: BookkeepingCategoryDao,
        recordDao: BookkeepingRecordDao,
    ): BookkeepingRepository {
        return BookkeepingRepository(
            database = database,
            categoryDao = categoryDao,
            recordDao = recordDao,
        )
    }

    // HabitTracker feature
    @Provides
    fun provideHabitDao(database: FeatureDatabase): HabitDao {
        return database.habitDao()
    }

    @Provides
    fun provideHabitCheckInDao(database: FeatureDatabase): HabitCheckInDao {
        return database.habitCheckInDao()
    }

    @Provides
    @Singleton
    fun provideHabitRepository(
        database: FeatureDatabase,
        habitDao: HabitDao,
        checkInDao: HabitCheckInDao,
    ): HabitRepository {
        return HabitRepository(
            database = database,
            habitDao = habitDao,
            checkInDao = checkInDao,
        )
    }

    // Teleprompter feature
    @Provides
    fun provideTeleprompterScriptDao(database: FeatureDatabase): TeleprompterScriptDao {
        return database.teleprompterScriptDao()
    }

    @Provides
    fun provideXiangqiGameDao(database: FeatureDatabase): XiangqiGameDao {
        return database.xiangqiGameDao()
    }

    @Provides
    fun provideXiangqiPlyDao(database: FeatureDatabase): XiangqiPlyDao {
        return database.xiangqiPlyDao()
    }

    @Provides
    fun provideXiangqiAiTaskDao(database: FeatureDatabase): XiangqiAiTaskDao {
        return database.xiangqiAiTaskDao()
    }

    @Provides
    fun provideTTSAudioEntryDao(database: AppDatabase): TTSAudioEntryDao {
        return database.ttsAudioEntryDao()
    }

    @Provides
    fun provideWheelDao(database: FeatureDatabase): WheelDao {
        return database.wheelDao()
    }

    @Provides
    fun provideChatMessageDao(database: FeatureDatabase): ChatMessageDao {
        return database.chatMessageDao()
    }

    @Provides
    fun provideChatSessionDao(database: FeatureDatabase): ChatSessionDao {
        return database.chatSessionDao()
    }

    @Provides
    @Singleton
    fun provideTeleprompterRepository(
        dao: TeleprompterScriptDao,
    ): TeleprompterRepository {
        return TeleprompterRepository(dao = dao)
    }

    // Unified DataDraft feature
    @Provides
    fun provideDataDraftDao(database: FeatureDatabase): DataDraftDao {
        return database.dataDraftDao()
    }

    @Provides
    @Singleton
    fun provideDataDraftHelper(dataDraftDao: DataDraftDao): DataDraftHelper {
        return DataDraftHelper(dataDraftDao)
    }

    @Provides
    fun provideRecentAccessDao(database: FeatureDatabase): RecentAccessDao {
        return database.recentAccessDao()
    }

    @Provides
    fun providePasswordVaultEntryDao(database: AppDatabase): PasswordVaultEntryDao {
        return database.passwordVaultEntryDao()
    }

    @Provides
    fun providePasswordVaultCategoryDao(database: AppDatabase): PasswordVaultCategoryDao {
        return database.passwordVaultCategoryDao()
    }

    @Provides
    @Singleton
    fun providePasswordVaultRepository(
        entryDao: PasswordVaultEntryDao,
        categoryDao: PasswordVaultCategoryDao,
    ): PasswordVaultRepository = PasswordVaultRepository(
        entryDao = entryDao,
        categoryDao = categoryDao,
    )

    @Provides
    fun provideAuthCodeDao(database: AppDatabase): AuthCodeDao =
        database.authCodeDao()

    @Provides
    @Singleton
    fun provideAuthCodeRepository(dao: AuthCodeDao): AuthCodeRepository =
        AuthCodeRepository(dao)

    @Provides
    fun provideBlessingRecordDao(database: FeatureDatabase): BlessingRecordDao {
        return database.blessingRecordDao()
    }

    @Provides
    fun provideBlessingWishDao(database: FeatureDatabase): BlessingWishDao {
        return database.blessingWishDao()
    }

    @Provides
    fun provideBlessingTabConfigDao(database: FeatureDatabase): BlessingTabConfigDao {
        return database.blessingTabConfigDao()
    }

    @Provides
    @Singleton
    fun provideBlessingRepository(
        recordDao: BlessingRecordDao,
        wishDao: BlessingWishDao,
        tabConfigDao: BlessingTabConfigDao,
    ): BlessingRepository {
        return BlessingRepository(
            recordDao = recordDao,
            wishDao = wishDao,
            tabConfigDao = tabConfigDao,
        )
    }
}
