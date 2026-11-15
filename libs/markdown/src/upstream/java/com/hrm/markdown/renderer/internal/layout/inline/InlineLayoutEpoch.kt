package com.hrm.markdown.renderer.internal.layout.inline

/**
 * Precomputed identity for immutable values used by cache keys.
 *
 * Hashing happens exactly once. Equality still compares the complete value, so colliding hashes
 * remain distinct. The wrapped value must stay immutable while this identity is retained.
 */
internal class StructuralIdentity private constructor(
    private val value: Any?,
) {
    private val cachedHashCode: Int = value?.hashCode() ?: 0

    override fun equals(other: Any?): Boolean =
        other is StructuralIdentity && value == other.value

    override fun hashCode(): Int = cachedHashCode

    companion object {
        fun of(value: Any?): StructuralIdentity = StructuralIdentity(value)
    }
}

/**
 * Referential identity for stateful collaborators that do not have a stable value version.
 *
 * A constant hash deliberately avoids calling a potentially mutable value-based [Any.hashCode].
 * The surrounding cache key still contains block identity, revision and layout constraints for
 * bucket distribution. Equality checks the reference itself.
 */
internal class ReferentialIdentity private constructor(
    private val value: Any?,
) {
    override fun equals(other: Any?): Boolean =
        other is ReferentialIdentity && value === other.value

    override fun hashCode(): Int = 0

    companion object {
        fun of(value: Any?): ReferentialIdentity = ReferentialIdentity(value)
    }
}

internal class InlineLayoutEpoch(
    private val themeIdentity: StructuralIdentity,
    private val directiveRegistryIdentity: ReferentialIdentity,
    private val configIdentity: StructuralIdentity,
    private val densityBits: Int,
    private val fontScaleBits: Int,
    private val textMeasurerIdentity: ReferentialIdentity,
    private val latexMeasurerIdentity: ReferentialIdentity,
    private val onLinkClickIdentity: ReferentialIdentity,
    private val onFootnoteClickIdentity: ReferentialIdentity,
) {
    private val cachedHashCode: Int = calculateHashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is InlineLayoutEpoch) return false
        return themeIdentity == other.themeIdentity &&
            directiveRegistryIdentity == other.directiveRegistryIdentity &&
            configIdentity == other.configIdentity &&
            densityBits == other.densityBits &&
            fontScaleBits == other.fontScaleBits &&
            textMeasurerIdentity == other.textMeasurerIdentity &&
            latexMeasurerIdentity == other.latexMeasurerIdentity &&
            onLinkClickIdentity == other.onLinkClickIdentity &&
            onFootnoteClickIdentity == other.onFootnoteClickIdentity
    }

    override fun hashCode(): Int = cachedHashCode

    private fun calculateHashCode(): Int {
        var result = themeIdentity.hashCode()
        result = 31 * result + directiveRegistryIdentity.hashCode()
        result = 31 * result + configIdentity.hashCode()
        result = 31 * result + densityBits
        result = 31 * result + fontScaleBits
        result = 31 * result + textMeasurerIdentity.hashCode()
        result = 31 * result + latexMeasurerIdentity.hashCode()
        result = 31 * result + onLinkClickIdentity.hashCode()
        return 31 * result + onFootnoteClickIdentity.hashCode()
    }
}
