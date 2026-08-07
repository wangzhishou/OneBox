# Add project specific ProGuard rules here.
-keep class com.wanbaohe.code.editor.CodeEditorState { *; }
-keep class com.wanbaohe.code.editor.CodeToolConfig { *; }
# Keep data classes used for configuration

-keep class kotlin.Metadata { *; }
-keep class androidx.compose.** { *; }
# Keep Compose related classes

-keep public class com.wanbaohe.code.editor.** { *; }
# Keep Code Editor public API

# proguardFiles setting in build.gradle.kts.
# You can control the set of applied configuration files using the
