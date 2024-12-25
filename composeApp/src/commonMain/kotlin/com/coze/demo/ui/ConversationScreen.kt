package com.coze.demo.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.coze.api.model.ChatV3Message
import com.coze.api.model.conversation.Conversation
import com.coze.demo.ConversationDemo
import com.coze.demo.MessageDemo
import com.coze.demo.ui.components.ErrorMessage
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

@Composable
fun ConversationScreen() {
    val conversationDemo = remember { ConversationDemo() }
    val messageDemo = remember { MessageDemo() }
    val coroutineScope = rememberCoroutineScope()

    var conversationId by remember { mutableStateOf("") }
    var conversationResult by remember { mutableStateOf("") }
    var conversations by remember { mutableStateOf<List<Conversation>>(emptyList()) }
    var selectedConversationId by remember { mutableStateOf<String?>(null) }
    var messages by remember { mutableStateOf<List<ChatV3Message>>(emptyList()) }
    var messageContent by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedMessageDetails by remember { mutableStateOf<String?>(null) }
    var isGetMessageLoading by remember { mutableStateOf(false) }

    // Loading states
    var isListConversationsLoading by remember { mutableStateOf(false) }
    var isCreateConversationLoading by remember { mutableStateOf(false) }
    var isGetConversationLoading by remember { mutableStateOf(false) }
    var isClearConversationLoading by remember { mutableStateOf(false) }
    var isCreateMessageLoading by remember { mutableStateOf(false) }

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

        // 对话ID输入
        TextField(
            value = conversationId,
            onValueChange = { conversationId = it },
            label = { Text("对话ID") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isListConversationsLoading && !isCreateConversationLoading && 
                     !isGetConversationLoading && !isClearConversationLoading
        )

        // 对话操作按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    coroutineScope.launch {
                        isListConversationsLoading = true
                        try {
                            val response = conversationDemo.listConversations()
                            conversations = response.data?.conversations ?: emptyList()
                            if (conversations.isNotEmpty()) {
                                conversationId = conversations.first().id
                            }
                            errorMessage = null
                        } catch (e: Exception) {
                            println("[Error] ${e.message}")
                            errorMessage = e.message
                        } finally {
                            isListConversationsLoading = false
                        }
                    }
                },
                enabled = !isListConversationsLoading,
                modifier = Modifier.weight(1f)
            ) {
                if (isListConversationsLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colors.onPrimary
                    )
                } else {
                    Text("列出对话")
                }
            }

            Button(
                onClick = {
                    coroutineScope.launch {
                        isCreateConversationLoading = true
                        try {
                            val timestamp = Clock.System.now().toEpochMilliseconds()
                            val response = conversationDemo.createConversation(
                                messages = null,
                                metaData = mapOf(
                                    "created_time" to timestamp.toString(),
                                    "source" to "demo_app"
                                )
                            )
                            selectedConversationId = response.data?.id
                            errorMessage = null
                            
                            val conversation = response.data
                            conversationResult = if (conversation == null) {
                                "[Create Conversation]\nFailed to create conversation"
                            } else {
                                buildString {
                                    appendLine("[Create Conversation]")
                                    appendLine("ID: ${conversation.id}")
                                    appendLine("Created: ${conversation.createdAt}")
                                    appendLine("Meta Data: ${conversation.metaData}")
                                }
                            }
                        } catch (e: Exception) {
                            println("[Error] Create conversation failed: ${e.message}")
                            errorMessage = e.message
                        } finally {
                            isCreateConversationLoading = false
                        }
                    }
                },
                enabled = !isCreateConversationLoading,
                modifier = Modifier.weight(1f)
            ) {
                if (isCreateConversationLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colors.onPrimary
                    )
                } else {
                    Text("创建对话")
                }
            }
        }

        // 对话列表显示
        if (conversations.isNotEmpty()) {
            Text("对话列表", style = MaterialTheme.typography.subtitle1)
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(conversations) { conversation ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("ID: ${conversation.id}")
                            Text("创建时间: ${conversation.createdAt}")
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        selectedConversationId = conversation.id
                                        coroutineScope.launch {
                                            try {
                                                val response = messageDemo.listConversationMessages(conversation.id)
                                                messages = response.data
                                                errorMessage = null
                                            } catch (e: Exception) {
                                                println("[Error] List messages failed: ${e.message}")
                                                errorMessage = e.message
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("查看消息")
                                }
                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            isClearConversationLoading = true
                                            try {
                                                conversationDemo.clearConversation(conversation.id)
                                                val response = conversationDemo.listConversations()
                                                conversations = response.data?.conversations ?: emptyList()
                                                errorMessage = null
                                            } catch (e: Exception) {
                                                println("[Error] Clear conversation failed: ${e.message}")
                                                errorMessage = e.message
                                            } finally {
                                                isClearConversationLoading = false
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    enabled = !isClearConversationLoading
                                ) {
                                    if (isClearConversationLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = MaterialTheme.colors.onPrimary
                                        )
                                    } else {
                                        Text("清除")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 消息列表显示
        selectedConversationId?.let { currentConversationId ->
            Text("当前对话消息", style = MaterialTheme.typography.subtitle1)
            Text("对话ID: $currentConversationId", style = MaterialTheme.typography.caption)
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextField(
                    value = messageContent,
                    onValueChange = { messageContent = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("输入消息内容") },
                    enabled = !isCreateMessageLoading
                )
                Button(
                    onClick = {
                        coroutineScope.launch {
                            isCreateMessageLoading = true
                            try {
                                messageDemo.createConversationMessage(
                                    conversationId = currentConversationId,
                                    content = messageContent
                                )
                                messageContent = ""
                                val response = messageDemo.listConversationMessages(currentConversationId)
                                messages = response.data
                                errorMessage = null
                            } catch (e: Exception) {
                                println("[Error] Create message failed: ${e.message}")
                                errorMessage = e.message
                            } finally {
                                isCreateMessageLoading = false
                            }
                        }
                    },
                    enabled = !isCreateMessageLoading && messageContent.isNotEmpty(),
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colors.secondary,
                        contentColor = MaterialTheme.colors.onSecondary,
                        disabledBackgroundColor = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
                    )
                ) {
                    if (isCreateMessageLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colors.onSecondary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("发送")
                    }
                }
            }

            if (messages.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 2.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("暂无消息")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages) { message ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = 2.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "角色: ${message.role}",
                                        style = MaterialTheme.typography.caption
                                    )
                                    Text(message.content)
                                }
                                Row {
                                    // 查看消息详情按钮
                                    IconButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                isGetMessageLoading = true
                                                try {
                                                    val messageDetails = messageDemo.retrieveMessage(currentConversationId, message.id)
                                                    selectedMessageDetails = buildString {
                                                        appendLine("[消息详情]")
                                                        appendLine("ID: ${messageDetails.id}")
                                                        appendLine("角色: ${messageDetails.role}")
                                                        appendLine("内容类型: ${messageDetails.contentType}")
                                                        appendLine("创建时间: ${messageDetails.createdAt}")
                                                        appendLine("更新时间: ${messageDetails.updatedAt}")
                                                        appendLine("内容: ${messageDetails.content}")
                                                    }
                                                    errorMessage = null
                                                } catch (e: Exception) {
                                                    println("[Error] Retrieve message failed: ${e.message}")
                                                    errorMessage = e.message
                                                } finally {
                                                    isGetMessageLoading = false
                                                }
                                            }
                                        }
                                    ) {
                                        if (isGetMessageLoading) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp)
                                            )
                                        } else {
                                            Text("ℹ️")
                                        }
                                    }
                                    // 删除消息按钮
                                    IconButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                try {
                                                    messageDemo.deleteConversationMessage(currentConversationId, message.id)
                                                    val response = messageDemo.listConversationMessages(currentConversationId)
                                                    messages = response.data
                                                    errorMessage = null
                                                } catch (e: Exception) {
                                                    println("[Error] Delete message failed: ${e.message}")
                                                    errorMessage = e.message
                                                }
                                            }
                                        }
                                    ) {
                                        Text("×")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 消息详情显示
            selectedMessageDetails?.let { details ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    elevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("消息详情", style = MaterialTheme.typography.h6)
                            IconButton(onClick = { selectedMessageDetails = null }) {
                                Text("×")
                            }
                        }
                        Text(text = details)
                    }
                }
            }
        }
    }
} 