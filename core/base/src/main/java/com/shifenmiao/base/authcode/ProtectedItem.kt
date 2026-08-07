package com.shifenmiao.base.authcode

/**
 * 需要密码保护的应用的轻量级 UI 模型。
 *
 * 与 Room 的 [com.shifenmiao.database.item.entity.ItemEntity] 解耦,UI 层不直接持有
 * 数据库实体,符合"高层只依赖抽象"的设计约定。
 */
data class ProtectedItem(
    val id: Int,
    val title: String,
    val iconName: String?,
)
