package com.wanbaohe.cloud.storage.model

sealed interface CloudBrowserState {
    data object Idle : CloudBrowserState

    data object Loading : CloudBrowserState

    data class Success(
        val items: List<CloudObjectItem>,
        val currentPrefix: String,
        val breadcrumbs: List<String>,
    ) : CloudBrowserState

    data class Empty(
        val currentPrefix: String,
    ) : CloudBrowserState

    data class Error(
        val message: String,
        val throwable: Throwable? = null,
    ) : CloudBrowserState
}
