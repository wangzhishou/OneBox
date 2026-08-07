package com.shifenmiao.base.manager

/**
 * 管理删除确认对话框的显示状态
 * ���应用程序本次运行周期内，记住用户的删除确认首选项
 */
object DeleteConfirmationManager {
    // 保存各种删除操作类型的确认状态
    private val confirmationPreferences = mutableMapOf<String, Boolean>()
    
    /**
     * 检查特定类型的删除操作是否需要确认
     * @param operationType 操作类型标识
     * @return 如果需要确认返回true，否则返回false
     */
    fun shouldConfirmDeletion(operationType: String): Boolean {
        return confirmationPreferences[operationType] != false
    }
    
    /**
     * 设置特定类型删除操作的确认状态
     * @param operationType 操作类型标识
     * @param shouldConfirm 是否需要确认
     */
    fun setShouldConfirmDeletion(operationType: String, shouldConfirm: Boolean) {
        confirmationPreferences[operationType] = shouldConfirm
    }
    
    /**
     * 重置所有确认状态
     */
    fun resetAllPreferences() {
        confirmationPreferences.clear()
    }
    
    // 预定义的操作类型常量
    object OperationType {
        const val HISTORY_ITEM = "history_item"
        const val FAVORITE_ITEM = "favorite_item"
        const val THEME_SETTING = "favorite_item"
        const val CHAT_MESSAGE = "chat_message"
    }
}
