package com.wanbaohe.boardgame.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 玩家条一侧的数据。棋种无关:颜色指示、是否行棋方、点击回调都由调用方决定。
 *
 * @param name 展示名(人类玩家昵称 / AI 服务名)
 * @param subtitle 副标题(如「红方 · deepseek-chat」「黑方」)
 * @param avatarUrl 头像 URL,空串则显示首字母占位
 * @param indicatorColor 棋子颜色指示小圆点
 * @param isActiveTurn 是否当前行棋方(「轮到」角标 + 高亮描边)
 * @param onClick 点击回调(如 AI 方弹出模型选择),null 不可点
 */
data class PlayerBarData(
    val name: String,
    val subtitle: String,
    val avatarUrl: String = "",
    val indicatorColor: Color,
    val isActiveTurn: Boolean = false,
    val onClick: (() -> Unit)? = null,
)

/** 操作栏动作项(悔棋/重做/分析/分享/重开/认输/重命名/全屏等) */
data class BoardGameAction(
    val icon: ImageVector,
    val contentDescription: String? = null,
    val enabled: Boolean = true,
    val onClick: () -> Unit,
)

/** 模块内底部导航 tab 项 */
data class BoardGameTab(
    val id: String,
    val label: String,
    val icon: ImageVector,
)

/** AI 来源标签(如「需登录」「扣积分」「免费」),文案与颜色由调用方给 */
data class AiSourceTag(
    val text: String,
    val color: Color,
)

/**
 * AI 来源选择项。
 * 棋种无关:象棋的「工作模型/Pikafish」、五子棋的「工作模型」都只列 title/subtitle/tags。
 */
data class AiPickerItem(
    val title: String,
    val subtitle: String = "",
    val tags: List<AiSourceTag> = emptyList(),
    val trailingIcon: ImageVector? = null,
)

/** 历史卡片一方的玩家:圆形字标 + 名字 */
data class GameCardPlayer(
    val badge: String,
    val name: String,
    val badgeColor: Color,
    val badgeBackground: Color,
    val badgeBorder: Color,
)

/**
 * 历史对局卡片数据。状态/结果等文案一律由调用方本地化后传入,kit 不引 R.string。
 *
 * @param actionLabel 右下角入口文字(已结束的局是「复盘」,进行中的局是「查看回放」)
 */
data class GameCardData(
    val title: String,
    val statusText: String,
    val modeBadge: String,
    val updatedAt: Long,
    val firstPlayer: GameCardPlayer,
    val secondPlayer: GameCardPlayer,
    val vsLabel: String,
    val plyCountText: String,
    val resultText: String = "",
    val actionLabel: String,
)

/** 历史卡片内弹窗(重命名/删除确认)所需文案 */
data class GameCardLabels(
    val renameTitle: String,
    val renameHint: String,
    val deleteConfirmTitle: String,
    val deleteConfirmMessage: String,
    val confirm: String,
    val cancel: String,
    val renameContentDescription: String,
    val deleteContentDescription: String,
)
