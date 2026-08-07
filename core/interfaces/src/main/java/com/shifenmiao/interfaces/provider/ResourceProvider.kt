package com.shifenmiao.interfaces.provider

/**
 * 实现接口
 * // 在主应用模块中实现
 * class ResourceProviderImpl(private val context: Context) : ResourceProvider {
 *     override fun getString(resId: Int): String {
 *         return context.getString(resId)
 *     }
 * }
 * 传递接口实现并使用  假设你有一个需要使用资源字符串的类，你可以这样做
 * class SomeClass(private val resourceProvider: ResourceProvider) {
 *     fun doSomething() {
 *         val someString = resourceProvider.getString(R.string.some_string)
 *         // 使用获取到的字符串
 *     }
 * }
 */
// 在公共模块中定义
interface ResourceProvider {
    fun getString(resId: Int): String
}