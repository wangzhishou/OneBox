package com.shifenmiao.base.utils

object ListUtils {
    fun <T> elementExistsInList(element: T, list: List<T>): Boolean {
        for (item in list) {
            if (item == element) {
                return true
            }
        }
        return false
    }

}