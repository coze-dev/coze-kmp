package com.coze.demo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

// 自定义颜色
private object CozeColors {
    // 主蓝色系列
    val Blue50 = Color(0xFFEBF5FB)   // 最浅，几乎是白色带蓝
    val Blue100 = Color(0xFFD6EAF8)  // 非常浅的蓝色
    val Blue200 = Color(0xFFAED6F1)  // 浅蓝色
    val Blue300 = Color(0xFF85C1E9)  // 中浅蓝色
    val Blue400 = Color(0xFF5DADE2)  // 中蓝色
    val Blue500 = Color(0xFF3498DB)  // 标准蓝色
    
    // 灰色系列
    val Grey50 = Color(0xFFFBFBFB)   // 几乎纯白
    val Grey100 = Color(0xFFF7F7F7)  // 非常浅的灰色
    val Grey200 = Color(0xFFF2F2F2)  // 浅灰色
    val Grey300 = Color(0xFFE8E8E8)  // 中浅灰色
    val Grey700 = Color(0xFF666666)  // 中灰色
    val Grey900 = Color(0xFF212121)  // 深灰色
}

// 亮色主题颜色
private val LightColors = lightColors(
    primary = CozeColors.Blue300,          // 主色调：清新的中浅蓝色
    primaryVariant = CozeColors.Blue400,   // 深一点的主色调
    secondary = CozeColors.Blue200,        // 次要色调：浅蓝色
    secondaryVariant = CozeColors.Blue300, // 深一点的次要色调
    background = CozeColors.Grey50,        // 背景色：几乎纯白
    surface = Color.White,                 // 表面色：纯白
    error = Color(0xFFE57373),            // 错误色：柔和的红色
    onPrimary = Color.White,              // 主色调上的文字颜色：白色
    onSecondary = CozeColors.Grey900,     // 次要色调上的文字颜色：深灰色
    onBackground = CozeColors.Grey900,     // 背景上的文字颜色：深灰色
    onSurface = CozeColors.Grey900,       // 表面上的文字颜色：深灰色
    onError = Color.White                  // 错误色上的文字颜色：白色
)

// 暗色主题颜色
private val DarkColors = darkColors(
    primary = CozeColors.Blue300,         // 主色调：中浅蓝色
    primaryVariant = CozeColors.Blue400,  // 深蓝色
    secondary = CozeColors.Blue200,       // 次要色调：浅蓝色
    background = CozeColors.Grey900,      // 背景色：深灰色
    surface = Color(0xFF1E1E1E),         // 表面色：稍浅的深灰色
    error = Color(0xFFEF9A9A),           // 错误色：浅红色
    onPrimary = Color.White,             // 主色调上的文字颜色：白色
    onSecondary = Color.White,           // 次要色调上的文字颜色：白色
    onBackground = Color.White,          // 背景上的文字颜色：白色
    onSurface = Color.White,            // 表面上的文字颜色：白色
    onError = Color.Black               // 错误色上的文字颜色：黑色
)

// 自定义形状
private val Shapes = Shapes(
    small = androidx.compose.foundation.shape.RoundedCornerShape(6),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(8),
    large = androidx.compose.foundation.shape.RoundedCornerShape(12)
)

// 自定义文字样式
private val Typography = Typography(
    h6 = TextStyle(
        fontSize = 18.sp,
        letterSpacing = 0.15.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
    ),
    subtitle1 = TextStyle(
        fontSize = 15.sp,
        letterSpacing = 0.15.sp
    ),
    body1 = TextStyle(
        fontSize = 14.sp,
        letterSpacing = 0.25.sp
    ),
    button = TextStyle(
        fontSize = 14.sp,
        letterSpacing = 0.5.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
    ),
    caption = TextStyle(
        fontSize = 12.sp,
        letterSpacing = 0.4.sp
    )
)

@Composable
fun CozeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
} 