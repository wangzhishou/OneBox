plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.feature)
    alias(libs.plugins.image.toolbox.hilt)
    alias(libs.plugins.image.toolbox.compose)
}

android.namespace = "com.wanbaohe.a2ui"

// 为国内渠道增加 src/domestic sourceSet,与 src/google 形成 flavor 隔离(同 core/r、core/ui 的做法):
//   - 国内渠道 + foss:src/main + src/domestic(平台位置选择走行政区 CityPicker)
//   - Google 渠道:src/main + src/google(平台位置选择走 Google Places Autocomplete)
afterEvaluate {
    android.sourceSets {
        listOf("onebox", "xiaomi", "yyb", "oppo", "vivo", "huawei", "foss").forEach { flavor ->
            getByName(flavor).kotlin.srcDir("src/domestic/java")
        }
    }
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    googleImplementation(libs.places)
}
