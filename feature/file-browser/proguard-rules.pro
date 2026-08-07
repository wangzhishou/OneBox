# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep file browser models
-keep class com.wanbaohe.file.browser.model.** { *; }

# Keep component factory
-keep interface com.wanbaohe.file.browser.screenLogic.FileBrowserComponent$Factory { *; }

