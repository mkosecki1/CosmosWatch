package com.cosmoswatch.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

val CosmosWatchTypography = Typography(
    headlineSmall = TextStyle(
        fontFamily = Fraunces,
        fontWeight = FrauncesWeightTitle,
        fontSize = 26.sp,
        lineHeight = 30.sp
    ),
    titleSmall = TextStyle(
        fontFamily = IbmPlexSans,
        fontWeight = FontWeight.Medium,
        fontSize = 15.5.sp,
        lineHeight = 19.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = IbmPlexSans,
        fontWeight = FontWeight.Normal,
        fontSize = 13.5.sp,
        lineHeight = 21.sp
    ),
    labelMedium = TextStyle(
        fontFamily = IbmPlexMono,
        fontWeight = FontWeight.Normal,
        fontSize = 11.5.sp,
        letterSpacing = 0.06.em
    ),
    labelSmall = TextStyle(
        fontFamily = IbmPlexMono,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        letterSpacing = 0.03.em
    )
)

val VideoTagTextStyle = TextStyle(
    fontFamily = IbmPlexMono,
    fontWeight = FontWeight.Medium,
    fontSize = 9.sp,
    letterSpacing = 0.06.em
)
