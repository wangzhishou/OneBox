package com.shifenmiao.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal object Release152Migrations {
    const val VERSION = 152

    val feature: Array<Migration> = arrayOf(
        object : Migration(151, VERSION) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `speed_test_record` ADD COLUMN `uploadMbps` REAL NOT NULL DEFAULT -1")
                db.execSQL("ALTER TABLE `speed_test_record` ADD COLUMN `clientIp` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `speed_test_record` ADD COLUMN `clientLocation` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `speed_test_record` ADD COLUMN `carrier` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `speed_test_record` ADD COLUMN `serverHost` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `speed_test_record` ADD COLUMN `latencyNode` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `speed_test_record` ADD COLUMN `downloadNode` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `speed_test_record` ADD COLUMN `uploadNode` TEXT NOT NULL DEFAULT ''")
            }
        },
    )
}
