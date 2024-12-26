package com.coze.demo.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.coze.api.model.workspace.Workspace
import com.coze.api.model.workspace.WorkspaceListRequest
import com.coze.api.workspace.WorkspaceService
import com.coze.demo.ui.components.ErrorMessage
import kotlinx.coroutines.launch

@Composable
fun WorkspaceScreen(
    workspaceService: WorkspaceService,
    onWorkspaceSelected: (Workspace) -> Unit
) {
    var workspaces by remember { mutableStateOf<List<Workspace>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var pageNum by remember { mutableStateOf(1) }
    var pageSize by remember { mutableStateOf(10) }
    var totalCount by remember { mutableStateOf(0) }
    
    val coroutineScope = rememberCoroutineScope()
    
    fun loadWorkspaces() {
        coroutineScope.launch {
            isLoading = true
            try {
                val response = workspaceService.list(
                    req = WorkspaceListRequest(
                        pageNum = pageNum,
                        pageSize = pageSize
                    )
                ).data
                workspaces = response?.workspaces!!
                totalCount = response.totalCount
                errorMessage = null
            } catch (e: Exception) {
                println("[Workspace] Load failed: ${e.message}")
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }
    
    // 初始加载
    LaunchedEffect(Unit) {
        loadWorkspaces()
    }
    
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        // 错误信息显示
        errorMessage?.let {
            ErrorMessage(
                message = it,
                onDismiss = { errorMessage = null }
            )
        }

        // 分页控制
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "总数: $totalCount",
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (pageNum > 1) {
                            pageNum--
                            loadWorkspaces()
                        }
                    },
                    enabled = !isLoading && pageNum > 1
                ) {
                    Text("上一页")
                }
                
                Text(
                    "第 $pageNum 页",
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                
                Button(
                    onClick = {
                        if (pageNum * pageSize < totalCount) {
                            pageNum++
                            loadWorkspaces()
                        }
                    },
                    enabled = !isLoading && pageNum * pageSize < totalCount
                ) {
                    Text("下一页")
                }
            }
        }

        // 工作空间列表
        Card(
            modifier = Modifier.fillMaxWidth().weight(1f),
            elevation = 4.dp,
            backgroundColor = MaterialTheme.colors.surface
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    workspaces.isEmpty() -> {
                        Text(
                            text = "暂无工作空间",
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(workspaces) { workspace ->
                                WorkspaceItem(
                                    workspace = workspace,
                                    onClick = { onWorkspaceSelected(workspace) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun WorkspaceItem(
    workspace: Workspace,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        elevation = 2.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🏢",
                style = MaterialTheme.typography.h6
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = workspace.name,
                    style = MaterialTheme.typography.subtitle1
                )
                Text(
                    text = "角色: ${workspace.roleType}",
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            }
            
            Text(
                text = workspace.workspaceType,
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.primary
            )
        }
    }
} 