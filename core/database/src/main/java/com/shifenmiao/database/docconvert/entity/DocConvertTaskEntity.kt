package com.shifenmiao.database.docconvert.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "doc_convert_tasks")
data class DocConvertTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: String,
    val fileName: String,
    val fileSizeBytes: Long = 0,
    val sourcePath: String? = null,
    val status: String,
    val percent: Int? = null,
    val wordUrl: String? = null,
    val excelUrl: String? = null,
    val localWordPath: String? = null,
    val localExcelPath: String? = null,
    val errorMsg: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

