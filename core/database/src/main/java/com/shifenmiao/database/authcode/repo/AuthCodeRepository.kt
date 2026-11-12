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

package com.shifenmiao.database.authcode.repo

import com.shifenmiao.database.authcode.dao.AuthCodeDao
import com.shifenmiao.database.authcode.entity.AuthCodeEntity
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 授权码表仓库。
 *
 * - [save] 写入或替换单行记录
 * - [get] / [hasCode] 用于判断是否已设置 / 读取已存的 hash + salt
 * - [delete] 清空授权码(回到未设置状态)
 */
@Singleton
class AuthCodeRepository @Inject constructor(
    private val dao: AuthCodeDao,
) {
    suspend fun save(entity: AuthCodeEntity) = dao.upsert(entity)

    suspend fun get(): AuthCodeEntity? = dao.getById()

    suspend fun hasCode(): Boolean = dao.count() > 0

    suspend fun delete() = dao.deleteAll()
}
