package com.cipherxzc.whatsnext.ui.core.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.cipherxzc.whatsnext.R

//val notoSansScVar = FontFamily(
//    Font(R.font.noto_sans_sc, weight = FontWeight.W400),
//    Font(R.font.noto_sans_sc, weight = FontWeight.W700)
//)
//
//val interLatinVar = FontFamily(
//    Font(R.font.inter, weight = FontWeight.Normal),
//    Font(R.font.inter_italic, weight = FontWeight.Normal, style = FontStyle.Italic)
//)

// 合并 Latin + CJK，Latin 放后做备用
val WhatsNextSans = FontFamily(
    Font(R.font.noto_sans_sc, weight = FontWeight.W400),
    Font(R.font.noto_sans_sc, weight = FontWeight.W700),
    Font(R.font.inter, weight = FontWeight.Normal),
    Font(R.font.inter_italic, weight = FontWeight.Normal, style = FontStyle.Italic)
)

val WhatsNextTypography = Typography(
    bodyMedium = TextStyle(  // 默认正文、列表项
        fontFamily = WhatsNextSans,
        fontSize = 15.sp,
        lineHeight = 22.sp
    ),
    titleMedium = TextStyle(  // 卡片标题 / 任务标题
        fontFamily = WhatsNextSans,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
    ),
    displaySmall = TextStyle(  // AppBar / Dialog 标题
        fontFamily = WhatsNextSans,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold
    ),
    labelSmall = TextStyle(  // Chip / Tag / Caption
        fontFamily = WhatsNextSans,
        fontSize   = 12.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 16.sp,
        letterSpacing = 0.3.sp
    )
)