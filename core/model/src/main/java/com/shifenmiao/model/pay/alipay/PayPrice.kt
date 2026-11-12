/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.shifenmiao.model.pay.alipay

import android.os.Parcelable
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import com.shifenmiao.core.R
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMovie
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCoffee
import com.t8rin.imagetoolbox.core.resources.icons.line.LineEmojiFood
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLocalCafe
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLocalDrink
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMeditation
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDiamond
import com.t8rin.imagetoolbox.core.resources.icons.line.LineEmojiPeople
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFlight
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLocalPharmacy
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePedalBike
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSavings
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSportsBar

@Parcelize
sealed class PayPrice(
    val price: Float = 0.1f,
    @StringRes val name: Int = R.string.pay_wu_mao,
    val icon: @RawValue ImageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCoffee,
    @StringRes val desc: Int = R.string.pay_desc,
    val containerColor: @RawValue Color = Color.Unspecified,
    val contentColor: @RawValue Color = Color.Unspecified,
    var userId: Int = 0,
) : Parcelable {
    data object WuMaoPrice : PayPrice(
        price = 0.5f,
        name = R.string.pay_wu_mao,
        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSavings,
        desc = R.string.pay_desc,
    )

    data object OnePrice : PayPrice(
        price = 1.0f,
        name = R.string.pay_plus_one,
        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineEmojiPeople,
        desc = R.string.pay_desc,
    )

    data object ColaPrice : PayPrice(
        price = 3.0f,
        name = R.string.pay_cola,
        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineLocalDrink,
        desc = R.string.pay_desc,
    )

    data object TeaPrice : PayPrice(
        price = 5.0f,
        name = R.string.pay_tea,
        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineEmojiFood,
        desc = R.string.pay_desc,
    )

    data object JuicePrice : PayPrice(
        price = 6.0f,
        name = R.string.pay_juice,
        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineLocalDrink,
        desc = R.string.pay_desc,
    )

    data object CoffeePrice : PayPrice(
        price = 9.9f,
        name = R.string.pay_coffee,
        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineLocalCafe,
        desc = R.string.pay_desc,
        containerColor = Color(0xFFE7AD70),
        contentColor = Color.White
    )

    data object BeerPrice : PayPrice(
        price = 10.0f,
        name = R.string.pay_beer,
        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSportsBar,
        desc = R.string.pay_desc,
        containerColor = Color(0xFFF7BF4B),
        contentColor = Color.Black
    )

    //电影价格
    data object MoviePrice : PayPrice(
        price = 50f,
        name = R.string.pay_movie,
        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMovie,
        desc = R.string.pay_desc,
    )

    //  一箱牛奶
    data object MilkPrice : PayPrice(
        price = 98f,
        name = R.string.pay_milk,
        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineLocalCafe,
        desc = R.string.pay_desc,
    )

    //药品价格
    data object MedicinePrice : PayPrice(
        price = 198f,
        name = R.string.pay_medicine,
        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineLocalPharmacy,
        desc = R.string.pay_desc,
    )

    data object SaunaPrice : PayPrice(
        price = 298f,
        name = R.string.pay_sauna,
        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMeditation,
        desc = R.string.pay_desc,
    )

    //自行车价格
    data object BikePrice : PayPrice(
        price = 500f,
        name = R.string.pay_bike,
        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePedalBike,
        desc = R.string.pay_desc,
    )

    //旅行价格
    data object TravelPrice : PayPrice(
        price = 1000f,
        name = R.string.pay_travel,
        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFlight,
        desc = R.string.pay_desc,
    )

    //红包价格
    data object RedPacketPrice : PayPrice(
        price = 10000f,
        name = R.string.pay_red_packet,
        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDiamond,
        desc = R.string.pay_desc,
        containerColor = Color(0xFFD4AF37),
        contentColor = Color.White
    )

    companion object {
        val priceEntries by lazy {
            listOf(
                WuMaoPrice,
                OnePrice,
                ColaPrice,
                TeaPrice,
                JuicePrice,
                CoffeePrice,
                BeerPrice,
                MoviePrice,
                MilkPrice,
                MedicinePrice,
                SaunaPrice,
                BikePrice,
                TravelPrice,
                RedPacketPrice
            )
        }
        const val START_INDEX = 0
    }
}