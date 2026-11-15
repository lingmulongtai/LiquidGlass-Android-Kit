@file:OptIn(ExperimentalTextApi::class)

package io.github.lingmulongtai.liquidglass

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight

private fun inter(weight: Int) = Font(
    resId = R.font.liquidglass_inter,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight), FontVariation.opticalSizing(14.sp))
)

internal val InterFamily = FontFamily(inter(400), inter(500), inter(600), inter(700), inter(800))
