package com.coze.demo.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.coze.api.model.EnterMessage
import com.coze.api.model.RoleType
import com.coze.api.model.WorkflowEventInterrupt
import com.coze.demo.WorkflowDemo
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive

enum class WorkflowTab(val title: String) {
    WORKFLOW("Workflow"),
    CHAT_FLOW("Chat Flow")
}

@Composable
fun WorkflowScreen() {
    var selectedTab by remember { mutableStateOf(WorkflowTab.WORKFLOW) }
    var workflowMessages by remember { mutableStateOf(listOf<String>()) }
    var chatFlowMessages by remember { mutableStateOf(listOf<String>()) }
    var isLoading by remember { mutableStateOf(false) }
    
    val coroutineScope = rememberCoroutineScope()
    val workflowDemo = remember { WorkflowDemo() }

    fun addErrorMessage(error: Throwable, isWorkflow: Boolean) {
        val messages = listOf(
            "Error occurred:",
            "- Message: ${error.message}",
            "- Type: ${error::class.simpleName}",
            "- Stack trace: ${error.stackTraceToString()}"
        )
        if (isWorkflow) {
            workflowMessages = workflowMessages + messages
        } else {
            chatFlowMessages = chatFlowMessages + messages
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Tabs
        TabRow(
            selectedTabIndex = selectedTab.ordinal,
            backgroundColor = MaterialTheme.colors.surface,
            contentColor = MaterialTheme.colors.primary
        ) {
            WorkflowTab.values().forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = { Text(tab.title) }
                )
            }
        }

        // Content based on selected tab
        Box(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            when (selectedTab) {
                WorkflowTab.WORKFLOW -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        WorkflowTabContent(
                            messages = workflowMessages,
                            isLoading = isLoading,
                            onAddMessage = { workflowMessages = workflowMessages + it },
                            onSetLoading = { isLoading = it },
                            onError = { addErrorMessage(it, true) },
                            workflowDemo = workflowDemo
                        )
                        
                        // Workflow messages list
                        Card(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            elevation = 4.dp,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize().padding(8.dp)
                            ) {
                                items(workflowMessages) { message ->
                                    Text(
                                        text = message,
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                WorkflowTab.CHAT_FLOW -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ChatFlowTabContent(
                            messages = chatFlowMessages,
                            isLoading = isLoading,
                            onAddMessage = { chatFlowMessages = chatFlowMessages + it },
                            onSetLoading = { isLoading = it },
                            onError = { addErrorMessage(it, false) },
                            workflowDemo = workflowDemo
                        )
                        
                        // Chat flow messages list
                        Card(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            elevation = 4.dp,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize().padding(8.dp)
                            ) {
                                items(chatFlowMessages) { message ->
                                    Text(
                                        text = message,
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkflowTabContent(
    messages: List<String>,
    isLoading: Boolean,
    onAddMessage: (String) -> Unit,
    onSetLoading: (Boolean) -> Unit,
    onError: (Throwable) -> Unit,
    workflowDemo: WorkflowDemo
) {
    var workflowId by remember { mutableStateOf("7452610533675532296") }
    var currentInterrupt by remember { mutableStateOf<WorkflowEventInterrupt?>(null) }
    var resumeData by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = workflowId,
            onValueChange = { workflowId = it },
            label = { Text("Workflow ID") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    coroutineScope.launch {
                        onSetLoading(true)
                        onAddMessage("Running workflow (non-stream)...")
                        currentInterrupt = null
                        try {
                            workflowDemo.runWorkflow(
                                workflowId = workflowId,
                                onMessage = { onAddMessage(it) },
                                onError = { error -> onError(Exception(error)) },
                                onComplete = { onAddMessage("Workflow completed") },
                                onInterrupt = { interrupt -> 
                                    currentInterrupt = interrupt
                                    onAddMessage("Workflow interrupted: ${interrupt.nodeTitle}")
                                },
                                useStream = false
                            )
                        } catch (e: Exception) {
                            onError(e)
                        } finally {
                            onSetLoading(false)
                        }
                    }
                },
                enabled = workflowId.isNotEmpty() && !isLoading,
                modifier = Modifier.weight(1f)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colors.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Run [Non-Stream]")
                }
            }

            Button(
                onClick = {
                    coroutineScope.launch {
                        onSetLoading(true)
                        onAddMessage("Running workflow (stream)...")
                        currentInterrupt = null
                        try {
                            workflowDemo.runWorkflow(
                                workflowId = workflowId,
                                onMessage = { onAddMessage(it) },
                                onError = { error -> onError(Exception(error)) },
                                onComplete = { onAddMessage("Workflow completed") },
                                onInterrupt = { interrupt -> 
                                    currentInterrupt = interrupt
                                    onAddMessage("Workflow interrupted: ${interrupt.nodeTitle}")
                                }
                            )
                        } catch (e: Exception) {
                            onError(e)
                        } finally {
                            onSetLoading(false)
                        }
                    }
                },
                enabled = workflowId.isNotEmpty() && !isLoading,
                modifier = Modifier.weight(1f)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colors.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Run [Stream]")
                }
            }
        }

        if (currentInterrupt != null) {
            OutlinedTextField(
                value = resumeData,
                onValueChange = { resumeData = it },
                label = { Text("Resume Data") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    coroutineScope.launch {
                        currentInterrupt?.let { interrupt ->
                            onSetLoading(true)
                            try {
                                workflowDemo.resumeWorkflow(
                                    workflowId = workflowId,
                                    eventId = interrupt.interruptData.eventId,
                                    resumeData = resumeData,
                                    interruptType = interrupt.interruptData.type
                                )
                                onAddMessage("Workflow resumed")
                                currentInterrupt = null
                                resumeData = ""
                            } catch (e: Exception) {
                                onError(e)
                            } finally {
                                onSetLoading(false)
                            }
                        }
                    }
                },
                enabled = resumeData.isNotEmpty() && !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colors.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Resume Workflow")
                }
            }
        }
    }
}

@Composable
private fun ChatFlowTabContent(
    messages: List<String>,
    isLoading: Boolean,
    onAddMessage: (String) -> Unit,
    onSetLoading: (Boolean) -> Unit,
    onError: (Throwable) -> Unit,
    workflowDemo: WorkflowDemo
) {
    var workflowChatId by remember { mutableStateOf("7455160128556482578") }
    var name by remember { mutableStateOf("Jason") }
    var userMessage by remember { mutableStateOf("Hi") }
    var currentInterrupt by remember { mutableStateOf<WorkflowEventInterrupt?>(null) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = workflowChatId,
            onValueChange = { workflowChatId = it },
            label = { Text("聊天工作流 ID") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Your Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = userMessage,
            onValueChange = { userMessage = it },
            label = { Text("Your Message") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                if (name.isBlank()) {
                    onAddMessage("Please enter your name first")
                    return@Button
                }
                if (userMessage.isBlank()) {
                    onAddMessage("Please enter your message first")
                    return@Button
                }
                coroutineScope.launch {
                    onSetLoading(true)
                    onAddMessage("Starting Chat flow...")
                    currentInterrupt = null
                    try {
                        workflowDemo.runChatFlow(
                            workflowId = workflowChatId,
                            parameters = mapOf(
                                "name" to JsonPrimitive(name)
                            ),
                            additionalMessages = listOf(
                                EnterMessage(role = RoleType.USER, content = userMessage)
                            ),
                            onMessage = { onAddMessage(it) },
                            onError = { error -> onError(Exception(error)) },
                            onComplete = { 
                                onAddMessage("Chat completed")
                                userMessage = ""
                            },
                            onInterrupt = { interrupt -> 
                                currentInterrupt = interrupt
                                onAddMessage("Chat interrupted: ${interrupt.nodeTitle}")
                            }
                        )
                    } catch (e: Exception) {
                        onError(e)
                    } finally {
                        onSetLoading(false)
                    }
                }
            },
            enabled = workflowChatId.isNotEmpty() && !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colors.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Start ChatFlow")
            }
        }
    }
} 