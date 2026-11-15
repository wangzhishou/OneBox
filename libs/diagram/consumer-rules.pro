# =============================================================================
# diagram-render 消费方保留规则（Consumer ProGuard Rules）
#
# 该文件随 AAR 一起发布，会在使用本库的 Android 应用进行 R8/ProGuard 优化时自动生效。
#
# 编写原则（参考 Android 官方《库作者优化指南》）：
#  - 仅保留运行时反射 / JNI / 序列化等无法被 R8 静态分析到的入口点。
#  - 严禁包级 -keep（如 -keep com.hrm.diagram.render.** 会导致下游 App 体积膨胀）。
#  - 严禁全局选项（如 -dontobfuscate / -allowaccessmodification / -ignorewarnings）。
#  - Compose 运行时已自带 consumer-rules，本文件不再重复声明。
# =============================================================================
#
# diagram-render 不直接使用反射 / JNI / ServiceLoader，
# Compose / renderer / streaming facade 通过纯 Kotlin 与 Compose API 调用完成。
# 因此不需要任何 -keep 规则。
#
# Kotlin 反射所需的类元数据（Companion / data class / sealed 子类等）
-keepattributes InnerClasses,EnclosingMethod,Signature
#
# 公共 API 注解（@OptIn / @Stable / @Immutable 等运行时注解）
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations
#
# -----------------------------------------------------------------------------
# Compose Desktop / 普通 JVM ProGuard 兼容（仅压制 fatal warning，不阻止 shrink）
# -----------------------------------------------------------------------------
# 与 diagram-core / diagram-layout / diagram-parser 同因：Kotlin 编译器在
# `companion object` 中 `private inline fun` + `private val` 的提升机制，会在某些
# KMP/Kotlin 版本组合下触发 `*$Companion.class` 与 outer 类静态字段的引用错位，
# 被 ProGuard 全图静态校验视为 unresolved 并升级为 fatal。
# 仅压制对应 warning，不使用 `-keep`。
-dontwarn com.hrm.diagram.**
