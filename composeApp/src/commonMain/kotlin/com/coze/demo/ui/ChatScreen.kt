package com.coze.demo.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.coze.demo.ChatDemo
import com.coze.demo.ui.components.ErrorMessage
import kotlinx.coroutines.launch

@Composable
fun ChatScreen() {
    val chatDemo = remember { ChatDemo() }
    val coroutineScope = rememberCoroutineScope()
    
    var inputText by remember { mutableStateOf("Why is the sky blue?") }
    var userMsg by remember { mutableStateOf("") }
    var greetings by remember { mutableStateOf(listOf<String>()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isChatLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 错误信息显示
        errorMessage?.let {
            ErrorMessage(
                message = it,
                onDismiss = { errorMessage = null }
            )
        }

        // 输入区域
        TextField(
            value = inputText,
            onValueChange = { inputText = it },
            label = { Text("Chat With Coze Bot") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isChatLoading
        )

        // 按钮区域
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Stream chat button
            Button(
                onClick = {
                    coroutineScope.launch {
                        isChatLoading = true
                        try {
                            userMsg = inputText
                            inputText = ""
                            greetings = listOf("[Coze Response (Stream)]\n")
                            chatDemo.streamTest(userMsg).collect { phrase ->
                                val lastText = greetings.last()
                                greetings = greetings.dropLast(1) + (lastText + phrase)
                            }
                        } catch (e: Exception) {
                            println("[Error] Stream chat failed: ${e.message}")
                            errorMessage = e.message
                        } finally {
                            isChatLoading = false
                        }
                    }
                },
                enabled = !isChatLoading && inputText.isNotEmpty(),
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.primary,
                    contentColor = MaterialTheme.colors.onPrimary,
                    disabledBackgroundColor = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
                )
            ) {
                if (isChatLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colors.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("发送 [Stream]")
                }
            }

            // Non-stream chat button
            Button(
                onClick = {
                    coroutineScope.launch {
                        isChatLoading = true
                        try {
                            userMsg = inputText
                            inputText = ""
                            greetings = listOf("[Coze Response (Non-Stream)]\n")
                            chatDemo.noneStreamCreateAndPoll(userMsg).collect { phrase ->
                                val lastText = greetings.last()
                                greetings = greetings.dropLast(1) + (lastText + phrase)
                            }
                        } catch (e: Exception) {
                            println("[Error] Non-stream chat failed: ${e.message}")
                            errorMessage = e.message
                        } finally {
                            isChatLoading = false
                        }
                    }
                },
                enabled = !isChatLoading && inputText.isNotEmpty(),
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.primary,
                    contentColor = MaterialTheme.colors.onPrimary,
                    disabledBackgroundColor = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
                )
            ) {
                if (isChatLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colors.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("发送 [Non-Stream]")
                }
            }
        }

        // 显示用户消息
        if (userMsg.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(text = "[User Message]\n$userMsg")
                }
            }
        }

        // 显示聊天响应
        if (greetings.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .heightIn(max = 300.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(8.dp)
                ) {
                    Text(text = greetings.last())
                }
            }
        }
    }
} 