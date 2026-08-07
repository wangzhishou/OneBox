package com.shifenmiao.model.login

import androidx.annotation.StringRes
import com.shifenmiao.core.R

/**
 * Error state holding values for error ui
 */
data class ErrorState(
    val hasError: Boolean = false,
    @StringRes val errorMessageStringResource: Int = R.string.empty_string
)