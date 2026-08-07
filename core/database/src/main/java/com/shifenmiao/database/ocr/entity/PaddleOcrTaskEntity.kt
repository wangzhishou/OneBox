package com.shifenmiao.database.ocr.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ocr_tasks")
data class PaddleOcrTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: String,
    val fileName: String,
    val fileSizeBytes: Long = 0,
    val sourcePath: String? = null,
    val status: String, // pending, processing, success, failed
    val markdownUrl: String? = null,
    val parseResultUrl: String? = null,
    val rawDownloadedPath: String? = null,
    val localPath: String? = null,
    val errorMsg: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
