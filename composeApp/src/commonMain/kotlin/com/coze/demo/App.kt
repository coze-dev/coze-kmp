package com.coze.demo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

@OptIn(ExperimentalLayoutApi::class)
@Preview
@Composable
fun App() {
    // Initialize demo instances
    val authDemo = AuthDemo
    val chatDemo = ChatDemo()
    val conversationDemo = ConversationDemo()
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
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Stream chat button
                            Button(onClick = {
                                coroutineScope.launch {
                                    userMsg = inputText
                                    inputText = ""
                                    var text = "[Coze Response (Stream)]\n"
                                    chatDemo.streamTest(userMsg).collect { phrase ->
                                        text += phrase
                                        greetings = greetings.dropLast(1) + text
                                        displayText = greetings.joinToString("\n")
                                    }
                                }
                            }) {
                                Text("Send [Stream]")
                            }
                            // Non-stream chat button
                            Button(onClick = {
                                coroutineScope.launch {
                                    userMsg = inputText
                                    inputText = ""
                                    var text = "[Coze Response (Non-Stream)]\n"
                                    chatDemo.noneStreamCreateAndPoll(userMsg).collect { phrase ->
                                        text += phrase
                                        greetings = greetings.dropLast(1) + text
                                        displayText = greetings.joinToString("\n")
                                    }
                                }
                            }) {
                                Text("Send [Non-Stream]")
                            }
                        }
                    }
                }

                // User message display
                if (userMsg.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = 4.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "[User Msg]\n$userMsg")
                        }
                    }
                }

                // Chat response display
                if (displayText.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = 4.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .heightIn(max = 500.dp)
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp)
                        ) {
                            Text(text = displayText)
                        }
                    }
                }

                // Conversation management section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Conversation Management", style = MaterialTheme.typography.h6)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // List conversations button
                            Button(onClick = {
                                coroutineScope.launch {
                                    try {
                                        val result = conversationDemo.listConversations()
                                        val conversations = result.data?.conversations
                                        conversationResult = if (conversations.isNullOrEmpty()) {
                                            "[Conversations List]\nNo conversations found"
                                        } else {
                                            conversationId = conversations.firstOrNull()?.id ?: ""
                                            
                                            buildString {
                                                appendLine("[Conversations List]")
                                                appendLine("Has more: ${result.data.hasMore}")
                                                appendLine("---")
                                                conversations.forEach { conv ->
                                                    appendLine("ID: ${conv.id}")
                                                    appendLine("Created: ${conv.createdAt}")
                                                    appendLine("Last Section ID: ${conv.lastSectionId ?: "N/A"}")
                                                    appendLine("Meta Data: ${conv.metaData}")
                                                    appendLine("---")
                                                }
                                            }
                                        }
                                    } catch (e: Exception) {
                                        conversationResult = "[Error] ${e.message}"
                                    }
                                }
                            }) {
                                Text("List Conversations")
                            }

                            // Create conversation button
                            Button(onClick = {
                                coroutineScope.launch {
                                    try {
                                        val timestamp = Clock.System.now().toEpochMilliseconds()
                                        val metaData = mapOf(
                                            "created_time" to timestamp.toString(),
                                            "source" to "demo_app"
                                        )
                                        val result = conversationDemo.createConversation(
                                            metaData = metaData
                                        )
                                        val conversation = result.data
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
                                        conversationResult = "[Error] ${e.message}"
                                    }
                                }
                            }) {
                                Text("Create Conversation")
                            }

                            // Conversation operations row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TextField(
                                    value = conversationId,
                                    onValueChange = { conversationId = it },
                                    label = { Text("Conversation ID") },
                                    modifier = Modifier.weight(1f)
                                )
                                // Get conversation button
                                Button(onClick = {
                                    coroutineScope.launch {
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
                                            conversationResult = "[Error] ${e.message}"
                                        }
                                    }
                                }) {
                                    Text("Get")
                                }
                                // Clear conversation button
                                Button(onClick = {
                                    coroutineScope.launch {
                                        try {
                                            val result = conversationDemo.clearConversation(conversationId)
                                            conversationResult = buildString {
                                                appendLine("[Clear Result]")
                                                appendLine("Session ID: ${result.data?.id}")
                                                appendLine("Conversation ID: ${result.data?.conversationId}")
                                            }
                                        } catch (e: Exception) {
                                            conversationResult = "[Error] ${e.message}"
                                        }
                                    }
                                }) {
                                    Text("Clear")
                                }
                            }
                        }
                    }
                }

                // Conversation result display
                if (conversationResult.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = 4.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .heightIn(max = 300.dp)
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp)
                        ) {
                            Text(text = conversationResult)
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
                        Button(onClick = {
                            coroutineScope.launch {
                                authResult = authDemo.getJWTAuth() ?: ""
                            }
                        }) {
                            Text("Test JWT Auth")
                        }
                        if (authResult.isNotEmpty()) {
                            Text(text = "[Auth Result]\n$authResult")
                        }
                    }
                }
            }
        }
    }
}

