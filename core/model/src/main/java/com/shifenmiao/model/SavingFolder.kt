package com.shifenmiao.model

import android.net.Uri
import java.io.File
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SavingFolder(
    val outputStream: OutputStream? = null,
    val file: File? = null,
    val fileUri: Uri? = null
)