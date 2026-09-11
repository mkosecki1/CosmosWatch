package com.cosmoswatch.core.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.cosmoswatch.core.ui.R

val FrauncesWeightWordmark = FontWeight(480)
val FrauncesWeightTitle = FontWeight(560)

val Fraunces = FontFamily(
    Font(
        R.font.fraunces_variable,
        weight = FrauncesWeightWordmark,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(480),
            FontVariation.Setting("opsz", 20f)
        ),
    ),
    Font(
        R.font.fraunces_variable,
        weight = FrauncesWeightTitle,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(560),
            FontVariation.Setting("opsz", 20f)
        ),
    ),
)

val IbmPlexSans = FontFamily(
    Font(
        R.font.ibm_plex_sans_variable,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400)),
    ),
    Font(
        R.font.ibm_plex_sans_variable,
        weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(FontVariation.weight(500)),
    ),
)

val IbmPlexMono = FontFamily(
    Font(R.font.ibm_plex_mono_regular, weight = FontWeight.Normal),
    Font(R.font.ibm_plex_mono_medium, weight = FontWeight.Medium),
)
