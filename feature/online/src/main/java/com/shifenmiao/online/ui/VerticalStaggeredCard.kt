package com.shifenmiao.online.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.pm.ShortcutManagerCompat
import com.shifenmiao.base.ui.card.CardAction
import com.shifenmiao.base.ui.card.GenericTonalCard
import com.shifenmiao.base.ui.card.TonalCardPaletteDefaults
import com.shifenmiao.base.ui.card.TonalCardVisualVariant
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.base.utils.StringUtils
import com.shifenmiao.common.components.OperateBar
import com.shifenmiao.common.handle.ItemScreenResolver
import com.shifenmiao.core.R
import com.shifenmiao.database.item.entity.ItemWithCategoriesAndStats
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.model.ListItemType
import com.shifenmiao.online.component.ItemListComponent
import com.shifenmiao.theme.AppTheme
import com.shifenmiao.base.provider.LocalDataDraftHelper
import com.t8rin.imagetoolbox.core.ui.utils.navigation.LocalOnNavigate
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.utils.helper.ContextUtils.createScreenShortcut
import com.t8rin.imagetoolbox.core.utils.appContext
import kotlinx.coroutines.launch
import com.t8rin.imagetoolbox.core.resources.icons.Delete
import com.t8rin.imagetoolbox.core.resources.icons.Edit
import com.t8rin.imagetoolbox.core.resources.icons.PushPin
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLock
import com.t8rin.imagetoolbox.core.resources.icons.line.LineUnlock
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRemove
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFavorite
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAddToHome
import com.t8rin.imagetoolbox.core.resources.icons.line.LineVerticalAlignTop

@Composable
fun VerticalStaggeredCard(
    itemListComponent: ItemListComponent,
    itemWithStats: ItemWithCategoriesAndStats,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    onDeleteRequest: ((itemId: Int, itemTitle: String) -> Unit)? = null,
    onCategoryClick: ((categoryId: Int?) -> Unit)? = null,
    themeColor: Color? = MaterialTheme.colorScheme.primaryContainer.copy(alpha = AppTheme.dimens.containerAlpha),
    iconContentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    iconContainerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    tagBackgroundColor: Color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = AppTheme.dimens.containerAlpha),
    tagTextColor: Color = MaterialTheme.colorScheme.onTertiaryContainer,
    actionContainerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = AppTheme.dimens.containerAlpha),
    actionContentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    shape: Shape = MaterialTheme.shapes.large,
    index: Int = 0,
    maxTitleLines: Int = 1,
    commentCount: Int? = null,
    onCommentClick: (() -> Unit)? = null,
    // 搜索结果场景传入搜索词, 对标题/描述做高亮; 默认 null 时渲染与列表页完全一致
    highlightKeyword: String? = null,
) {
    val item = itemWithStats.item
    val title = remember(item.title) { item.title }
    val isFavorited = itemWithStats.isFavorited
    val isPinned = itemWithStats.isPinned
    val requiresAuth = itemWithStats.requiresAuth
    val canEdit = itemWithStats.userState?.canEdit == true
    val onNavigator = LocalOnNavigate.current
    val lockedPreviewLabel = stringResource(R.string.item_locked_preview)
    // 加锁的 item 不在列表泄露摘要,用统一占位文案代替
    val description = remember(item.description, requiresAuth, lockedPreviewLabel) {
        if (requiresAuth) lockedPreviewLabel else item.description
    }

    val scope = rememberCoroutineScope()
    val dataDraftHelper = LocalDataDraftHelper.current
    val context = LocalContext.current
    val visualVariant = remember(item.isHighlighted, item.recommend) {
        when {
            item.isHighlighted -> TonalCardVisualVariant.Highlighted
            item.recommend -> TonalCardVisualVariant.Recommend
            else -> TonalCardVisualVariant.Default
        }
    }
    val resolvedPalette = TonalCardPaletteDefaults.palette(
        variant = visualVariant,
        defaultContainerColor = themeColor
            ?: MaterialTheme.colorScheme.primaryContainer.copy(alpha = AppTheme.dimens.containerAlpha),
        defaultIconContentColor = iconContentColor,
        defaultIconContainerColor = iconContainerColor,
        defaultTagBackgroundColor = tagBackgroundColor,
        defaultTagTextColor = tagTextColor,
        defaultActionContainerColor = actionContainerColor,
        defaultActionContentColor = actionContentColor,
    )

    val favoriteLabel = stringResource(R.string.favorite)
    val unpinLabel = stringResource(R.string.unpin)
    val bringToTopLabel = stringResource(R.string.bring_to_top)
    val pinLabel = stringResource(R.string.pin)
    val editLabel = stringResource(R.string.edit)
    val deleteLabel = stringResource(R.string.delete)
    val addToHomeScreenLabel = stringResource(R.string.add_to_home_screen)
    val lockLabel = stringResource(R.string.item_action_lock)
    val unlockLabel = stringResource(R.string.item_action_unlock)

    val cardActions = remember(
        item,
        index,
        title,
        isFavorited,
        isPinned,
        requiresAuth,
        canEdit,
        favoriteLabel,
        unpinLabel,
        bringToTopLabel,
        pinLabel,
        editLabel,
        deleteLabel,
        addToHomeScreenLabel,
        lockLabel,
        unlockLabel,
        itemListComponent,
        dataDraftHelper,
        onNavigator,
        onDeleteRequest,
        scope,
        context,
        resolvedPalette.iconContentColor,
        resolvedPalette.iconContainerColor,
    ) {
        buildList {
            add(
                CardAction(
                    icon = if (isFavorited) com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFavorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = favoriteLabel,
                    onClick = {
                        itemListComponent.setFavorite(item)
                    }
                )
            )

            if (isPinned) {
                add(
                    CardAction(
                        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineRemove,
                        contentDescription = unpinLabel,
                        onClick = {
                            itemListComponent.togglePin(item.id, true)
                        }
                    )
                )

                if (index > 0) {
                    add(
                        CardAction(
                            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineVerticalAlignTop,
                            contentDescription = bringToTopLabel,
                            onClick = {
                                itemListComponent.bringToTop(item.id)
                            }
                        )
                    )
                }
            } else {
                add(
                    CardAction(
                        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.PushPin,
                        contentDescription = pinLabel,
                        onClick = {
                            itemListComponent.togglePin(item.id, false)
                        }
                    )
                )
            }

            add(
                CardAction(
                    // 图标表达点击动作:未加锁显示闭合锁(点我加锁),已加锁显示打开的锁(点我解锁)
                    icon = if (requiresAuth) com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineUnlock else com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineLock,
                    contentDescription = if (requiresAuth) unlockLabel else lockLabel,
                    onClick = {
                        // 锁 / 解锁都需先过授权码校验,避免未授权用户篡改安全标记
                        ActionUtils.showAuthCode(
                            source = "item_lock_toggle_${item.id}",
                            onSuccess = {
                                itemListComponent.toggleRequiresAuth(item.id)
                            },
                        )
                    }
                )
            )

            if (canEdit) {
                add(
                    CardAction(
                        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Edit,
                        contentDescription = editLabel,
                        onClick = {
                            val openEditor: () -> Unit = {
                                scope.launch {
                                    val resolution = ItemScreenResolver.resolveForEdit(
                                        item = item,
                                        dataDraftHelper = dataDraftHelper
                                    )
                                    val targetScreen = resolution.screen
                                    if (targetScreen != null) {
                                        onNavigator(targetScreen)
                                    } else {
                                        AppToastHost.showToast(
                                            resolution.message
                                                ?: AppContext.getString(R.string.cannot_edit)
                                        )
                                    }
                                }
                            }
                            // 加锁 item 进入编辑前同样先过授权码
                            if (requiresAuth) {
                                ActionUtils.showAuthCode(
                                    source = "item_edit_lock_${item.id}",
                                    onSuccess = openEditor,
                                )
                            } else {
                                openEditor()
                            }
                        }
                    )
                )
                add(
                    CardAction(
                        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Delete,
                        contentDescription = deleteLabel,
                        onClick = {
                            // 加锁 item 删除前同样先过授权码,避免被越权删除
                            if (requiresAuth) {
                                ActionUtils.showAuthCode(
                                    source = "item_delete_lock_${item.id}",
                                    onSuccess = { onDeleteRequest?.invoke(item.id, title) },
                                )
                            } else {
                                onDeleteRequest?.invoke(item.id, title)
                            }
                        }
                    )
                )
            }

            if (ListItemType.NORMAL == ListItemType.fromId(item.listType)) {
                add(
                    CardAction(
                        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAddToHome,
                        contentDescription = addToHomeScreenLabel,
                        onClick = {
                            if (ShortcutManagerCompat.isRequestPinShortcutSupported(appContext)) {
                                item.miniProgramId.toIntOrNull()?.let { id ->
                                    val screen = Screen.entries.find { it.id == id }
                                    if (screen != null) {
                                        scope.launch {
                                            context.createScreenShortcut(
                                                screen = screen,
                                                tint = resolvedPalette.iconContentColor,
                                                backgroundColor = resolvedPalette.iconContainerColor
                                            )
                                        }
                                    }
                                    return@CardAction
                                }
                            }
                            scope.launch {
                                AppToastHost.showToast(AppContext.getString(R.string.cannot_create_shortcut))
                            }
                            return@CardAction
                        }
                    )
                )
            }
        }
    }


    val supportingContentColor =
        resolvedPalette.supportingContentColor

    // 仅在传入搜索词时计算高亮(复用搜索卡片的同款高亮配色), 否则保持原样
    val highlightKeywordNonBlank = highlightKeyword?.takeIf { it.isNotBlank() }
    val highlightColor = MaterialTheme.colorScheme.onErrorContainer
    val highlightBackgroundColor = MaterialTheme.colorScheme.errorContainer
    val highlightedTitle = remember(title, highlightKeywordNonBlank, highlightColor, highlightBackgroundColor) {
        highlightKeywordNonBlank?.let {
            StringUtils.getHighlightedDescription(title, it, highlightColor, highlightBackgroundColor)
        }
    }
    val highlightedDescription = remember(description, highlightKeywordNonBlank, highlightColor, highlightBackgroundColor) {
        highlightKeywordNonBlank?.let {
            StringUtils.getHighlightedDescription(description, it, highlightColor, highlightBackgroundColor)
        }
    }

    GenericTonalCard(
        id = item.id,
        supportingContentColor = supportingContentColor,
        themeColor = resolvedPalette.containerColor,
        iconContentColor = resolvedPalette.iconContentColor,
        iconContainerColor = resolvedPalette.iconContainerColor,
        actionContainerColor = resolvedPalette.actionContainerColor,
        actionContentColor = resolvedPalette.actionContentColor,
        title = title,
        description = description,
        // iconName 只放图标注册表 key;为空(null/"")时回退为标题,由 LetterIcon 取当前语言标题首字
        iconName = item.iconName?.takeIf { it.isNotEmpty() } ?: title,
        maxTitleLines = maxTitleLines,
        modifier = modifier,
        shape = shape,
        actions = cardActions,
        palette = resolvedPalette,
        onClick = onClick,
        onLongClick = onLongClick,
        highlightedTitle = highlightedTitle,
        highlightedDescription = highlightedDescription,
        stateBar = { _, _ ->
            OperateBar(
                dataItem = item,
                palette = resolvedPalette,
                tagText = firstCategoryName(itemWithStats),
                onTagClick = onCategoryClick?.let { handler ->
                    { handler(firstCategoryId(itemWithStats)) }
                },
                commentCount = commentCount,
                onCommentClick = onCommentClick,
            )
        }
    )
}

private fun firstCategoryName(item: ItemWithCategoriesAndStats): String? =
    item.categories.firstOrNull()?.name

private fun firstCategoryId(item: ItemWithCategoriesAndStats): Int? =
    item.categories.firstOrNull()?.id
