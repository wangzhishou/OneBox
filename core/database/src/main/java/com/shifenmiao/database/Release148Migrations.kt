package com.shifenmiao.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal object Release148Migrations {
    const val VERSION = 148

    val feature: Array<Migration> = arrayOf(
        object : Migration(147, VERSION) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `altitude_record` ADD COLUMN `weatherIcon` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `altitude_record` ADD COLUMN `weatherUv` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `altitude_record` ADD COLUMN `weatherAqi` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `altitude_record` ADD COLUMN `weatherAqiCategory` TEXT NOT NULL DEFAULT ''")
            }
        },
    )
}
