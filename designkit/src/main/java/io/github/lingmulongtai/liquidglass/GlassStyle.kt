package io.github.lingmulongtai.liquidglass

enum class GlassSurfaceGroup { CONTAINERS, HEADER, NAVIGATION, CONTROLS }

internal enum class GlassParameter(val min: Float, val max: Float) {
    FROST(0f, 40f), BEND(0f, 2f), DISPERSION(0f, 1f), RIM(0f, 1f),
    SHINE(0f, 1f), SATURATION(0f, 2f), TINT(0f, 1f), REACH(.25f, 2f)
}

/** Null keeps each component's original material, including its pressed/resting differences. */
data class GlassTuning(
    val frost: Float? = null, val bend: Float? = null, val dispersion: Float? = null,
    val rim: Float? = null, val shine: Float? = null, val saturation: Float? = null,
    val tint: Float? = null, val reach: Float? = null
) {
    internal fun value(parameter: GlassParameter): Float? = when (parameter) {
        GlassParameter.FROST -> frost; GlassParameter.BEND -> bend; GlassParameter.DISPERSION -> dispersion
        GlassParameter.RIM -> rim; GlassParameter.SHINE -> shine; GlassParameter.SATURATION -> saturation
        GlassParameter.TINT -> tint; GlassParameter.REACH -> reach
    }
    internal fun withValue(parameter: GlassParameter, value: Float?): GlassTuning {
        val safe = value?.takeIf { it.isFinite() }?.coerceIn(parameter.min, parameter.max)
        return when (parameter) {
            GlassParameter.FROST -> copy(frost = safe); GlassParameter.BEND -> copy(bend = safe)
            GlassParameter.DISPERSION -> copy(dispersion = safe); GlassParameter.RIM -> copy(rim = safe)
            GlassParameter.SHINE -> copy(shine = safe); GlassParameter.SATURATION -> copy(saturation = safe)
            GlassParameter.TINT -> copy(tint = safe); GlassParameter.REACH -> copy(reach = safe)
        }
    }
    internal fun normalized(): GlassTuning = GlassParameter.entries.fold(this) { result, key -> result.withValue(key, value(key)) }
}

/** Material adjustments for each group of glass surfaces. */
data class GlassStyle(
    val containers: GlassTuning = GlassTuning(
        frost = 8f,
        bend = .7f,
        dispersion = .3f,
        shine = .15f,
        saturation = 1.4f,
        tint = .2f,
        reach = 1f
    ),
    val header: GlassTuning = GlassTuning(),
    val navigation: GlassTuning = GlassTuning(), val controls: GlassTuning = GlassTuning()
) {
    internal fun tuning(group: GlassSurfaceGroup): GlassTuning = when (group) {
        GlassSurfaceGroup.CONTAINERS -> containers
        GlassSurfaceGroup.HEADER -> header
        GlassSurfaceGroup.NAVIGATION -> navigation
        GlassSurfaceGroup.CONTROLS -> controls
    }
}
