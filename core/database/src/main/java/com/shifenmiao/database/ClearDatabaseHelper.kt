package com.shifenmiao.database

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClearDatabaseHelper @Inject constructor(
    private val appDatabase: AppDatabase,
) {
    fun clearAll() {
        CoroutineScope(Dispatchers.IO).launch {
            /**
             * Item相关的删除全在这里，删除需要依赖索引顺序
             */
            appDatabase.messageDao().deleteAll()
            appDatabase.imageDao().clearAll()
            appDatabase.conversationDao().deleteAll()
            appDatabase.activityLogDao().deleteAll()
        }
    }

    fun close() {
        appDatabase.close()
    }
}