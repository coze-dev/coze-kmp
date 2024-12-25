package com.coze.demo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.coze.api.model.ChatV3Message
import com.coze.api.model.bot.BotInfo
import com.coze.api.model.conversation.Conversation
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalLayoutApi::class)
@Preview
@Composable
fun App() {
    // Initialize demo instances
    val authDemo = AuthDemo
    val chatDemo = ChatDemo()
    val conversationDemo = ConversationDemo()
    val messageDemo = remember { MessageDemo() }
    val botDemo = remember { BotDemo() }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    MaterialTheme {
        // State management
        var inputText by remember { mutableStateOf("Why is the sky always blue") }
        var displayText by remember { mutableStateOf("") }
        var userMsg by remember { mutableStateOf("") }
        var greetings by remember { mutableStateOf(listOf<String>()) }
        var authResult by remember { mutableStateOf("") }
        var conversationResult by remember { mutableStateOf("") }
        var conversationId by remember { mutableStateOf("") }
        
        // Bot 相关状态
        var botName by remember { mutableStateOf("") }
        var botDescription by remember { mutableStateOf("") }
        var botId by remember { mutableStateOf("") }
        var botInfo: BotInfo? by remember { mutableStateOf<BotInfo?>(null) }
        var botResult by remember { mutableStateOf("") }
        var isCreateBotLoading by remember { mutableStateOf(false) }
        var isListBotsLoading by remember { mutableStateOf(false) }
        var isGetBotLoading by remember { mutableStateOf(false) }
        var isPublishBotLoading by remember { mutableStateOf(false) }

        // 新增状态管理
        var selectedConversationId by remember { mutableStateOf<String?>(null) }
        var selectedMessageId by remember { mutableStateOf<String?>(null) }
        var messageContent by remember { mutableStateOf("") }
        var messages by remember { mutableStateOf<List<ChatV3Message>>(emptyList()) }
        var conversations by remember { mutableStateOf<List<Conversation>>(emptyList()) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        // Loading states
        var isAuthLoading by remember { mutableStateOf(false) }
        var isChatLoading by remember { mutableStateOf(false) }
        var isListConversationsLoading by remember { mutableStateOf(false) }
        var isCreateConversationLoading by remember { mutableStateOf(false) }
        var isGetConversationLoading by remember { mutableStateOf(false) }
        var isClearConversationLoading by remember { mutableStateOf(false) }
        var isCreateMessageLoading by remember { mutableStateOf(false) }
        var isDeleteMessageLoading by remember { mutableStateOf(false) }
        var isGetMessageLoading by remember { mutableStateOf(false) }

        LaunchedEffect(greetings) {
            displayText = greetings.joinToString("\n")
        }

        // Main layout container
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 错误信息显示
                errorMessage?.let {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = 4.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = it,
                                color = androidx.compose.ui.graphics.Color.Red,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { errorMessage = null }
                            ) {
                                Text("×")
                            }
                        }
                    }
                }

                // Authentication test section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Authentication", style = MaterialTheme.typography.h6)
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    isAuthLoading = true
                                    try {
                                        authResult = authDemo.getJWTAuth() ?: ""
                                    } finally {
                                        isAuthLoading = false
                                    }
                                }
                            },
                            enabled = !isAuthLoading
                        ) {
                            if (isAuthLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colors.onPrimary
                                )
                            } else {
                                Text("Test JWT Auth")
                            }
                        }
                        if (authResult.isNotEmpty()) {
                            Text(text = "[Auth Result]\n$authResult")
                        }
                    }
                }

                // Chat section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Chat", style = MaterialTheme.typography.h6)
                        TextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            label = { Text("Chat With Coze Bot") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isChatLoading
                        )
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
                                enabled = !isChatLoading && inputText.isNotEmpty()
                            ) {
                                if (isChatLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = MaterialTheme.colors.onPrimary
                                    )
                                } else {
                                    Text("Send [Stream]")
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
                                enabled = !isChatLoading && inputText.isNotEmpty()
                            ) {
                                if (isChatLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = MaterialTheme.colors.onPrimary
                                    )
                                } else {
                                    Text("Send [Non-Stream]")
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

                // 对话管理部分
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("对话管理", style = MaterialTheme.typography.h6)
                        
                        // 对话操作区域
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
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
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                    enabled = !isListConversationsLoading
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
                                    enabled = !isCreateConversationLoading
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

                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            isGetConversationLoading = true
                                            try {
                                                val result = conversationDemo.getConversation(conversationId)
                                                val conversation = result.data
                                                conversationResult = if (conversation == null) {
                                                    "[Conversation Details]\nConversation not found"
                                                } else {
                                                    buildString {
                                                        appendLine("[Conversation Details]")
                                                        appendLine("ID: ${conversation.id}")
                                                        appendLine("Created: ${conversation.createdAt}")
                                                        appendLine("Last Section ID: ${conversation.lastSectionId ?: "N/A"}")
                                                        appendLine("Meta Data: ${conversation.metaData}")
                                                    }
                                                }
                                            } catch (e: Exception) {
                                                println("[Error] Get conversation failed: ${e.message}")
                                                errorMessage = e.message
                                            } finally {
                                                isGetConversationLoading = false
                                            }
                                        }
                                    },
                                    enabled = !isGetConversationLoading && conversationId.isNotEmpty()
                                ) {
                                    if (isGetConversationLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = MaterialTheme.colors.onPrimary
                                        )
                                    } else {
                                        Text("获取详情")
                                    }
                                }

                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            isClearConversationLoading = true
                                            try {
                                                val result = conversationDemo.clearConversation(conversationId)
                                                conversationResult = buildString {
                                                    appendLine("[Clear Result]")
                                                    appendLine("Session ID: ${result.data?.id}")
                                                    appendLine("Conversation ID: ${result.data?.conversationId}")
                                                }
                                            } catch (e: Exception) {
                                                println("[Error] Clear conversation failed: ${e.message}")
                                                errorMessage = e.message
                                            } finally {
                                                isClearConversationLoading = false
                                            }
                                        }
                                    },
                                    enabled = !isClearConversationLoading && conversationId.isNotEmpty()
                                ) {
                                    if (isClearConversationLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = MaterialTheme.colors.onPrimary
                                        )
                                    } else {
                                        Text("清除对话")
                                    }
                                }
                            }
                        }

                        // 对话操作结果显示
                        if (conversationResult.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("操作结果", style = MaterialTheme.typography.subtitle1)
                            Text(text = conversationResult)
                        }

                        // 对话列表显示
                        if (conversations.isNotEmpty()) {
                            Text("对话列表", style = MaterialTheme.typography.subtitle1)
                            LazyColumn(
                                modifier = Modifier.heightIn(max = 200.dp)
                            ) {
                                items(conversations) { conversation ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        elevation = 2.dp
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(8.dp)
                                        ) {
                                            Text("ID: ${conversation.id}")
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

                        // 消息管理
                        selectedConversationId?.let { currentConversationId ->
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("当前对话消息", style = MaterialTheme.typography.subtitle1)
                            Text("对话ID: $currentConversationId", style = MaterialTheme.typography.caption)
                            
                            // 消息输入
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
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
                                    enabled = !isCreateMessageLoading && messageContent.isNotEmpty()
                                ) {
                                    if (isCreateMessageLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = MaterialTheme.colors.onPrimary
                                        )
                                    } else {
                                        Text("发送")
                                    }
                                }
                            }

                            // 消息列表
                            if (messages.isNotEmpty()) {
                                LazyColumn(
                                    modifier = Modifier.heightIn(max = 300.dp),
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
                                                    .padding(8.dp),
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
                                                    IconButton(
                                                        onClick = {
                                                            selectedMessageId = message.id
                                                            coroutineScope.launch {
                                                                isGetMessageLoading = true
                                                                try {
                                                                    val messageDetails = messageDemo.retrieveMessage(currentConversationId, message.id)
                                                                    conversationResult = buildString {
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
                                                        },
                                                        enabled = !isGetMessageLoading
                                                    ) {
                                                        if (isGetMessageLoading && selectedMessageId == message.id) {
                                                            CircularProgressIndicator(
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                        } else {
                                                            Text("ℹ️")
                                                        }
                                                    }
                                                    IconButton(
                                                        onClick = {
                                                            selectedMessageId = message.id
                                                            coroutineScope.launch {
                                                                isDeleteMessageLoading = true
                                                                try {
                                                                    messageDemo.deleteConversationMessage(currentConversationId, message.id)
                                                                    val response = messageDemo.listConversationMessages(currentConversationId)
                                                                    messages = response.data
                                                                    errorMessage = null
                                                                } catch (e: Exception) {
                                                                    println("[Error] Delete message failed: ${e.message}")
                                                                    errorMessage = e.message
                                                                } finally {
                                                                    isDeleteMessageLoading = false
                                                                }
                                                            }
                                                        },
                                                        enabled = !isDeleteMessageLoading
                                                    ) {
                                                        if (isDeleteMessageLoading && selectedMessageId == message.id) {
                                                            CircularProgressIndicator(
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                        } else {
                                                            Text("×")
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Bot 管理部分
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("Bot 管理", style = MaterialTheme.typography.h6)
                        
                        // Bot 创建区域
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextField(
                                value = botName,
                                onValueChange = { botName = it },
                                label = { Text("Bot 名称") },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !isCreateBotLoading
                            )
                            TextField(
                                value = botDescription,
                                onValueChange = { botDescription = it },
                                label = { Text("Bot 描述") },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !isCreateBotLoading
                            )
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        isCreateBotLoading = true
                                        try {
                                            val newBotId = botDemo.createBot(botName, botDescription)
                                            botId = newBotId
                                            botResult = "Bot 创建成功: $newBotId"
                                            errorMessage = null
                                        } catch (e: Exception) {
                                            println("[Error] Create bot failed: ${e.message}")
                                            errorMessage = e.message
                                        } finally {
                                            isCreateBotLoading = false
                                        }
                                    }
                                },
                                enabled = !isCreateBotLoading && botName.isNotEmpty(),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (isCreateBotLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = MaterialTheme.colors.onPrimary
                                    )
                                } else {
                                    Text("创建 Bot")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Bot 操作区域
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextField(
                                value = botId,
                                onValueChange = { botId = it },
                                label = { Text("Bot ID") },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !isListBotsLoading && !isGetBotLoading && !isPublishBotLoading
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            isListBotsLoading = true
                                            try {
                                                val response = botDemo.listBots()
                                                botResult = buildString {
                                                    appendLine("[Bot 列表]")
                                                    appendLine("总数: ${response.total}")
                                                    response.spaceBots.forEach { bot ->
                                                        appendLine("---")
                                                        appendLine("ID: ${bot.botId}")
                                                        appendLine("名称: ${bot.botName}")
                                                        appendLine("描述: ${bot.description}")
                                                        appendLine("发布时间: ${bot.publishTime}")
                                                    }
                                                }
                                                if (response.spaceBots.isNotEmpty() == true) {
                                                    botId = response.spaceBots.first().botId
                                                }
                                                errorMessage = null
                                            } catch (e: Exception) {
                                                println("[Error] List bots failed: ${e.message}")
                                                errorMessage = e.message
                                            } finally {
                                                isListBotsLoading = false
                                            }
                                        }
                                    },
                                    enabled = !isListBotsLoading
                                ) {
                                    if (isListBotsLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = MaterialTheme.colors.onPrimary
                                        )
                                    } else {
                                        Text("列出 Bots")
                                    }
                                }

                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            isGetBotLoading = true
                                            try {
                                                botInfo = botDemo.getBot(botId)
                                                botResult = if (botInfo == null) {
                                                    "Bot 不存在"
                                                } else {
                                                    buildString {
                                                        appendLine("[Bot 详情]")
                                                        appendLine("ID: ${botInfo?.botId}")
                                                        appendLine("名称: ${botInfo?.name}")
                                                        appendLine("描述: ${botInfo?.description ?: "N/A"}")
                                                        appendLine("创建时间: ${botInfo?.createTime ?: "N/A"}")
                                                        appendLine("更新时间: ${botInfo?.updateTime ?: "N/A"}")
                                                    }
                                                }
                                                errorMessage = null
                                            } catch (e: Exception) {
                                                println("[Error] Get bot failed: ${e.message}")
                                                errorMessage = e.message
                                            } finally {
                                                isGetBotLoading = false
                                            }
                                        }
                                    },
                                    enabled = !isGetBotLoading && botId.isNotEmpty()
                                ) {
                                    if (isGetBotLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = MaterialTheme.colors.onPrimary
                                        )
                                    } else {
                                        Text("获取详情")
                                    }
                                }

                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            isPublishBotLoading = true
                                            try {
                                                val publishResult = botDemo.publishBot(botId)
                                                if (publishResult == null) {
                                                    botResult = "发布 Bot 失败！"
                                                } else {
                                                    botResult = buildString {
                                                        appendLine("[Bot 发布结果]")
                                                        appendLine("Bot ID: ${publishResult.botId}")
                                                        appendLine("版本: ${publishResult.version}")
                                                    }
                                                    errorMessage = null
                                                }
                                            } catch (e: Exception) {
                                                println("[Error] Publish bot failed: ${e.message}")
                                                errorMessage = e.message
                                            } finally {
                                                isPublishBotLoading = false
                                            }
                                        }
                                    },
                                    enabled = !isPublishBotLoading && botId.isNotEmpty()
                                ) {
                                    if (isPublishBotLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = MaterialTheme.colors.onPrimary
                                        )
                                    } else {
                                        Text("发布 Bot")
                                    }
                                }
                            }
                        }

                        // Bot 操作结果显示
                        if (botResult.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("操作结果", style = MaterialTheme.typography.subtitle1)
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = 2.dp
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .heightIn(max = 500.dp)
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    Text(text = botResult)
                                }
                            }
                        }
                    }
                }

                // 文件管理部分
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("文件管理", style = MaterialTheme.typography.h6)
                        
                        // 文件操作区域
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            var fileId by remember { mutableStateOf("") }
                            var fileResult by remember { mutableStateOf("") }
                            var isUploadFileLoading by remember { mutableStateOf(false) }
                            var isGetFileLoading by remember { mutableStateOf(false) }
                            val fileDemo = remember { FileDemo() }

                            // 文件上传按钮
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
                                                0x1f, 0x15,
                                                0xc4.toByte(),
                                                0x89.toByte(),  // CRC
                                                0x00, 0x00, 0x00, 0x0C,  // IDAT chunk length
                                                0x49, 0x44, 0x41, 0x54,  // "IDAT"
                                                0x08,
                                                0xd7.toByte(), 0x63, 0x00, 0x00, 0x00, 0x02, 0x00, 0x01,  // compressed data
                                                0x48,
                                                0x91.toByte(), 0x4b,
                                                0x86.toByte(),  // CRC
                                                0x00, 0x00, 0x00, 0x00,  // IEND chunk length
                                                0x49, 0x45, 0x4E, 0x44,  // "IEND"
                                                0xae.toByte(), 0x42, 0x60,
                                                0x82.toByte()   // CRC
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
                                enabled = !isUploadFileLoading
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

                            TextField(
                                value = fileId,
                                onValueChange = { fileId = it },
                                label = { Text("文件ID") },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !isGetFileLoading
                            )

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
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
                                    enabled = !isGetFileLoading && fileId.isNotEmpty()
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

                            // 文件操作结果显示
                            if (fileResult.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("操作结果", style = MaterialTheme.typography.subtitle1)
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    elevation = 2.dp
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .heightIn(max = 300.dp)
                                            .verticalScroll(rememberScrollState())
                                    ) {
                                        Text(text = fileResult)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

