package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val CyberFontFamily = FontFamily(
    Font(R.font.share_tech_mono, FontWeight.Normal)
)

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = CyberFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        letterSpacing = 1.sp,
        color = CyberRedBright
    ),
    displayMedium = TextStyle(
        fontFamily = CyberFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        letterSpacing = 1.sp,
        color = CyberRed
    ),
    titleLarge = TextStyle(
        fontFamily = CyberFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.8.sp,
        color = CyberRedBright
    ),
    titleMedium = TextStyle(
        fontFamily = CyberFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.5.sp,
        color = CyberTextPrimary
    ),
    bodyLarge = TextStyle(
        fontFamily = CyberFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.4.sp,
        color = CyberTextPrimary
    ),
    bodyMedium = TextStyle(
        fontFamily = CyberFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.3.sp,
        color = CyberTextPrimary
    ),
    bodySmall = TextStyle(
        fontFamily = CyberFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.2.sp,
        color = CyberTextSecondary
    ),
    labelLarge = TextStyle(
        fontFamily = CyberFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.8.sp,
        color = CyberRedBright
    ),
    labelMedium = TextStyle(
        fontFamily = CyberFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
        color = CyberTextPrimary
    ),
    labelSmall = TextStyle(
        fontFamily = CyberFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.3.sp,
        color = CyberTextDim
    )
)
