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
fun ImageScreen(
    datasetDemo: DatasetDemo,
    scaffoldState: ScaffoldState,
    scope: CoroutineScope
) {
    var datasetId by remember { mutableStateOf("") }
    var documentId by remember { mutableStateOf("") }
    var caption by remember { mutableStateOf("") }
    var keyword by remember { mutableStateOf("") }
    var hasCaption by remember { mutableStateOf(false) }
    var pageNum by remember { mutableStateOf("1") }
    var pageSize by remember { mutableStateOf("10") }
    var isLoading by remember { mutableStateOf(false) }
    var photoList by remember { mutableStateOf<List<Photo>>(emptyList()) }
    var totalCount by remember { mutableStateOf(0) }
    var imageName by remember { mutableStateOf("") }
    var fileId by remember { mutableStateOf("") }

    suspend fun showMessage(message: String) {
        scaffoldState.snackbarHostState.showSnackbar(message)
    }

    // 上传图片卡片
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = cardShape
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("上传图片", style = MaterialTheme.typography.h6)
            OutlinedTextField(
                value = datasetId,
                onValueChange = { datasetId = it },
                label = { Text("数据集ID") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = imageName,
                onValueChange = { imageName = it },
                label = { Text("图片名称") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = fileId,
                onValueChange = { fileId = it },
                label = { Text("文件ID") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    if (datasetId.isBlank() || fileId.isBlank()) {
                        scope.launch {
                            showMessage("请填写必要信息")
                        }
                        return@Button
                    }
                    
                    if (isLoading) return@Button
                    isLoading = true
                    
                    scope.launch {
                        try {
                            val response = datasetDemo.createImage(
                                datasetId = datasetId,
                                name = imageName.takeIf { it.isNotBlank() } ?: "图片.png",
                                fileId = fileId
                            )
                            response.data?.firstOrNull()?.documentId?.let { id ->
                                showMessage("上传成功：$id")
                                // 清空上传表单
                                imageName = ""
                                fileId = ""
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

    // 更新图片描述卡片
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = cardShape
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("更新图片描述", style = MaterialTheme.typography.h6)
            OutlinedTextField(
                value = datasetId,
                onValueChange = { datasetId = it },
                label = { Text("数据集ID") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = documentId,
                onValueChange = { documentId = it },
                label = { Text("图片ID") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = caption,
                onValueChange = { caption = it },
                label = { Text("图片描述") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    if (datasetId.isBlank() || documentId.isBlank() || caption.isBlank()) {
                        scope.launch {
                            showMessage("请填写必要信息")
                        }
                        return@Button
                    }
                    
                    if (isLoading) return@Button
                    isLoading = true
                    
                    scope.launch {
                        try {
                            val response = datasetDemo.updateImage(
                                datasetId = datasetId,
                                documentId = documentId,
                                caption = caption
                            )
                            response.data?.let {
                                showMessage("更新成功")
                                // 清空更新表单
                                documentId = ""
                                caption = ""
                                // 刷新列表
                                val listResponse = datasetDemo.listImages(
                                    datasetId = datasetId,
                                    pageNum = pageNum.toIntOrNull() ?: 1,
                                    pageSize = pageSize.toIntOrNull() ?: 10
                                )
                                listResponse.data?.let { data ->
                                    photoList = data.photoInfos
                                    totalCount = data.totalCount
                                }
                            } ?: showMessage("更新失败：响应数据为空")
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

    // 图片列表卡片
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = cardShape
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("图片列表", style = MaterialTheme.typography.h6)
            OutlinedTextField(
                value = datasetId,
                onValueChange = { datasetId = it },
                label = { Text("数据集ID") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = keyword,
                onValueChange = { keyword = it },
                label = { Text("关键词") },
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Checkbox(
                    checked = hasCaption,
                    onCheckedChange = { hasCaption = it }
                )
                Text("有图片描述")
            }
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
                            val response = datasetDemo.listImages(
                                datasetId = datasetId,
                                keyword = keyword.takeIf { it.isNotBlank() },
                                hasCaption = hasCaption,
                                pageNum = pageNum.toIntOrNull() ?: 1,
                                pageSize = pageSize.toIntOrNull() ?: 10
                            )
                            response.data?.let { data ->
                                showMessage("查询成功：共 ${data.totalCount} 条记录")
                                photoList = data.photoInfos
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

            if (photoList.isNotEmpty()) {
                Text(
                    "共 $totalCount 条记录",
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.primary
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    photoList.forEach { photo ->
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
                                        photo.name,
                                        style = MaterialTheme.typography.subtitle1,
                                        color = MaterialTheme.colors.primary
                                    )
                                    Text(
                                        "ID: ${photo.documentId}",
                                        style = MaterialTheme.typography.caption,
                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                                if (photo.caption.isNotEmpty()) {
                                    Text(
                                        photo.caption,
                                        style = MaterialTheme.typography.body2,
                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f)
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        "大小: ${photo.size}",
                                        style = MaterialTheme.typography.caption
                                    )
                                    Text(
                                        "类型: ${photo.type}",
                                        style = MaterialTheme.typography.caption
                                    )
                                }
                                Text(
                                    "状态: ${photo.status}",
                                    style = MaterialTheme.typography.caption,
                                    color = when (photo.status) {
                                        PhotoStatus.COMPLETED -> MaterialTheme.colors.primary
                                        PhotoStatus.IN_PROCESSING -> MaterialTheme.colors.secondary
                                        PhotoStatus.PROCESSING_FAILED -> MaterialTheme.colors.error
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
} 