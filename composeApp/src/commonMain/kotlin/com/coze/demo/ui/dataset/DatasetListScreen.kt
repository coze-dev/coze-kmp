package com.coze.demo.ui.dataset

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.coze.api.model.dataset.*
import com.coze.demo.DatasetDemo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun DatasetListScreen(
    datasetDemo: DatasetDemo,
    scaffoldState: ScaffoldState,
    scope: CoroutineScope
) {
    var datasetName by remember { mutableStateOf("") }
    var createSpaceId by remember { mutableStateOf("7303429391730851841") }
    var querySpaceId by remember { mutableStateOf("7303429391730851841") }
    var description by remember { mutableStateOf("") }
    var datasetId by remember { mutableStateOf("") }
    var documentIds by remember { mutableStateOf("") }
    var pageNum by remember { mutableStateOf("1") }
    var pageSize by remember { mutableStateOf("10") }
    var isLoading by remember { mutableStateOf(false) }
    var datasetList by remember { mutableStateOf<List<Dataset>>(emptyList()) }
    var totalCount by remember { mutableStateOf(0) }

    suspend fun showMessage(message: String) {
        scaffoldState.snackbarHostState.showSnackbar(message)
    }

    // 创建数据集卡片
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = cardShape
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("创建数据集", style = MaterialTheme.typography.h6)
            OutlinedTextField(
                value = datasetName,
                onValueChange = { datasetName = it },
                label = { Text("数据集名称") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = createSpaceId,
                onValueChange = { createSpaceId = it },
                label = { Text("空间ID") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("描述") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    if (datasetName.isBlank() || createSpaceId.isBlank()) {
                        scope.launch {
                            showMessage("请填写必要信息")
                        }
                        return@Button
                    }
                    
                    if (isLoading) return@Button
                    isLoading = true
                    
                    scope.launch {
                        try {
                            val response = datasetDemo.createDataset(
                                name = datasetName,
                                spaceId = createSpaceId,
                                description = description.takeIf { it.isNotBlank() }
                            )
                            response.data?.datasetId?.let { id ->
                                showMessage("创建成功：$id")
                                // 清空创建表单
                                datasetName = ""
                                createSpaceId = ""
                                description = ""
                            } ?: showMessage("创建失败：响应数据为空")
                        } catch (e: Exception) {
                            showMessage(e.message ?: "创建失败")
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading,
                shape = buttonShape
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("创建")
                }
            }
        }
    }

    // 数据集列表卡片
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = cardShape
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("数据集列表", style = MaterialTheme.typography.h6)
            OutlinedTextField(
                value = querySpaceId,
                onValueChange = { querySpaceId = it },
                label = { Text("空间ID") },
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = pageNum,
                    onValueChange = { pageNum = it },
                    label = { Text("页码") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = pageSize,
                    onValueChange = { pageSize = it },
                    label = { Text("每页数量") },
                    modifier = Modifier.weight(1f)
                )
            }
            Button(
                onClick = {
                    if (querySpaceId.isBlank()) {
                        scope.launch {
                            showMessage("请填写空间ID")
                        }
                        return@Button
                    }
                    
                    if (isLoading) return@Button
                    isLoading = true
                    
                    scope.launch {
                        try {
                            val response = datasetDemo.listDatasets(
                                spaceId = querySpaceId,
                                pageNum = pageNum.toIntOrNull() ?: 1,
                                pageSize = pageSize.toIntOrNull() ?: 10
                            )
                            response.data?.let { data ->
                                showMessage("查询成功：共 ${data.totalCount} 条记录")
                                datasetList = data.datasetList
                                totalCount = data.totalCount
                            } ?: showMessage("查询失败：响应数据为空")
                        } catch (e: Exception) {
                            showMessage(e.message ?: "查询失败")
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading,
                shape = buttonShape
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("查询")
                }
            }

            if (datasetList.isNotEmpty()) {
                Text(
                    "共 $totalCount 条记录",
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.primary
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    datasetList.forEach { dataset ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = 2.dp,
                            shape = itemShape,
                            backgroundColor = MaterialTheme.colors.surface
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        dataset.name,
                                        style = MaterialTheme.typography.subtitle1,
                                        color = MaterialTheme.colors.primary
                                    )
                                    Text(
                                        "ID: ${dataset.datasetId}",
                                        style = MaterialTheme.typography.caption,
                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                                if (dataset.description.isNotEmpty()) {
                                    Text(
                                        dataset.description,
                                        style = MaterialTheme.typography.body2,
                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f)
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        "文档数: ${dataset.docCount}",
                                        style = MaterialTheme.typography.caption
                                    )
                                    Text(
                                        "片段数: ${dataset.sliceCount}",
                                        style = MaterialTheme.typography.caption
                                    )
                                    Text(
                                        "使用次数: ${dataset.hitCount}",
                                        style = MaterialTheme.typography.caption
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Text(
                    "暂无数据",
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        }
    }

    // 删除数据集卡片
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = cardShape
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("删除数据集", style = MaterialTheme.typography.h6)
            OutlinedTextField(
                value = datasetId,
                onValueChange = { datasetId = it },
                label = { Text("数据集ID") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    if (datasetId.isBlank()) {
                        scope.launch {
                            showMessage("请填写数据集ID")
                        }
                        return@Button
                    }
                    
                    if (isLoading) return@Button
                    isLoading = true
                    
                    scope.launch {
                        try {
                            val response = datasetDemo.deleteDataset(datasetId)
                            if (response.code == 0) {
                                showMessage("删除成功")
                                // 刷新列表
                                if (querySpaceId.isNotBlank()) {
                                    val listResponse = datasetDemo.listDatasets(
                                        spaceId = querySpaceId,
                                        pageNum = pageNum.toIntOrNull() ?: 1,
                                        pageSize = pageSize.toIntOrNull() ?: 10
                                    )
                                    listResponse.data?.let { data ->
                                        datasetList = data.datasetList
                                        totalCount = data.totalCount
                                    }
                                }
                            } else {
                                showMessage("删除失败：${response.msg}")
                            }
                        } catch (e: Exception) {
                            showMessage(e.message ?: "删除失败")
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading,
                shape = buttonShape
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("删除")
                }
            }
        }
    }

    // 更新数据集卡片
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = cardShape
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("更新数据集", style = MaterialTheme.typography.h6)
            OutlinedTextField(
                value = datasetId,
                onValueChange = { datasetId = it },
                label = { Text("数据集ID") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = datasetName,
                onValueChange = { datasetName = it },
                label = { Text("新数据集名称") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("新描述") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    if (datasetId.isBlank()) {
                        scope.launch {
                            showMessage("请填写数据集ID")
                        }
                        return@Button
                    }
                    
                    if (isLoading) return@Button
                    isLoading = true
                    
                    scope.launch {
                        try {
                            val requestData = mapOf(
                                "name" to datasetName.takeIf { it.isNotBlank() },
                                "description" to description.takeIf { it.isNotBlank() }
                            )
                            println("更新数据集请求数据: $requestData")
                            
                            val response = datasetName.takeIf { it.isNotBlank() }?.let {
                                datasetDemo.updateDataset(
                                    datasetId = datasetId,
                                    name = it,
                                    description = description.takeIf { it.isNotBlank() }
                                )
                            }
                            if (response != null && response.code == 0) {
                                showMessage("更新成功")
                                // 刷新列表
                                if (querySpaceId.isNotBlank()) {
                                    val listResponse = datasetDemo.listDatasets(
                                        spaceId = querySpaceId,
                                        pageNum = pageNum.toIntOrNull() ?: 1,
                                        pageSize = pageSize.toIntOrNull() ?: 10
                                    )
                                    listResponse.data?.let { data ->
                                        datasetList = data.datasetList
                                        totalCount = data.totalCount
                                    }
                                }
                            } else {
                                showMessage("更新失败：响应数据为空")
                            }
                        } catch (e: Exception) {
                            showMessage(e.message ?: "更新失败")
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading,
                shape = buttonShape
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("更新")
                }
            }
        }
    }
} 