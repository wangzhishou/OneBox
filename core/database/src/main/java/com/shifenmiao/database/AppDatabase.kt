package com.shifenmiao.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.shifenmiao.database.activity.dao.ActivityLogDao
import com.shifenmiao.database.activity.entity.ActivityLogEntity
import com.shifenmiao.database.blog.dao.BlogArticleDao
import com.shifenmiao.database.blog.entity.BlogArticleEntity
import com.shifenmiao.database.authcode.dao.AuthCodeDao
import com.shifenmiao.database.authcode.entity.AuthCodeEntity
import com.shifenmiao.database.agent.dao.ItemAgentDao
import com.shifenmiao.database.agent.entity.ItemAgentEntity
import com.shifenmiao.database.ai.converters.Converters
import com.shifenmiao.database.ai.dao.AiEngineDao
import com.shifenmiao.database.ai.dao.AiModelDao
import com.shifenmiao.database.ai.dao.ConversationDao
import com.shifenmiao.database.ai.dao.ConversationToolPolicyDao
import com.shifenmiao.database.ai.dao.MessageDao
import com.shifenmiao.database.ai.dao.ToolBindingDao
import com.shifenmiao.database.ai.dao.ToolCallTaskDao
import com.shifenmiao.database.ai.dao.ToolCatalogDao
import com.shifenmiao.database.ai.entity.AiEngineEntity
import com.shifenmiao.database.ai.entity.AiModelEntity
import com.shifenmiao.database.ai.entity.ConversationEntity
import com.shifenmiao.database.ai.entity.ConversationToolPolicyEntity
import com.shifenmiao.database.ai.entity.MessageEntity
import com.shifenmiao.database.ai.entity.ToolBindingEntity
import com.shifenmiao.database.ai.entity.ToolCallTaskEntity
import com.shifenmiao.database.ai.entity.ToolCatalogEntity
import com.shifenmiao.database.chat_prompt.dao.PromptDao
import com.shifenmiao.database.chat_prompt.entity.PromptEntity
import com.shifenmiao.database.converters.SourceTypeConverter
import com.shifenmiao.database.image.dao.ImageDao
import com.shifenmiao.database.image.entity.ImageEntity
import com.shifenmiao.database.item.dao.CategoryDao
import com.shifenmiao.database.item.dao.ItemDataDao
import com.shifenmiao.database.item.dao.ItemEntityDao
import com.shifenmiao.database.item.entity.Category
import com.shifenmiao.database.item.entity.ItemAgentLink
import com.shifenmiao.database.item.entity.ItemCategoryCrossRef
import com.shifenmiao.database.item.entity.ItemClickStatEntity
import com.shifenmiao.database.item.entity.ItemDataEntity
import com.shifenmiao.database.item.entity.ItemDataLink
import com.shifenmiao.database.item.entity.ItemEntity
import com.shifenmiao.database.item.entity.ItemPromptLink
import com.shifenmiao.database.item.entity.ItemUserState
import com.shifenmiao.database.passwordvault.dao.PasswordVaultCategoryDao
import com.shifenmiao.database.passwordvault.dao.PasswordVaultEntryDao
import com.shifenmiao.database.passwordvault.entity.PasswordVaultCategoryEntity
import com.shifenmiao.database.passwordvault.entity.PasswordVaultEntryEntity
import com.shifenmiao.database.search.dao.SearchResultDao
import com.shifenmiao.database.search.entity.SearchResultEntity
import com.shifenmiao.database.theme.dao.ThemePresetDao
import com.shifenmiao.database.theme.entity.ThemePresetEntity
import com.shifenmiao.database.tts.dao.TTSAudioEntryDao
import com.shifenmiao.database.tts.entity.TTSAudioEntryEntity
import com.shifenmiao.model.Source
import com.shifenmiao.storage.AppSharedStorage
import com.t8rin.imagetoolbox.core.utils.LocaleUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

@Database(
    entities = [
        MessageEntity::class,
        ItemEntity::class,
        ItemUserState::class,
        ItemClickStatEntity::class,
        Category::class,
        ItemCategoryCrossRef::class,
        ItemDataEntity::class,
        ItemDataLink::class,
        ItemAgentEntity::class,
        ItemAgentLink::class,
        PromptEntity::class,
        ItemPromptLink::class,
        ConversationEntity::class,
        ActivityLogEntity::class,
        AiModelEntity::class,
        AiEngineEntity::class,
        ImageEntity::class,
        SearchResultEntity::class,
        ToolCallTaskEntity::class,
        ThemePresetEntity::class,
        ToolCatalogEntity::class,
        ConversationToolPolicyEntity::class,
        ToolBindingEntity::class,
        TTSAudioEntryEntity::class,
        PasswordVaultEntryEntity::class,
        PasswordVaultCategoryEntity::class,
        AuthCodeEntity::class,
        BlogArticleEntity::class,
    ],
    version = 2
)
@TypeConverters(Converters::class, SourceTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun blogArticleDao(): BlogArticleDao

    abstract fun searchResultDao(): SearchResultDao

    abstract fun imageDao(): ImageDao

    abstract fun aiEngineDao(): AiEngineDao

    abstract fun aiModelDao(): AiModelDao

    abstract fun itemEntityDao(): ItemEntityDao

    abstract fun itemDataDao(): ItemDataDao

    abstract fun messageDao(): MessageDao

    abstract fun conversationDao(): ConversationDao

    abstract fun agentDao(): ItemAgentDao

    abstract fun chatPromptDao(): PromptDao

    abstract fun categoryDao(): CategoryDao

    abstract fun activityLogDao(): ActivityLogDao

    abstract fun toolCallTaskDao(): ToolCallTaskDao

    abstract fun themePresetDao(): ThemePresetDao

    abstract fun toolCatalogDao(): ToolCatalogDao

    abstract fun conversationToolPolicyDao(): ConversationToolPolicyDao

    abstract fun toolBindingDao(): ToolBindingDao

    abstract fun ttsAudioEntryDao(): TTSAudioEntryDao

    abstract fun passwordVaultEntryDao(): PasswordVaultEntryDao

    abstract fun passwordVaultCategoryDao(): PasswordVaultCategoryDao

    abstract fun authCodeDao(): AuthCodeDao

    companion object {
        /**
         * 系统预置版本号：递增会强制重新插入 item_prompt 系统行。
         * v2 新增 AI 对聊/互动 prompt 模板预置。
         * v3 raw-en 默认系统提示词英文化(原为中文), en 语言库需重刷。
         * v4 raw-en 新增英文 AI 对聊/互动模板, en 语言库需重刷。
         * v5 AI 互聊模板扁平化(删除模式/主题维度), 需重刷覆盖旧的分模式模板。
         * v6 数据库 v2 迁移重建内容表(同步主键改 document_id), 系统预置 prompt 随表清空需重刷。
         * v7 新增易经卜卦解读系统提示词。
         * v8 易经卜卦解读 prompt 丰富化(引卦辞/爻辞/易传原文), 需重刷覆盖旧版。
         */
        private const val SYSTEM_PRESET_VERSION = 8

        /**
         * v1 → v2：同步主键从 (source, remote_id) 全局切换为 (source, document_id)。
         *
         * Strapi v5 draft-and-publish 会让数字行 id 在重发后漂移，按 remoteId 去重失效。
         * 内容表（item 及其资源/关联表）直接 DROP 重建：启动时版本变化已触发清同步水位
         * + 全量重拉（见 ItemSyncManager.syncAllOnAppLaunch），内容会自愈重建；
         * 用户数据表 item_user_state / item_click_stat 保留（死引用经 INNER JOIN 自然隐藏）。
         * 系统预置 prompt 靠 SYSTEM_PRESET_VERSION 递增重刷。
         *
         * 建表 SQL 与实体定义严格一致（取自 Room 生成的 schemas/2.json createSql）。
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 先删关联/资源表，最后删主表，避免 FK 引用悬空
                db.execSQL("DROP TABLE IF EXISTS `item_category`")
                db.execSQL("DROP TABLE IF EXISTS `item_agent_link`")
                db.execSQL("DROP TABLE IF EXISTS `item_prompt_link`")
                db.execSQL("DROP TABLE IF EXISTS `item_data_link`")
                db.execSQL("DROP TABLE IF EXISTS `item_agent`")
                db.execSQL("DROP TABLE IF EXISTS `item_prompt`")
                db.execSQL("DROP TABLE IF EXISTS `item_data`")
                db.execSQL("DROP TABLE IF EXISTS `category`")
                db.execSQL("DROP TABLE IF EXISTS `item`")

                db.execSQL("CREATE TABLE IF NOT EXISTS `item` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `remote_id` INTEGER, `source` INTEGER NOT NULL DEFAULT 0, `list_type` INTEGER NOT NULL, `title` TEXT NOT NULL, `description` TEXT NOT NULL, `url` TEXT NOT NULL, `mini_program_id` TEXT NOT NULL, `placeholder` TEXT NOT NULL, `icon_path` TEXT, `icon_name` TEXT, `recommend` INTEGER NOT NULL DEFAULT 0, `vip_level` INTEGER NOT NULL DEFAULT 0, `is_highlighted` INTEGER NOT NULL DEFAULT 0, `is_online` INTEGER NOT NULL DEFAULT 0, `is_ai` INTEGER NOT NULL DEFAULT 0, `created_at` INTEGER NOT NULL, `updated_at` INTEGER NOT NULL, `published_at` INTEGER, `document_id` TEXT, `comment_count` INTEGER)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_item_source_document_id` ON `item` (`source`, `document_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_item_list_type` ON `item` (`list_type`)")

                db.execSQL("CREATE TABLE IF NOT EXISTS `category` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `document_id` TEXT, `can_edit` INTEGER NOT NULL DEFAULT 0, `source` INTEGER NOT NULL DEFAULT 0, `updated_at` INTEGER NOT NULL)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_category_name_source` ON `category` (`name`, `source`)")

                db.execSQL("CREATE TABLE IF NOT EXISTS `item_agent` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `remote_id` INTEGER, `document_id` TEXT, `source` INTEGER NOT NULL DEFAULT 0, `title` TEXT NOT NULL, `description` TEXT, `header` TEXT, `body` TEXT, `prompt` TEXT, `updated_at` INTEGER NOT NULL)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_item_agent_source_document_id` ON `item_agent` (`source`, `document_id`)")

                db.execSQL("CREATE TABLE IF NOT EXISTS `item_prompt` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `remote_id` INTEGER, `document_id` TEXT, `source` INTEGER NOT NULL DEFAULT 0, `title` TEXT NOT NULL, `description` TEXT, `prompt` TEXT, `placeholder` TEXT, `templates` TEXT, `updated_at` INTEGER NOT NULL)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_item_prompt_source_document_id` ON `item_prompt` (`source`, `document_id`)")

                db.execSQL("CREATE TABLE IF NOT EXISTS `item_data` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `remote_id` INTEGER, `document_id` TEXT, `source` INTEGER NOT NULL DEFAULT 0, `title` TEXT NOT NULL, `kind` TEXT NOT NULL, `data` TEXT, `url` TEXT, `extra` TEXT, `size_bytes` INTEGER NOT NULL DEFAULT 0, `created_at` INTEGER NOT NULL, `updated_at` INTEGER NOT NULL)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_item_data_source_document_id` ON `item_data` (`source`, `document_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_item_data_kind` ON `item_data` (`kind`)")

                db.execSQL("CREATE TABLE IF NOT EXISTS `item_category` (`item_id` INTEGER NOT NULL, `category_id` INTEGER NOT NULL, `created_at` INTEGER NOT NULL, PRIMARY KEY(`item_id`, `category_id`), FOREIGN KEY(`item_id`) REFERENCES `item`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`category_id`) REFERENCES `category`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT )")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_item_category_category_id` ON `item_category` (`category_id`)")

                db.execSQL("CREATE TABLE IF NOT EXISTS `item_agent_link` (`item_id` INTEGER NOT NULL, `agent_id` INTEGER NOT NULL, `created_at` INTEGER NOT NULL, PRIMARY KEY(`item_id`), FOREIGN KEY(`item_id`) REFERENCES `item`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`agent_id`) REFERENCES `item_agent`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_item_agent_link_agent_id` ON `item_agent_link` (`agent_id`)")

                db.execSQL("CREATE TABLE IF NOT EXISTS `item_prompt_link` (`item_id` INTEGER NOT NULL, `prompt_id` INTEGER NOT NULL, `created_at` INTEGER NOT NULL, PRIMARY KEY(`item_id`), FOREIGN KEY(`item_id`) REFERENCES `item`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`prompt_id`) REFERENCES `item_prompt`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_item_prompt_link_prompt_id` ON `item_prompt_link` (`prompt_id`)")

                db.execSQL("CREATE TABLE IF NOT EXISTS `item_data_link` (`item_id` INTEGER NOT NULL, `data_id` INTEGER NOT NULL, `created_at` INTEGER NOT NULL, PRIMARY KEY(`item_id`), FOREIGN KEY(`item_id`) REFERENCES `item`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`data_id`) REFERENCES `item_data`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_item_data_link_data_id` ON `item_data_link` (`data_id`)")
            }
        }

        fun loadRawPrompt(context: Context, resId: Int): String =
            context.resources.openRawResource(resId).bufferedReader(Charsets.UTF_8).use { it.readText() }.trim()

        val SYSTEM_PROMPT_BAZI: String by lazy {
            "你是一位精通中国传统命理学的易经师，专注于八字命盘解析。请根据用户提供的四柱八字信息给出详细解析。"
        }

        const val DB_NAME_PREFIX: String = "one_box"

        // 语言切换后进程会冷重启（见 LocaleSwitchWatcher），Hilt @Singleton 注入的库实例
        // 因此总能对应当前语言；getInstanceOrCreate 内的懒切换是直连调用方
        // （如 FileTransferServer）在重启完成前的兜底。
        @Volatile
        private var INSTANCE: AppDatabase? = null

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

        fun getInstanceOrCreate(@ApplicationContext context: Context): AppDatabase {
            val currentLocale = LocaleUtils.getCurrentLocaleTag()
            val currentDbName = dbNameForLocale(currentLocale)

            val existing = INSTANCE
            if (existing != null && INSTANCE_LOCALE == currentLocale) {
                return existing
            }

            return synchronized(this) {
                // 双重检查
                if (INSTANCE != null && INSTANCE_LOCALE == currentLocale) {
                    return@synchronized INSTANCE!!
                }

                // 语言切换了，关闭旧实例
                closeInstance()

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    currentDbName
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration(true)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    // 系统会自动根据当前语言选择对应的 raw 资源
                                    context.resources.openRawResource(R.raw.initial_data)
                                        .use { inputStream ->
                                            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                                                var line: String?
                                                db.beginTransaction()
                                                try {
                                                    while (reader.readLine()
                                                            .also { line = it } != null
                                                    ) {
                                                        line?.let { rawLine ->
                                                            val trimmed = rawLine.trim()
                                                            if (trimmed.isNotBlank() && !trimmed.startsWith("--")) {
                                                                val sql = if (trimmed.uppercase()
                                                                        .startsWith("INSERT INTO ")
                                                                ) {
                                                                    "INSERT OR IGNORE INTO " + trimmed.substring(12)
                                                                } else {
                                                                    trimmed
                                                                }
                                                                try {
                                                                    db.execSQL(sql)
                                                                } catch (e: Exception) {
                                                                    Log.e(
                                                                        "AppDatabase",
                                                                        "Error executing SQL: $sql",
                                                                        e
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                    db.setTransactionSuccessful()
                                                } finally {
                                                    db.endTransaction()
                                                }
                                            }
                                        }
                                } catch (e: Exception) {
                                    if (db.isOpen) {
                                        db.close()
                                    }
                                    Log.e("AppDatabase", "Error reading initial SQL", e)
                                }
                            }
                        }

                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    val dbVersion = SYSTEM_PRESET_VERSION.toString()
                                    val lastVersion = AppSharedStorage.loadSystemPresetVersion()
                                    if (lastVersion != dbVersion) {
                                        ensureSystemPresets(db, context)
                                        AppSharedStorage.saveSystemPresetVersion(dbVersion)
                                    }
                                } catch (e: Exception) {
                                    Log.e("AppDatabase", "Error ensuring system presets", e)
                                }
                            }
                        }

                        private fun ensureSystemPresets(db: SupportSQLiteDatabase, ctx: Context) {
                            val now = System.currentTimeMillis()

                            upsertSystemPreset(
                                db = db,
                                now = now,
                                systemKey = PromptEntity.SYSTEM_PROMPT_KEY_DEFAULT_PROMPT,
                                displayTitle = ctx.getString(R.string.sys_prompt_default_title),
                                description = ctx.getString(R.string.sys_prompt_default_desc),
                                promptText = loadRawPrompt(ctx, R.raw.prompt_system_default)
                            )
                            upsertSystemPreset(
                                db = db,
                                now = now,
                                systemKey = PromptEntity.SYSTEM_PROMPT_KEY_AGENT_CREATE,
                                displayTitle = ctx.getString(R.string.sys_prompt_agent_create_title),
                                description = ctx.getString(R.string.sys_prompt_agent_create_desc),
                                promptText = loadRawPrompt(ctx, R.raw.prompt_agent_create)
                            )
                            upsertSystemPreset(
                                db = db,
                                now = now,
                                systemKey = PromptEntity.SYSTEM_PROMPT_KEY_CHAT_PROMPT_CREATE,
                                displayTitle = ctx.getString(R.string.sys_prompt_prompt_create_title),
                                description = ctx.getString(R.string.sys_prompt_prompt_create_desc),
                                promptText = loadRawPrompt(ctx, R.raw.prompt_prompt_create)
                            )
                            upsertSystemPreset(
                                db = db,
                                now = now,
                                systemKey = PromptEntity.SYSTEM_PROMPT_KEY_BAZI,
                                displayTitle = ctx.getString(R.string.sys_prompt_bazi_title),
                                description = ctx.getString(R.string.sys_prompt_bazi_desc),
                                promptText = loadRawPrompt(ctx, R.raw.prompt_bazi)
                            )
                            upsertSystemPreset(
                                db = db,
                                now = now,
                                systemKey = PromptEntity.SYSTEM_PROMPT_KEY_XIANGQI_MOVE,
                                displayTitle = ctx.getString(R.string.sys_prompt_xiangqi_title),
                                description = ctx.getString(R.string.sys_prompt_xiangqi_desc),
                                promptText = loadRawPrompt(ctx, R.raw.prompt_xiangqi_move)
                            )
                            upsertSystemPreset(
                                db = db,
                                now = now,
                                systemKey = PromptEntity.SYSTEM_PROMPT_KEY_ICHING_INTERPRETATION,
                                displayTitle = ctx.getString(R.string.sys_prompt_iching_title),
                                description = ctx.getString(R.string.sys_prompt_iching_desc),
                                promptText = loadRawPrompt(ctx, R.raw.prompt_iching_interpretation)
                            )
                            upsertSystemPreset(
                                db = db,
                                now = now,
                                systemKey = PromptEntity.SYSTEM_PROMPT_KEY_WORKING_MODE_ASK,
                                displayTitle = ctx.getString(R.string.sys_prompt_mode_ask_title),
                                description = ctx.getString(R.string.sys_prompt_mode_ask_desc),
                                promptText = loadRawPrompt(ctx, R.raw.chat_working_mode_ask)
                            )
                            upsertSystemPreset(
                                db = db,
                                now = now,
                                systemKey = PromptEntity.SYSTEM_PROMPT_KEY_WORKING_MODE_PLAN,
                                displayTitle = ctx.getString(R.string.sys_prompt_mode_plan_title),
                                description = ctx.getString(R.string.sys_prompt_mode_plan_desc),
                                promptText = loadRawPrompt(ctx, R.raw.chat_working_mode_plan)
                            )
                            upsertSystemPreset(
                                db = db,
                                now = now,
                                systemKey = PromptEntity.SYSTEM_PROMPT_KEY_WORKING_MODE_AGENT,
                                displayTitle = ctx.getString(R.string.sys_prompt_mode_agent_title),
                                description = ctx.getString(R.string.sys_prompt_mode_agent_desc),
                                promptText = loadRawPrompt(ctx, R.raw.chat_working_mode_agent)
                            )
                            upsertSystemPreset(
                                db = db,
                                now = now,
                                systemKey = PromptEntity.SYSTEM_PROMPT_KEY_DUEL_TEMPLATES,
                                displayTitle = ctx.getString(R.string.sys_prompt_duel_title),
                                description = ctx.getString(R.string.sys_prompt_duel_desc),
                                promptText = loadRawPrompt(ctx, R.raw.prompt_duel_templates)
                            )
                        }

                        private fun upsertSystemPreset(
                            db: SupportSQLiteDatabase,
                            now: Long,
                            systemKey: String,
                            displayTitle: String,
                            description: String,
                            promptText: String
                        ) {
                            val exists = db.query(
                                """
                                SELECT COUNT(*) FROM item_prompt
                                WHERE source = ? AND placeholder = ?
                                """.trimIndent(),
                                arrayOf(Source.SYSTEM.value.toString(), systemKey)
                            ).use { cursor ->
                                cursor.moveToFirst()
                                cursor.getInt(0) > 0
                            }
                            if (exists) {
                                db.execSQL(
                                    """
                                    UPDATE item_prompt
                                    SET title = ?, description = ?, prompt = ?, updated_at = ?
                                    WHERE source = ? AND placeholder = ?
                                    """.trimIndent(),
                                    arrayOf<Any>(
                                        displayTitle,
                                        description,
                                        promptText,
                                        now,
                                        Source.SYSTEM.value,
                                        systemKey
                                    )
                                )
                            } else {
                                db.execSQL(
                                    "INSERT INTO item_prompt (title, description, prompt, placeholder, templates, updated_at, source) VALUES (?, ?, ?, ?, NULL, ?, ?)",
                                    arrayOf<Any>(
                                        displayTitle,
                                        description,
                                        promptText,
                                        systemKey,
                                        now,
                                        Source.SYSTEM.value
                                    )
                                )
                            }
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
