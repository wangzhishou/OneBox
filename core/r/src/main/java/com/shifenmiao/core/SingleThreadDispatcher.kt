package com.shifenmiao.core


import java.util.concurrent.Executors

object SingleThreadDispatcher {
    // 创建一个单线程的 ExecutorService
    val singlethreadexecutor = Executors.newSingleThreadExecutor()

    // 提交任务到该执行器
    fun submitTask(task: Runnable) {
        singlethreadexecutor.submit(task)
    }

    // 关闭执行器
    fun shutdown() {
        singlethreadexecutor.shutdown()
    }
}