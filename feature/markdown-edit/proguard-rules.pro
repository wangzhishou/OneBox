# Add project specific ProGuard rules here.
-keep class com.wanbaohe.markdown.edit.MarkdownEditorState { *; }
-keep class com.wanbaohe.markdown.edit.MarkdownToolConfig { *; }
# Keep data classes used for configuration

-keep class kotlin.Metadata { *; }
-keep class androidx.compose.** { *; }
# Keep Compose related classes

-keep public class com.wanbaohe.markdown.edit.** { *; }
# Keep Markdown Editor public API

# proguardFiles setting in build.gradle.kts.
# You can control the set of applied configuration files using the

