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

package com.shifenmiao.ai.agent.tool

import android.content.Context
import androidx.annotation.ArrayRes
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AgentToolTextProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dispatchersHolder: DispatchersHolder
) {
    fun string(@StringRes resId: Int, vararg args: Any): String {
        return context.getString(resId, *args)
    }

    fun array(@ArrayRes resId: Int): List<String> {
        return context.resources.getStringArray(resId).toList()
    }

    /**
     * 同步读取 raw resource（适用于属性初始化器等非 suspend 上下文）。
     * 文件很小（< 10KB），I/O 耗时可忽略。
     */
    fun raw(@RawRes resId: Int): String {
        return context.resources.openRawResource(resId)
            .bufferedReader(Charsets.UTF_8)
            .use { it.readText() }
            .trim()
    }

    /**
     * 异步读取 raw resource（适用于 suspend 上下文）。
     * 通过 Dispatchers.IO 确保不阻塞调用线程。
     */
    suspend fun rawAsync(@RawRes resId: Int): String {
        return withContext(dispatchersHolder.ioDispatcher) {
            context.resources.openRawResource(resId)
                .bufferedReader(Charsets.UTF_8)
                .use { it.readText() }
                .trim()
        }
    }
}
