# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# 对于你的应用程序关键包路径，保持更多信息
-keepattributes Exceptions,InnerClasses,Signature,Deprecated,*Annotation*

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile

-keepclassmembers class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator CREATOR;
}

-keep class * implements com.t8rin.imagetoolbox.core.filters.presentation.model.UiFilter
-keepclassmembers class * implements com.t8rin.imagetoolbox.core.filters.presentation.model.UiFilter {
    <init>(...);
}
-keep class * implements com.t8rin.imagetoolbox.core.filters.domain.model.Filter
-keepclassmembers class * implements com.t8rin.imagetoolbox.core.filters.domain.model.Filter {
    <init>(...);
}
-keepclassmembers class com.t8rin.imagetoolbox.core.filters.** {
    <init>(...);
}
-keep class com.t8rin.imagetoolbox.core.filters.**
-keep class com.t8rin.imagetoolbox.core.filters.*

# Please add these rules to your existing keep rules in order to suppress warnings.
# This is generated automatically by the Android Gradle plugin.
-dontwarn org.bouncycastle.jsse.BCSSLParameters
-dontwarn org.bouncycastle.jsse.BCSSLSocket
-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
-dontwarn org.conscrypt.Conscrypt$Version
-dontwarn org.conscrypt.Conscrypt
-dontwarn org.conscrypt.ConscryptHostnameVerifier
-dontwarn org.openjsse.javax.net.ssl.SSLParameters
-dontwarn org.openjsse.javax.net.ssl.SSLSocket
-dontwarn org.openjsse.net.ssl.OpenJSSE
-dontwarn sun.security.x509.X509Key

-keep class org.beyka.tiffbitmapfactory.**{ *; }

-keep class org.bouncycastle.jcajce.provider.** { *; }
-keep class org.bouncycastle.jce.provider.** { *; }

-keepnames class com.shifenmiao.model.*
-keepnames class com.shifenmiao.model.** { *; }

-keepnames class com.shifenmiao.network.model.*
-keepnames class com.shifenmiao.network.model.** { *; }

##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-dontwarn sun.misc.**
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.shifenmiao.network.model.** { <fields>; }
-keepclassmembers class com.shifenmiao.model.** { <fields>; }
-keepclassmembers class com.shifenmiao.ai.model.** { <fields>; }
-keepclassmembers @kotlinx.serialization.Serializable class ** {
  <fields>;
}
-keepclassmembers class ** implements java.io.Serializable {
  <fields>;
}

# Prevent proguard from stripping interface information from TypeAdapter, TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Prevent R8 from leaving Data object members always null
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# Retain generic signatures of TypeToken and its subclasses with R8 version 3.0 and higher.
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken

# Keep all *Params data classes used by gson.fromJson(...Params::class.java) inside builtin tools.
# Without this, R8 inlines/marks these private nested data classes as abstract and removes their
# constructors, breaking Gson reflective instantiation at runtime.
-keep class com.shifenmiao.ai.agent.tool.builtin.**$*Params { *; }
-keep class com.shifenmiao.ai.agent.tool.builtin.**$*ToolParams { *; }

# Keep top-level data classes deserialized by Gson at runtime.
# R8's horizontal class merging can merge a public data class (e.g. AgentUserQuestionRequest)
# with unrelated classes into a synthetic abstract class, breaking gson.fromJson(...::class.java).
-keep class com.shifenmiao.ai.agent.tool.AgentUserQuestionRequest { *; }
-keep class com.shifenmiao.ai.agent.tool.AgentUserQuestionItem { *; }
-keep class com.shifenmiao.ai.agent.tool.AgentUserQuestionOption { *; }
-keep class com.shifenmiao.ai.agent.tool.AgentUserQuestionPresentation { *; }
-keep class com.shifenmiao.ai.agent.tool.InteractivePendingRequestSnapshot { *; }
-keep class com.shifenmiao.ai.agent.tool.LegacyPendingRequest { *; }
-keep class com.shifenmiao.ai.agent.tool.ToolConfirmationRequest { *; }
-keep class com.shifenmiao.model.ai.tool.ConversationToolPolicy { *; }
-keep class com.shifenmiao.model.ai.tool.ToolCatalogItem { *; }
-keep class com.shifenmiao.ai.utils.AgentToolCallRecord { *; }
-keep class com.shifenmiao.ai.agent.ToolCallRecord { *; }

##---------------End: proguard configuration for Gson  ----------

-keep class androidx.lifecycle.LiveData { *; }


# Retrofit does reflection on generic parameters. InnerClasses is required to use Signature and
# EnclosingMethod is required to use InnerClasses.
-keepattributes Signature, InnerClasses, EnclosingMethod

# Retrofit does reflection on method and parameter annotations.
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# Keep annotation default values (e.g., retrofit2.http.Field.encoded).
-keepattributes AnnotationDefault

# Retain service method parameters when optimizing.
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Ignore annotation used for build tooling.
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# Ignore JSR 305 annotations for embedding nullability information.
-dontwarn javax.annotation.**

# Guarded by a NoClassDefFoundError try/catch and only used when on the classpath.
-dontwarn kotlin.Unit

# Top-level functions that can only be used by Kotlin.
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# With R8 full mode, it sees no subtypes of Retrofit interfaces since they are created with a Proxy
# and replaces all potential values with null. Explicitly keeping the interfaces prevents this.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>

# Keep inherited services.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface * extends <1>

# With R8 full mode generic signatures are stripped for classes that are not
# kept. Suspend functions are wrapped in continuations where the type argument
# is used.
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# R8 full mode strips generic signatures from return types if not kept.
-if interface * { @retrofit2.http.* public *** *(...); }
-keep,allowoptimization,allowshrinking,allowobfuscation class <3>

# With R8 full mode generic signatures are stripped for classes that are not kept.
-keep,allowobfuscation,allowshrinking class retrofit2.Response

# Keep all classes and fields annotated with @Serializable
-keep @kotlinx.serialization.Serializable class * { *; }

#wechat opensdk
-keep class com.tencent.mm.opensdk.** {
    *;
}

-keep class com.tencent.wxop.** {
    *;
}

-keep class com.tencent.mm.sdk.** {
    *;
}
#event bus
-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

-keep class com.zackratos.ultimatebarx.ultimatebarx.** { *; }
-keep public class * extends androidx.fragment.app.Fragment { *; }


# Material icons: reflection removed, R8 tree-shaking enabled.
# Only directly-referenced icons in ImageVectorMap are kept.
# (Previously kept all outlined+filled classes for Class.forName usage)
# app/proguard-rules.pro


#baidu
-keep class com.baidu.speech.**{*;}
-keep class com.baidu.tts.**{*;}
-keep class com.baidu.speechsynthesizer.**{*;}

-keep class org.bouncycastle.jcajce.provider.** { *; }
-keep class org.bouncycastle.jce.provider.** { *; }


-keep class com.tencent.mars.** {
  public protected private *;
}

-keep class com.wanbaohe.explorer.** { *; }
-keep class org.eclipse.tm4e.** { *; }
-keep class org.joni.** { *; }

-dontwarn com.google.re2j.**
# qweather SDK
-keep public class com.qweather.sdk.QWeather {
    public *;
}

-keep public class com.qweather.sdk.basic.**{ *; }
-keepclassmembers class com.qweather.sdk.basic** { *; }

-keep public class com.qweather.sdk.parameter.**{ *; }
-keepclassmembers class com.qweather.sdk.parameter** { *; }

-keep public class com.qweather.sdk.response.**{ *; }
-keepclassmembers class com.qweather.sdk.response** { *; }

-keep interface com.qweather.sdk.Callback{  *; }
-keep interface com.qweather.sdk.TokenGenerator{  *; }
-keep public class com.qweather.sdk.JWTGenerator {
    public *;
}

# Suppress warnings for missing optional classes referenced by libraries.
# javax.el.* is referenced by net.engio.mbassy (MBassador) and is not used on Android.
# org.ietf.jgss.* is referenced by com.hierynomus.smbj and is not used on Android.
-dontwarn javax.el.BeanELResolver
-dontwarn javax.el.ELContext
-dontwarn javax.el.ELResolver
-dontwarn javax.el.ExpressionFactory
-dontwarn javax.el.FunctionMapper
-dontwarn javax.el.ValueExpression
-dontwarn javax.el.VariableMapper
-dontwarn org.ietf.jgss.GSSContext
-dontwarn org.ietf.jgss.GSSCredential
-dontwarn org.ietf.jgss.GSSException
-dontwarn org.ietf.jgss.GSSManager
-dontwarn org.ietf.jgss.GSSName
-dontwarn org.ietf.jgss.Oid

-dontwarn org.opencv.geometry.Geometry