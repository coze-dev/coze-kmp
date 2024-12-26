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
import com.coze.api.model.WorkflowEventInterrupt
import com.coze.demo.WorkflowDemo
import kotlinx.coroutines.launch

@Composable
fun WorkflowScreen() {
    var workflowId by remember { mutableStateOf("7452610533675532296") }
    var messages by remember { mutableStateOf(listOf<String>()) }
    var currentInterrupt by remember { mutableStateOf<WorkflowEventInterrupt?>(null) }
    var resumeData by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    
    val coroutineScope = rememberCoroutineScope()
    val workflowDemo = remember { WorkflowDemo() }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = workflowId,
            onValueChange = { workflowId = it },
            label = { Text("Workflow ID") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 按钮区域
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 非流式运行按钮
            Button(
                onClick = {
                    coroutineScope.launch {
                        isLoading = true
                        messages = listOf("Running workflow (non-stream)...")
                        currentInterrupt = null
                        try {
                            workflowDemo.runWorkflow(
                                workflowId = workflowId,
                                onMessage = { messages = messages + it },
                                onError = { messages = messages + "Error: $it" },
                                onComplete = { messages = messages + "Workflow completed" },
                                onInterrupt = { interrupt -> 
                                    currentInterrupt = interrupt
                                    messages = messages + "Workflow interrupted: ${interrupt.nodeTitle}"
                                },
                                useStream = false
                            )
                        } finally {
                            isLoading = false
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

            // 流式运行按钮
            Button(
                onClick = {
                    coroutineScope.launch {
                        isLoading = true
                        messages = listOf("Running workflow (stream)...")
                        currentInterrupt = null
                        try {
                            workflowDemo.runWorkflow(
                                workflowId = workflowId,
                                onMessage = { messages = messages + it },
                                onError = { messages = messages + "Error: $it" },
                                onComplete = { messages = messages + "Workflow completed" },
                                onInterrupt = { interrupt -> 
                                    currentInterrupt = interrupt
                                    messages = messages + "Workflow interrupted: ${interrupt.nodeTitle}"
                                },
                                useStream = true
                            )
                        } finally {
                            isLoading = false
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

        Spacer(modifier = Modifier.height(16.dp))

        // 显示中断恢复输入框
        if (currentInterrupt != null) {
            TextField(
                value = resumeData,
                onValueChange = { resumeData = it },
                label = { Text("Resume Data") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    coroutineScope.launch {
                        currentInterrupt?.let { interrupt ->
                            isLoading = true
                            try {
                                workflowDemo.resumeWorkflow(
                                    workflowId = workflowId,
                                    eventId = interrupt.interruptData.eventId,
                                    resumeData = resumeData,
                                    interruptType = interrupt.interruptData.type
                                )
                                messages = messages + "Workflow resumed"
                                currentInterrupt = null
                                resumeData = ""
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                },
                enabled = resumeData.isNotEmpty() && !isLoading
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

            Spacer(modifier = Modifier.height(16.dp))
        }

        // 消息列表
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = 4.dp,
            shape = RoundedCornerShape(8.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(8.dp)
            ) {
                items(messages) { message ->
                    Text(
                        text = message,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
} 