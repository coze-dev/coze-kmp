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
import com.coze.demo.WorkspaceDemo
import com.coze.demo.ui.components.ErrorMessage
import com.coze.demo.ui.dataset.DatasetScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class NavigationTab(val icon: String, val label: String) {
    Chat("💌", "聊天对话"),
    Bot("🤖", "机器人"),
    Workflow("⚡️", "工作流"),
    Dataset("📚", "数据集"),
    Space("🏢", "其它"),
//    Template("🌐", "模板")
}

@Composable
fun MainScreen() {
    var selectedTab by remember { mutableStateOf(NavigationTab.Chat) }
    var selectedChatTab by remember { mutableStateOf(0) }
    var selectedSpaceTab by remember { mutableStateOf(0) }
    
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
                NavigationTab.values().forEach { tab ->
                    BottomNavigationItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { 
                            Text(
                                tab.icon,
                                fontSize = 20.sp,
                                color = Color.Unspecified.copy(alpha = if (selectedTab == tab) 1f else 0.6f)
                            ) 
                        },
                        label = {
                            Text(
                                tab.label,
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        selectedContentColor = MaterialTheme.colors.primary,
                        unselectedContentColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                }
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
                                "🔑",
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

                // 主内容区域
                when (selectedTab) {
                    NavigationTab.Chat -> {
                        Column {
                            TabRow(
                                selectedTabIndex = selectedChatTab,
                                backgroundColor = MaterialTheme.colors.surface,
                                contentColor = MaterialTheme.colors.primary
                            ) {
                                Tab(
                                    selected = selectedChatTab == 0,
                                    onClick = { selectedChatTab = 0 },
                                    text = { Text("聊天") }
                                )
                                Tab(
                                    selected = selectedChatTab == 1,
                                    onClick = { selectedChatTab = 1 },
                                    text = { Text("会话") }
                                )
                            }
                            when (selectedChatTab) {
                                0 -> ChatScreen()
                                1 -> ConversationScreen()
                            }
                        }
                    }
                    NavigationTab.Bot -> BotScreen()
                    NavigationTab.Workflow -> WorkflowScreen()
                    NavigationTab.Space -> {
                        Column {
                            TabRow(
                                selectedTabIndex = selectedSpaceTab,
                                backgroundColor = MaterialTheme.colors.surface,
                                contentColor = MaterialTheme.colors.primary
                            ) {
                                Tab(
                                    selected = selectedSpaceTab == 0,
                                    onClick = { selectedSpaceTab = 0 },
                                    text = { Text("工作空间") }
                                )
                                Tab(
                                    selected = selectedSpaceTab == 1,
                                    onClick = { selectedSpaceTab = 1 },
                                    text = { Text("文件") }
                                )
                            }
                            when (selectedSpaceTab) {
                                0 -> {
                                    val workspaceDemo = remember { WorkspaceDemo() }
                                    WorkspaceScreen(
                                        workspaceService = workspaceDemo.workspaceService,
                                        onWorkspaceSelected = { workspace ->
                                            println("Selected workspace: ${workspace.name}")
                                        }
                                    )
                                }
                                1 -> FileScreen()
                            }
                        }
                    }
                    NavigationTab.Dataset -> {
                        val workspaceDemo = remember { WorkspaceDemo() }
                        DatasetScreen(
                            workspaceService = workspaceDemo.workspaceService,
                            onWorkspaceSelected = { workspace ->
                                println("Selected workspace for dataset: ${workspace.name}")
                            }
                        )
                    }
//                    NavigationTab.Template -> WebViewScreen("https://www.coze.cn/template/project/7451809368615632923")
                }
            }
        }
    }
} 
