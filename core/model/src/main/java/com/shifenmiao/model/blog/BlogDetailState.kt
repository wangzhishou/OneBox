package com.shifenmiao.model.blog

sealed class BlogDetailState {
    object PageLoading : BlogDetailState()
    object Loading : BlogDetailState()
    data class Error(val message: String? = null) : BlogDetailState()
    object Empty : BlogDetailState()
    data class Success(val blog: BlogItem) : BlogDetailState()
}