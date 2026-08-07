package com.t8rin.imagetoolbox.core.settings.domain.model

/**
 * 预置渐变背景样式。
 *
 * 每种样式在 `MeshGradientBackground` 中映射到不同的光斑分布、颜色角色和透明度。
 * 使用 [ordinal2] 持久化到 DataStore，通过 [fromOrdinal] 反序列化。
 *
 * | 名称        | 描述                                                   |
 * |-------------|--------------------------------------------------------|
 * | Classic     | 经典四角光斑（默认，兼容旧版）                          |
 * | Aurora      | 极光——薄荷 + 青绿 + 薰衣草 + 柔粉，北欧冷色调           |
 * | Ocean       | 深海——午夜蓝 + 深青 + 荧光青 + 靛蓝，沉静神秘           |
 * | Sunset      | 日落——琥珀橙 + 玫瑰粉 + 金色，暖色漫射                  |
 * | SakuraMist  | 樱花雾——柔粉 + 蜜桃 + 淡紫 + 奶油，温柔梦幻             |
 * | MintBreeze  | 薄荷苏打——薄荷绿 + 晴空蓝 + 嫩芽绿 + 奶黄，清新气泡     |
 * | StarryNight | 午夜星河——午夜蓝 + 靛青 + 银灰 + 深蓝，静谧深邃         |
 * | Lavender    | 薰衣草田——紫罗兰 + 丁香 + 柔粉 + 淡蓝，浪漫花海         |
 * | WarmGlow    | 焦糖余晖——琥珀 + 珊瑚 + 奶油 + 暖金，黄昏暖光           |
 * | Ethereal    | 丝雾——跟随主题色的极淡渐变，清透宁静                    |
 * | NeonCyber   | 电子梦境——电青 + 紫罗兰 + 荧光绿 + 炽橙，高饱和幻境     |
 */
enum class GradientBackgroundStyle(val ordinal2: Int) {
    Classic(0),
    Aurora(1),
    Ocean(2),
    Sunset(3),
    SakuraMist(4),
    MintBreeze(5),
    StarryNight(6),
    Lavender(7),
    WarmGlow(8),
    Ethereal(9),
    NeonCyber(10);

    companion object {
        val entries2: List<GradientBackgroundStyle> = values().toList()

        fun fromOrdinal(ordinal: Int?): GradientBackgroundStyle =
            values().firstOrNull { it.ordinal2 == ordinal } ?: Classic
    }
}
