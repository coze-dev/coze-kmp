package com.coze.demo.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coze.demo.AuthDemo
import com.coze.demo.ui.components.ErrorMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MainScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    
    // Auth 状态
    val authDemo = remember { AuthDemo }
    val coroutineScope = rememberCoroutineScope()
    var authResult by remember { mutableStateOf("") }
    var showAuthResult by remember { mutableStateOf(false) }
    var isAuthLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // 自动隐藏认证结果
    LaunchedEffect(showAuthResult) {
        if (showAuthResult) {
            delay(3000)
            showAuthResult = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "🤖",
                            fontSize = 24.sp
                        )
                        Text(
                            "Coze API Demo",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.onPrimary
                        )
                    }
                },
                backgroundColor = MaterialTheme.colors.primary,
                elevation = 8.dp,
                actions = {
                    // Auth 按钮
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isAuthLoading = true
                                try {
                                    authResult = authDemo.getJWTAuth() ?: ""
                                    showAuthResult = true
                                    errorMessage = null
                                } catch (e: Exception) {
                                    errorMessage = e.message
                                } finally {
                                    isAuthLoading = false
                                }
                            }
                        },
                        enabled = !isAuthLoading,
                        modifier = Modifier.padding(horizontal = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = MaterialTheme.colors.primary,
                            contentColor = MaterialTheme.colors.onPrimary,
                            disabledBackgroundColor = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
                        ),
                        elevation = ButtonDefaults.elevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 4.dp
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isAuthLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = MaterialTheme.colors.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("🔑", fontSize = 16.sp)
                            }
                            Text(
                                "验证 JWT",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigation(
                modifier = Modifier.height(64.dp),
                backgroundColor = MaterialTheme.colors.surface,
                elevation = 16.dp,
            ) {
                BottomNavigationItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Text("💌", fontSize = 20.sp, color = Color.Unspecified.copy(alpha = if (selectedTab == 0) 1f else 0.6f)) },
                    label = {
                        Text(
                            "聊天",
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    selectedContentColor = MaterialTheme.colors.primary,
                    unselectedContentColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
                BottomNavigationItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { 
                        Text(
                            "🗣️",
                            fontSize = 20.sp,
                            color = Color.Unspecified.copy(alpha = if (selectedTab == 1) 1f else 0.6f)
                        ) 
                    },
                    label = {
                        Text(
                            "对话",
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    selectedContentColor = MaterialTheme.colors.primary,
                    unselectedContentColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
                BottomNavigationItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { 
                        Text(
                            "🤖",
                            fontSize = 20.sp,
                            color = Color.Unspecified.copy(alpha = if (selectedTab == 2) 1f else 0.6f)
                        ) 
                    },
                    label = {
                        Text(
                            "机器人",
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    selectedContentColor = MaterialTheme.colors.primary,
                    unselectedContentColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
                BottomNavigationItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { 
                        Text(
                            "📁",
                            fontSize = 20.sp,
                            color = Color.Unspecified.copy(alpha = if (selectedTab == 3) 1f else 0.6f)
                        ) 
                    },
                    label = {
                        Text(
                            "文件",
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    selectedContentColor = MaterialTheme.colors.primary,
                    unselectedContentColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            Column {
                // 错误信息显示
                errorMessage?.let {
                    ErrorMessage(
                        message = it,
                        onDismiss = { errorMessage = null }
                    )
                }

                // Auth 结果显示
                AnimatedVisibility(
                    visible = showAuthResult,
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        elevation = 4.dp,
                        backgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "✅",
                                fontSize = 20.sp
                            )
                            Column {
                                Text(
                                    "认证成功",
                                    style = MaterialTheme.typography.subtitle1.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colors.primary
                                    )
                                )
                                Text(
                                    authResult,
                                    style = MaterialTheme.typography.caption,
                                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }

            // 主内容区域
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = if (errorMessage != null || showAuthResult) 100.dp else 0.dp)
            ) {
                when (selectedTab) {
                    0 -> ChatScreen()
                    1 -> ConversationScreen()
                    2 -> BotScreen()
                    3 -> FileScreen()
                }
            }
        }
    }
} 