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