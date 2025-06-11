package com.cipherxzc.whatsnext.ui.core.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val WhatsNextShapes = Shapes(
    /* 小型元素：Tag、Chip、Switch 滑块 */
    small  = RoundedCornerShape(8.dp),
    /* 中型：Card、Dialog、Button */
    medium = RoundedCornerShape(16.dp),
    /* 大型：底部 Sheet、Modal、侧边抽屉 */
    large  = RoundedCornerShape(28.dp)
)