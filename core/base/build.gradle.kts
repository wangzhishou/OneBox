plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.hilt)
    alias(libs.plugins.image.toolbox.compose)
}

android.namespace = "com.shifenmiao.base"

dependencies {
    implementation(libs.reimagined)
    implementation(libs.reimagined.hilt)

    implementation(libs.androidx.documentfile)

    //AndroidX
    implementation(libs.activityCompose)
    implementation(libs.splashScreen)
    implementation(libs.androidx.exifinterface)
    implementation(libs.appCompat)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    //Konfetti
    implementation(libs.konfetti.compose)

    //Coil
    implementation(libs.coil)
    implementation(libs.coil.compose)
    implementation(libs.coilGif)
    implementation(libs.coilSvg)

    //Modules
    implementation(libs.onebox.cropper)
    implementation(projects.libs.dynamicTheme)
    implementation(libs.onebox.colordetector)
    implementation(libs.onebox.beforeafter)
    implementation(projects.libs.gesture)
    implementation(libs.onebox.screenshot)
    implementation(libs.onebox.systemuicontroller)
    implementation(libs.onebox.placeholder)
    api(libs.toolbox.logger)
    implementation(projects.libs.twain)
    implementation(projects.libs.richtext)

    implementation(projects.libs.zoomable)
    implementation(project(":core:domain"))

    implementation(libs.onebox.colorpicker) {
        exclude("com.github.SmartToolFactory", "Compose-Color-Detector")
    }

    implementation(libs.reorderable)

    implementation(libs.shadowGadgets)
    implementation(libs.shadowsPlus)

    implementation(libs.kotlinx.collections.immutable)

    implementation(libs.fadingEdges)
    implementation(libs.scrollbar)

    implementation(libs.androidx.palette.ktx)
    implementation(libs.m3color)
    implementation(libs.org.greenrebot.eventbus)
    /**
     * mmkv
     */
    "arm64Api"(libs.com.tencent.mmkv)
    "arm32Api"(libs.com.tencent.mmkv.arm32)
    "universalApi"(libs.com.tencent.mmkv)
    /**
     * lottie
     */
    implementation(libs.com.airbnb.lottie)

    /**
     * Json
     */
    implementation(libs.kotlinx.serialization.json)

    /**
     * markdown
     */

    implementation(libs.datastore.preferences.android)
    implementation(libs.coroutinesAndroid)


    implementation(projects.core.resources)
    implementation(projects.core.r)
    implementation(projects.core.ui)
    implementation(projects.core.settings)

    implementation(projects.core.model)
    implementation(projects.core.theme)
    implementation(projects.core.database)
    implementation(projects.core.interfaces)
    implementation(projects.core.storage)
}