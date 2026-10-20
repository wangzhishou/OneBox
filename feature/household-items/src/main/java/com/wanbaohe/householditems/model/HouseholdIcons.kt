package com.wanbaohe.householditems.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.wanbaohe.base.ui.icon.IconRegistry
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.Spray
import com.t8rin.imagetoolbox.core.resources.icons.line.LineBook
import com.t8rin.imagetoolbox.core.resources.icons.line.LineBorderColor
import com.t8rin.imagetoolbox.core.resources.icons.line.LineBuild
import com.t8rin.imagetoolbox.core.resources.icons.line.LineBusiness
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCatBaby
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCatBeauty
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCatBulky
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCatClothing
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCatMedicine
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCatSmallStuff
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCoffee
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDevices
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDiamond
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDrawerCabinet
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDumbbell
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFolderCustom
import com.t8rin.imagetoolbox.core.resources.icons.line.LineGroup
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLocBalcony
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLocBathroom
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLocHome
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLocKitchen
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLocLivingRoom
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLocMasterBedroom
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLocSecondBedroom
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLocStorageRoom
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLocStudy
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMore
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePets
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRestaurant
import com.t8rin.imagetoolbox.core.resources.icons.line.LineWorkspaces
import com.wanbaohe.householditems.R

/**
 * 位置图标映射:9 个预置 id → 专用图标,用户自建位置 → [LineFolderCustom]。
 */
fun locationIcon(locationId: String): ImageVector = when (locationId) {
    "loc_home" -> Icons.Outlined.LineLocHome
    "loc_living_room" -> Icons.Outlined.LineLocLivingRoom
    "loc_master_bedroom" -> Icons.Outlined.LineLocMasterBedroom
    "loc_second_bedroom" -> Icons.Outlined.LineLocSecondBedroom
    "loc_kitchen" -> Icons.Outlined.LineLocKitchen
    "loc_bathroom" -> Icons.Outlined.LineLocBathroom
    "loc_study" -> Icons.Outlined.LineLocStudy
    "loc_balcony" -> Icons.Outlined.LineLocBalcony
    "loc_storage_room" -> Icons.Outlined.LineLocStorageRoom
    "loc_company" -> Icons.Outlined.LineBusiness
    "loc_desk" -> Icons.Outlined.LineWorkspaces
    "loc_meeting_room" -> Icons.Outlined.LineGroup
    "loc_pantry" -> Icons.Outlined.LineCoffee
    "loc_locker" -> Icons.Outlined.LineDrawerCabinet
    else -> Icons.Outlined.LineFolderCustom
}

/**
 * 位置图标解析:用户自选的 [iconKey](IconRegistry key)优先;
 * 为空或注册表查不到时回退预置 id 映射 / 默认文件夹图标。
 */
fun locationIcon(iconKey: String?, locationId: String): ImageVector =
    iconKey?.let(IconRegistry::resolve) ?: locationIcon(locationId)

/** 预置物品分类:字符串资源 + 图标;分类存库的是本地化文本 */
data class HouseholdCategoryDef(
    @StringRes val nameResId: Int,
    val icon: ImageVector,
)

/** 16 个默认分类(编辑页宫格 + 图标反查共用),自定义分类不在其中 */
val defaultHouseholdCategories: List<HouseholdCategoryDef> = listOf(
    HouseholdCategoryDef(R.string.household_category_medicine, Icons.Outlined.LineCatMedicine),
    HouseholdCategoryDef(R.string.household_category_beauty, Icons.Outlined.LineCatBeauty),
    HouseholdCategoryDef(R.string.household_category_daily, Icons.Outlined.LineCatSmallStuff),
    HouseholdCategoryDef(R.string.household_category_food, Icons.Outlined.LineRestaurant),
    HouseholdCategoryDef(R.string.household_category_clothing, Icons.Outlined.LineCatClothing),
    HouseholdCategoryDef(R.string.household_category_digital, Icons.Outlined.LineDevices),
    HouseholdCategoryDef(R.string.household_category_books, Icons.Outlined.LineBook),
    HouseholdCategoryDef(R.string.household_category_stationery, Icons.Outlined.LineBorderColor),
    HouseholdCategoryDef(R.string.household_category_home_goods, Icons.Outlined.LineDrawerCabinet),
    HouseholdCategoryDef(R.string.household_category_cleaning, Icons.Outlined.Spray),
    HouseholdCategoryDef(R.string.household_category_baby, Icons.Outlined.LineCatBaby),
    HouseholdCategoryDef(R.string.household_category_sports, Icons.Outlined.LineDumbbell),
    HouseholdCategoryDef(R.string.household_category_pet, Icons.Outlined.LinePets),
    HouseholdCategoryDef(R.string.household_category_tools, Icons.Outlined.LineBuild),
    HouseholdCategoryDef(R.string.household_category_accessories, Icons.Outlined.LineDiamond),
    HouseholdCategoryDef(R.string.household_category_other, Icons.Outlined.LineMore),
)

/**
 * 物品分类图标:分类存的是本地化文本,与当前 locale 的 16 个预设值比较;
 * 兼容旧版"小东西/大件"两个预设;匹配不上返回 null(调用方回退首字占位)。
 */
@Composable
fun categoryIconOrNull(category: String): ImageVector? {
    if (category.isBlank()) return null
    val names = defaultHouseholdCategories.map { stringResource(it.nameResId) }
    defaultHouseholdCategories.forEachIndexed { index, def ->
        if (category == names[index]) return def.icon
    }
    // 旧版预设(已下线,兼容存量数据)
    val legacySmall = stringResource(R.string.household_category_small_legacy)
    val legacyBig = stringResource(R.string.household_category_big_legacy)
    return when (category) {
        legacySmall -> Icons.Outlined.LineCatSmallStuff
        legacyBig -> Icons.Outlined.LineCatBulky
        else -> null
    }
}
