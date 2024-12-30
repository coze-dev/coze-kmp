package com.coze.demo.ui.dataset

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
fun DocumentScreen(
    datasetDemo: DatasetDemo,
    scaffoldState: ScaffoldState,
    scope: CoroutineScope
) {
    var datasetId by remember { mutableStateOf("7453033229185007623") }
    var documentContent by remember { mutableStateOf("") }
    var documentName by remember { mutableStateOf("") }
    var documentId by remember { mutableStateOf("") }
    var documentIds by remember { mutableStateOf("") }
    var pageNum by remember { mutableStateOf("1") }
    var pageSize by remember { mutableStateOf("10") }
    var isLoading by remember { mutableStateOf(false) }
    var documentList by remember { mutableStateOf<List<Document>>(emptyList()) }
    var totalCount by remember { mutableStateOf(0) }

    suspend fun showMessage(message: String) {
        scaffoldState.snackbarHostState.showSnackbar(message)
    }

    // 上传文档卡片
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = cardShape
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("上传文档", style = MaterialTheme.typography.h6)
            OutlinedTextField(
                value = datasetId,
                onValueChange = { datasetId = it },
                label = { Text("数据集ID") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = documentName,
                onValueChange = { documentName = it },
                label = { Text("文档名称") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = documentContent,
                onValueChange = { documentContent = it },
                label = { Text("文档内容") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            Button(
                onClick = {
                    if (datasetId.isBlank() || documentContent.isBlank()) {
                        scope.launch {
                            showMessage("请填写必要信息")
                        }
                        return@Button
                    }
                    
                    if (isLoading) return@Button
                    isLoading = true
                    
                    scope.launch {
                        try {
                            val response = datasetDemo.createDocument(
                                datasetId = datasetId,
                                name = documentName.takeIf { it.isNotBlank() } ?: "文档.txt",
                                content = documentContent
                            )
                            response.data?.firstOrNull()?.documentId?.let { id ->
                                showMessage("上传成功：$id")
                                // 清空上传表单
                                documentName = ""
                                documentContent = ""
                            } ?: showMessage("上传失败：响应数据为空")
                        } catch (e: Exception) {
                            showMessage(e.message ?: "上传失败")
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
                    Text("上传")
                }
            }
        }
    }

    // 文档列表卡片
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = cardShape
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("文档列表", style = MaterialTheme.typography.h6)
            OutlinedTextField(
                value = datasetId,
                onValueChange = { datasetId = it },
                label = { Text("数据集ID") },
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
                            val response = datasetDemo.listDocuments(
                                datasetId = datasetId,
                                pageNum = pageNum.toIntOrNull() ?: 1,
                                pageSize = pageSize.toIntOrNull() ?: 10
                            )
                            if (response.code == 0) {
                                response.data?.let { data ->
                                    documentList = data
                                    totalCount = response.total ?: 0
                                    showMessage("查询成功：共 $totalCount 条记录")
                                }
                            } else {
                                showMessage("查询失败：${response.msg}")
                            }
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

            if (documentList.isNotEmpty()) {
                Text(
                    "共 $totalCount 条记录",
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.primary
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    documentList.forEach { document ->
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
                                        document.name,
                                        style = MaterialTheme.typography.subtitle1,
                                        color = MaterialTheme.colors.primary
                                    )
                                    Text(
                                        "ID: ${document.documentId}",
                                        style = MaterialTheme.typography.caption,
                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        "字符数: ${document.charCount}",
                                        style = MaterialTheme.typography.caption
                                    )
                                    Text(
                                        "片段数: ${document.sliceCount}",
                                        style = MaterialTheme.typography.caption
                                    )
                                    Text(
                                        "使用次数: ${document.hitCount}",
                                        style = MaterialTheme.typography.caption
                                    )
                                }
                                Text(
                                    "状态: ${document.status}",
                                    style = MaterialTheme.typography.caption,
                                    color = when (document.status) {
                                        DocumentStatus.COMPLETED -> MaterialTheme.colors.primary
                                        DocumentStatus.PROCESSING -> MaterialTheme.colors.secondary
                                        DocumentStatus.FAILED -> MaterialTheme.colors.error
                                    }
                                )
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

    // 删除文档卡片
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = cardShape
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("删除文档", style = MaterialTheme.typography.h6)
            OutlinedTextField(
                value = datasetId,
                onValueChange = { datasetId = it },
                label = { Text("数据集ID") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = documentIds,
                onValueChange = { documentIds = it },
                label = { Text("文档ID列表（用逗号分隔）") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    if (datasetId.isBlank() || documentIds.isBlank()) {
                        scope.launch {
                            showMessage("请填写必要信息")
                        }
                        return@Button
                    }
                    
                    if (isLoading) return@Button
                    isLoading = true
                    
                    scope.launch {
                        try {
                            val response = datasetDemo.deleteDocuments(
                                datasetId = datasetId,
                                documentIds = documentIds.split(",").map { it.trim() }
                            )
                            if (response.code == 0) {
                                showMessage("删除成功")
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

    // 更新文档卡片
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = cardShape
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("更新文档", style = MaterialTheme.typography.h6)
            OutlinedTextField(
                value = documentId,
                onValueChange = { documentId = it },
                label = { Text("文档ID") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = documentName,
                onValueChange = { documentName = it },
                label = { Text("新文档名称") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    if (documentId.isBlank()) {
                        scope.launch {
                            showMessage("请填写文档ID")
                        }
                        return@Button
                    }
                    
                    if (isLoading) return@Button
                    isLoading = true
                    
                    scope.launch {
                        try {
                            val response = datasetDemo.updateDocument(
                                documentId = documentId,
                                documentName = documentName.takeIf { it.isNotBlank() }
                            )
                            if (response.code == 0) {
                                showMessage("更新成功")
                            } else {
                                showMessage("更新失败：${response.msg}")
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