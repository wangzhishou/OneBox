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

package com.shifenmiao.database.authcode.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 全局授权码持久化实体。
 *
 * 全局仅保留一条记录 ([SINGLETON_ID]),[codeHash] 与 [salt] 配合做 SHA-256 校验:
 * - [codeHash] = SHA-256(salt + code)
 * - [salt] 为首次设置时随机生成,防止明文 / 哈希撞库
 *
 * 表为单行 (SINGLETON_ID),修改授权码时直接 REPLACE 即可。
 */
@Entity(
    tableName = "auth_code",
)
data class AuthCodeEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String = SINGLETON_ID,
    @ColumnInfo(name = "code_hash") val codeHash: String,
    @ColumnInfo(name = "salt") val salt: String,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
) {
    companion object {
        const val SINGLETON_ID: String = "auth_code_singleton"
    }
}
