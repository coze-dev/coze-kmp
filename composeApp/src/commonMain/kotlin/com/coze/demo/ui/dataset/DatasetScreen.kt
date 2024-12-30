package com.coze.demo.ui.dataset

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.coze.api.workspace.WorkspaceService
import com.coze.api.model.workspace.Workspace
import com.coze.demo.DatasetDemo

@Composable
fun DatasetScreen(
    workspaceService: WorkspaceService,
    onWorkspaceSelected: (Workspace) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val scrollState = rememberScrollState()
    val datasetDemo = remember { DatasetDemo() }
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TabRow(
                selectedTabIndex = selectedTab,
                backgroundColor = MaterialTheme.colors.surface,
                contentColor = MaterialTheme.colors.primary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("数据集") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("文档") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("图片") }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(vertical = 16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (selectedTab) {
                    0 -> DatasetListScreen(datasetDemo, scaffoldState, scope)
                    1 -> DocumentScreen(datasetDemo, scaffoldState, scope)
                    2 -> ImageScreen(datasetDemo, scaffoldState, scope)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
} 