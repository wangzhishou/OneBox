package com.shifenmiao.common.di

import android.app.Application
import android.content.Context
import com.t8rin.opencv_tools.utils.OpenCV
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OpenCVInitializer @Inject constructor(
    @ApplicationContext context: Context
) {
    init {
        OpenCV.init(context as Application)
    }
}
