package com.coze.demo.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.coze.demo.FileDemo
import com.coze.demo.ui.components.ErrorMessage
import kotlinx.coroutines.launch

@Composable
fun FileScreen() {
    val fileDemo = remember { FileDemo() }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var fileId by remember { mutableStateOf("") }
    var fileResult by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isUploadFileLoading by remember { mutableStateOf(false) }
    var isGetFileLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 错误信息显示
        errorMessage?.let {
            ErrorMessage(
                message = it,
                onDismiss = { errorMessage = null }
            )
        }

        // 文件上传区域
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("文件上传", style = MaterialTheme.typography.h6)
                Button(
                    onClick = {
                        coroutineScope.launch {
                            isUploadFileLoading = true
                            try {
                                // 创建一个简单的测试图片内容（一个1x1的黑色PNG图片的字节数组）
                                val pngBytes = byteArrayOf(
                                    0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,  // PNG signature
                                    0x00, 0x00, 0x00, 0x0D,  // IHDR chunk length
                                    0x49, 0x48, 0x44, 0x52,  // "IHDR"
                                    0x00, 0x00, 0x00, 0x01,  // width=1
                                    0x00, 0x00, 0x00, 0x01,  // height=1
                                    0x08,  // bit depth
                                    0x06,  // color type
                                    0x00,  // compression method
                                    0x00,  // filter method
                                    0x00,  // interlace method
                                    0x1f, 0x15, 0xc4.toByte(), 0x89.toByte(),  // CRC
                                    0x00, 0x00, 0x00, 0x0C,  // IDAT chunk length
                                    0x49, 0x44, 0x41, 0x54,  // "IDAT"
                                    0x08, 0xd7.toByte(), 0x63, 0x00, 0x00, 0x00, 0x02, 0x00, 0x01,  // compressed data
                                    0x48, 0x91.toByte(), 0x4b, 0x86.toByte(),  // CRC
                                    0x00, 0x00, 0x00, 0x00,  // IEND chunk length
                                    0x49, 0x45, 0x4E, 0x44,  // "IEND"
                                    0xae.toByte(), 0x42, 0x60, 0x82.toByte()   // CRC
                                )
                                
                                println("[DEBUG] 上传文件内容大小: ${pngBytes.size} bytes")
                                
                                val response = fileDemo.uploadFile(
                                    fileName = "test.png",
                                    mimeType = "image/png",
                                    content = pngBytes
                                )
                                fileResult = if (response == null) {
                                    "文件上传失败"
                                } else {
                                    buildString {
                                        appendLine("[文件上传结果]")
                                        appendLine("ID: ${response.id}")
                                        appendLine("文件名: ${response.fileName}")
                                        appendLine("大小: ${response.bytes} bytes")
                                        appendLine("创建时间: ${response.createdAt}")
                                    }
                                }
                                fileId = response?.id ?: ""
                                errorMessage = null
                            } catch (e: Exception) {
                                println("[Error] Upload file failed: ${e.message}")
                                errorMessage = e.message
                            } finally {
                                isUploadFileLoading = false
                            }
                        }
                    },
                    enabled = !isUploadFileLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isUploadFileLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colors.onPrimary
                        )
                    } else {
                        Text("上传测试图片")
                    }
                }
            }
        }

        // 文件查询区域
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("文件查询", style = MaterialTheme.typography.h6)
                TextField(
                    value = fileId,
                    onValueChange = { fileId = it },
                    label = { Text("文件ID") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isGetFileLoading
                )
                Button(
                    onClick = {
                        coroutineScope.launch {
                            isGetFileLoading = true
                            try {
                                val response = fileDemo.getFile(fileId)
                                fileResult = if (response == null) {
                                    "文件不存在"
                                } else {
                                    buildString {
                                        appendLine("[文件信息]")
                                        appendLine("ID: ${response.id}")
                                        appendLine("文件名: ${response.fileName}")
                                        appendLine("大小: ${response.bytes} bytes")
                                        appendLine("创建时间: ${response.createdAt}")
                                    }
                                }
                                errorMessage = null
                            } catch (e: Exception) {
                                println("[Error] Get file failed: ${e.message}")
                                errorMessage = e.message
                            } finally {
                                isGetFileLoading = false
                            }
                        }
                    },
                    enabled = !isGetFileLoading && fileId.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isGetFileLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colors.onPrimary
                        )
                    } else {
                        Text("获取文件信息")
                    }
                }
            }
        }

        // 操作结果显示
        if (fileResult.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("操作结果", style = MaterialTheme.typography.h6)
                    Text(text = fileResult)
                }
            }
        }
    }
} 