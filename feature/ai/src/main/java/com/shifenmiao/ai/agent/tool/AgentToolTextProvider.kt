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
