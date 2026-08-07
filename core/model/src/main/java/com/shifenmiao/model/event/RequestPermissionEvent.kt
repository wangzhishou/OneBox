package com.shifenmiao.model.event

import android.annotation.SuppressLint
import com.shifenmiao.core.R
import com.shifenmiao.interfaces.singleton.AppContext

data class RequestPermissionEvent(
    val permissions: List<String> = emptyList<String>(),
    val permissionRequest: PermissionRequest = PermissionRequest.ALL,
    val onSuccess: () -> Unit = {},
    val onFailed: () -> Unit = {},
    val onRequest: () -> Unit = {}
)


enum class PermissionRequest(
    val code: Int,
    val title: String,
    val description: String,
    val permissions: Array<String>,
    val requestCode: Int
) {
    ALL(0, "", "Request access to all permissions", arrayOf(), 1000),
    CAMERA(
        1,
        AppContext.getString(
            R.string.privacy_item_camera_title
        ),
        AppContext.getString(R.string.privacy_item_camera_description),
        arrayOf(android.Manifest.permission.CAMERA),
        1001
    ),
    LOCATION(
        2,
        "",
        "Request access to the location",
        arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
        1002
    ),
    STORAGE(
        3,
        AppContext.getString(
            R.string.privacy_item_1
        ),
        AppContext.getString(R.string.privacy_item_2_description) + "\n" + AppContext.getString(
            R.string.privacy_item_1_description
        ),
        arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
        1003
    ),
    CONTACTS(
        4,
        "",
        "Request access to the contacts",
        arrayOf(android.Manifest.permission.READ_CONTACTS),
        1004
    ),
    MICROPHONE(
        5,
        AppContext.getString(
            R.string.privacy_item_4
        ),
        AppContext.getString(
            R.string.privacy_item_4_description
        ),
        arrayOf(android.Manifest.permission.RECORD_AUDIO),
        1005
    ),

    @SuppressLint("InlinedApi")
    APK_INSTALL(
        6,
        "",
        "Request access to install APK",
        arrayOf(android.Manifest.permission.REQUEST_INSTALL_PACKAGES),
        1006
    ),
    NEW_PERMISSION(
        7,
        "",
        "Request access to the new permission",
        arrayOf("your.new.permission.HERE"),
        1007
    ) // New permission added
}