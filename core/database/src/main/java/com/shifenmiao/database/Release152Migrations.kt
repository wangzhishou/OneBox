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
