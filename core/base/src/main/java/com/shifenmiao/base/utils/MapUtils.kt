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

package com.shifenmiao.base.utils

/**
 * {
 *   sort: ['title:asc'],
 *   filters: {
 *     title: {
 *       $eq: 'hello',
 *     },
 *   },
 *   populate: {
 *     author: {
 *       fields: ['firstName', 'lastName']
 *     }
 *   },
 *   fields: ['title'],
 *   pagination: {
 *     pageSize: 10,
 *     page: 1,
 *   },
 *   publicationState: 'live',
 *   locale: ['en'],
 * }
 * 这种map数据，会转化成
 * /api/books?sort[0]=title:asc&filters[title][\$eq]=hello&populate[author][fields][0]=firstName&populate[author][fields][1]=lastName&fields[0]=title&pagination[pageSize]=10&pagination[page]=1&publicationState=live&locale[0]=en
 */

object MapUtils {
    fun flattenParams(params: Map<String, Any>): Map<String, String> {
        val flatParams = mutableMapOf<String, String>()
        flatten(params, null, flatParams)
        return flatParams
    }

    private fun flatten(params: Map<String, Any?>, prefix: String?, flatParams: MutableMap<String, String>) {
        for ((key, value) in params) {
            val fullPath = if (prefix != null) "$prefix[$key]" else key

            when (value) {
                // Recursively flatten nested maps
                is Map<*, *> -> {
                    @Suppress("UNCHECKED_CAST")
                    flatten(value as Map<String, Any?>, fullPath, flatParams)
                }
                is List<*> -> {
                    value.forEachIndexed { index, item ->
                        if (item != null) {
                            flatten(mapOf("value" to item), "$fullPath[$index]", flatParams)
                        } else {
                            // Handle null values if necessary, for example, by adding a placeholder
                            flatParams["$fullPath[$index]"] = "null"
                        }
                    }
                }
                else -> {
                    // If the value is non-null, convert it to string. Otherwise, use a placeholder.
                    flatParams[fullPath] = value?.toString() ?: "null"
                }
            }
        }
    }
}