package com.shifenmiao.base.manager

import com.shifenmiao.base.entrypoint.AppEntryPoint
import com.shifenmiao.database.ClearDatabaseHelper
import com.shifenmiao.interfaces.singleton.AppContext
import dagger.hilt.android.EntryPointAccessors

class DataBaseManager private constructor() {

    private var clearDatabaseHelper: ClearDatabaseHelper

    init {
        val entryPoint = EntryPointAccessors.fromApplication(
            context = AppContext.getContext(),
            entryPoint = AppEntryPoint::class.java
        )
        clearDatabaseHelper = entryPoint.getClearDatabaseHelper()
    }

    fun clearAll() {
        clearDatabaseHelper.clearAll()
    }

    companion object {
        val instance: DataBaseManager by lazy { DataBaseManager() }
    }
}