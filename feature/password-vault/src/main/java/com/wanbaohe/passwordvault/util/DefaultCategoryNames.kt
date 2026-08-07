package com.wanbaohe.passwordvault.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.wanbaohe.passwordvault.R
import com.wanbaohe.passwordvault.service.PasswordVaultServiceImpl

/**
 * 内置分类展示名本地化。
 *
 * 内置分类的名称在播种时以中文写入数据库（见 [PasswordVaultServiceImpl.ensureDefaultCategories]），
 * 且内置分类不可改名/删除，因此展示层统一按分类 id 映射到多语言字符串资源；
 * 用户自建分类不在映射表内，直接回退展示数据库中的名称。
 */
@Composable
fun localizedCategoryName(categoryId: String, fallbackName: String): String = when (categoryId) {
    PasswordVaultServiceImpl.DEFAULT_CATEGORY_LOGIN ->
        stringResource(R.string.password_vault_category_default_login)

    PasswordVaultServiceImpl.DEFAULT_CATEGORY_BANK ->
        stringResource(R.string.password_vault_category_default_bank)

    PasswordVaultServiceImpl.DEFAULT_CATEGORY_EMAIL ->
        stringResource(R.string.password_vault_category_default_email)

    PasswordVaultServiceImpl.DEFAULT_CATEGORY_WIFI ->
        stringResource(R.string.password_vault_category_default_wifi)

    PasswordVaultServiceImpl.DEFAULT_CATEGORY_OTHER ->
        stringResource(R.string.password_vault_category_default_other)

    else -> fallbackName
}
