package com.shifenmiao.model.item

import androidx.compose.runtime.Immutable
import com.shifenmiao.model.Category


@Immutable
data class ItemDataUiState(
    val itemId: Int = 0,
    val title: String = "",
    val description: String = "",
    val data: String = "",
    val iconName: String = "",
    val errorMessage: String? = null,
    val listType: Int = 0,
    val allCategories: List<Category> = emptyList(),
    val selectedCategories: Set<Category> = emptySet(),
    val isSaving: Boolean = false,
    val isDirty: Boolean = false,
    val isLoading: Boolean = true,
    val isEditing: Boolean = false,
    /** 笔记已加锁且当前未通过授权码校验:正文不加载,页面应展示锁定占位 */
    val isLocked: Boolean = false
)
