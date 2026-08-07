package com.wanbaohe.a2ui.catalog.builtin.input

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.shifenmiao.model.ui.picker.SelectedCountryData

private val PLACE_FIELDS = listOf(
    Place.Field.ID,
    Place.Field.DISPLAY_NAME,
    Place.Field.FORMATTED_ADDRESS,
    Place.Field.ADDRESS_COMPONENTS,
    Place.Field.LOCATION,
)

// Autocomplete 组件在 Places SDK 5.x 标记废弃,替代 API PlaceAutocomplete 只回 prediction
// 还需二次请求详情,待其提供完整 Place 返回后再迁移;当前版本功能不受影响
@Suppress("DEPRECATION")
private class GoogleLocationPickerState(
    private val appContext: Context,
) : PlatformLocationPickerState {

    var launcher: ManagedActivityResultLauncher<Intent, ActivityResult>? = null

    private var pendingOnChange: ((SelectedCountryData) -> Unit)? = null
    private var pendingOnCancel: (() -> Unit)? = null

    override fun show(
        title: String?,
        initData: SelectedCountryData?,
        initLayer: Int,
        onCancel: () -> Unit,
        onChange: (SelectedCountryData) -> Unit,
    ) {
        val apiKey = resolveApiKey(appContext)
        val activityLauncher = launcher
        if (apiKey == null || activityLauncher == null) {
            onCancel()
            return
        }
        if (!Places.isInitialized()) {
            Places.initializeWithNewPlacesApiEnabled(appContext, apiKey)
        }
        pendingOnChange = onChange
        pendingOnCancel = onCancel
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, PLACE_FIELDS)
            .setHint(title.orEmpty())
            .setInitialQuery(
                listOfNotNull(initData?.province, initData?.city, initData?.district)
                    .joinToString(" "),
            )
            .build(appContext)
        activityLauncher.launch(intent)
    }

    fun onActivityResult(result: ActivityResult) {
        val onChange = pendingOnChange
        val onCancel = pendingOnCancel
        pendingOnChange = null
        pendingOnCancel = null
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            onChange?.invoke(Autocomplete.getPlaceFromIntent(result.data!!).toSelectedCountryData())
        } else {
            onCancel?.invoke()
        }
    }

    override fun hide() {
        pendingOnChange = null
        pendingOnCancel = null
    }
}

@Composable
fun rememberPlatformLocationPickerState(): PlatformLocationPickerState {
    val context = LocalContext.current
    val state = remember { GoogleLocationPickerState(context.applicationContext) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        state.onActivityResult(result)
    }
    state.launcher = launcher
    return state
}

@Suppress("DEPRECATION")
private fun resolveApiKey(context: Context): String? {
    val appInfo = context.packageManager
        .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
    return appInfo.metaData
        ?.getString("com.google.android.geo.API_KEY")
        ?.takeIf { it.isNotBlank() }
}

private fun Place.toSelectedCountryData(): SelectedCountryData {
    val components = addressComponents?.asList().orEmpty()

    fun find(vararg types: String): String? =
        components.firstOrNull { component -> types.any { it in component.types } }?.name

    val province = find("administrative_area_level_1")
    val city = find("locality", "administrative_area_level_2")
    val district = find("sublocality_level_1", "sublocality", "neighborhood")

    // 海外地址层级不固定,拿不到任何层级时兜底用地点名/完整地址
    return if (province == null && city == null && district == null) {
        SelectedCountryData(province = displayName ?: formattedAddress)
    } else {
        SelectedCountryData(province = province, city = city, district = district)
    }
}
