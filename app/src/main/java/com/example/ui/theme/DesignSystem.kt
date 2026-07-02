package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

object DesignSystem {
    object Spacing {
        val ExtraSmall = 4.dp
        val Small = 8.dp
        val Medium = 12.dp
        val Default = 16.dp
        val SectionSpacing = 18.dp
        val Large = 20.dp
        val ExtraLarge = 24.dp
    }

    object Shapes {
        val Circle = RoundedCornerShape(30.dp)
        val CardSmall = RoundedCornerShape(10.dp)
        val CardMedium = RoundedCornerShape(12.dp)
        val CardLarge = RoundedCornerShape(16.dp)
        val CardExtraLarge = RoundedCornerShape(20.dp)
        val CardHuge = RoundedCornerShape(24.dp)
    }

    object Elevation {
        val CardLow = 1.dp
        val CardMedium = 4.dp
    }
}
