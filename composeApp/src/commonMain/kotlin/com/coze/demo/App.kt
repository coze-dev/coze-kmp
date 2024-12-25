package com.coze.demo

import androidx.compose.runtime.Composable
import com.coze.demo.ui.MainScreen
import com.coze.demo.ui.theme.CozeTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun App() {
    CozeTheme {
        MainScreen()
    }
}

