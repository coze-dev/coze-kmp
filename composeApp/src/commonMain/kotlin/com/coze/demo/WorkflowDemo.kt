package com.coze.demo

import com.coze.api.model.ChatFlowData
import com.coze.api.model.EnterMessage
import com.coze.api.model.StreamChatData
import com.coze.api.model.WorkflowEventInterrupt
import com.coze.api.model.WorkflowStreamData
import com.coze.api.model.workflow.*
import com.coze.api.workflow.WorkflowService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

/**
 * Workflow Demo | 工作流演示
 * Demonstrates workflow functionality | 演示工作流功能
 */
class WorkflowDemo {
    private val workflowService = WorkflowService()
    private val defaultBotId = "7373880376026103809"
    private val defaultAppId = "7439688724318961672"

    /**
     * Test workflow stream | 测试工作流流式处理
     * @param workflowId Workflow ID | 工作流ID
     * @return Flow<String> Event messages | 事件消息流
     */
    fun streamTest(workflowId: String): Flow<String> = flow {
        val streamFlow = workflowService.stream(
            RunWorkflowReq(
                workflowId = workflowId,
                botId = defaultBotId,
                parameters = mapOf(
                    "name" to JsonPrimitive("Jason!")
                )
            )
        )
        
        try {
            streamFlow.collect { event ->
                when (event) {
                    is WorkflowStreamData.MessageEvent -> {
                        val message = event.data
                        if (message.nodeTitle.isNotEmpty()) {
                            emit("[${message.nodeTitle}] ${message.content}")
                        } else {
                            emit(message.content)
                        }
                    }
                    is WorkflowStreamData.InterruptEvent -> {
                        emit("[Interrupt] ${event.data.nodeTitle}")
                    }
                    is WorkflowStreamData.DoneEvent -> {
                        emit("[Done] Debug URL: ${event.data.debugUrl}")
                    }
                    else -> {} // Errors are handled in WorkflowService | 错误已在WorkflowService中处理
                }
            }
        } catch (e: Exception) {
            emit("[Error] ${e.message}")
        }
    }

    /**
     * Run workflow | 运行工作流
     * @param workflowId Workflow ID | 工作流ID
     * @param onMessage Message callback | 消息回调
     * @param onError Error callback | 错误回调
     * @param onComplete Completion callback | 完成回调
     * @param onInterrupt Interrupt callback | 中断回调
     * @param useStream Use streaming mode | 使用流式模式
     */
    suspend fun runWorkflow(
        workflowId: String,
        onMessage: (String) -> Unit = {},
        onError: (String) -> Unit = {},
        onComplete: () -> Unit = {},
        onInterrupt: (WorkflowEventInterrupt) -> Unit = {},
        useStream: Boolean = true
    ) {
        try {
            val req = RunWorkflowReq(
                workflowId = workflowId,
                botId = defaultBotId,
                parameters = mapOf(
                    "name" to JsonPrimitive("Jason!")
                )
            )
            
            if (!useStream) {
                val result = workflowService.create(req)
                onMessage("[Non-Stream] Workflow execution result:")
                onMessage("- Data: ${result.data}")
                onMessage("- Cost: ${result.cost}")
                onMessage("- Token: ${result.token}")
                onMessage("- Debug URL: ${result.debugUrl}")
                onComplete()
                return
            }
            
            workflowService.stream(req)
                .catch { e -> 
                    if (e !is CancellationException) {
                        onError(e.message ?: "Unknown error")
                    }
                }
                .onCompletion { cause ->
                    if (cause == null) {
                        onComplete()
                    }
                }
                .collect { event ->
                    when (event) {
                        is WorkflowStreamData.MessageEvent -> {
                            val message = event.data
                            if (message.nodeTitle.isNotEmpty()) {
                                onMessage("[${message.nodeTitle}] ${message.content}")
                            } else {
                                onMessage(message.content)
                            }
                        }
                        is WorkflowStreamData.InterruptEvent -> {
                            onInterrupt(event.data)
                        }
                        is WorkflowStreamData.DoneEvent -> {
                            onMessage("[Done] Debug URL: ${event.data.debugUrl}")
                        }
                        else -> {} // Errors are handled in WorkflowService | 错误已在WorkflowService中处理
                    }
                }
        } catch (e: Exception) {
            if (e !is CancellationException) {
                onError("Failed to run workflow: ${e.message}")
            }
        }
    }

    /**
     * Resume workflow | 恢复工作流
     * @param workflowId Workflow ID | 工作流ID
     * @param eventId Event ID | 事件ID
     * @param resumeData Resume data | 恢复数据
     * @param interruptType Interrupt type | 中断类型
     */
    suspend fun resumeWorkflow(
        workflowId: String,
        eventId: String,
        resumeData: String,
        interruptType: Int
    ) {
        val req = ResumeWorkflowReq(
            workflowId = workflowId,
            eventId = eventId,
            resumeData = resumeData,
            interruptType = interruptType
        )
        workflowService.resume(req)
    }

    /**
     * Run chat flow | 运行聊天流
     * @param workflowId Workflow ID | 工作流ID
     * @param parameters Additional parameters | 额外参数
     * @param additionalMessages Additional messages | 额外消息
     * @param onMessage Message callback | 消息回调
     * @param onError Error callback | 错误回调
     * @param onComplete Completion callback | 完成回调
     * @param onInterrupt Interrupt callback | 中断回调
     */
    suspend fun runChatFlow(
        workflowId: String,
        parameters: Map<String, JsonElement>? = null,
        additionalMessages: List<EnterMessage>,
        onMessage: (String) -> Unit = {},
        onError: (String) -> Unit = {},
        onComplete: () -> Unit = {},
        onInterrupt: (WorkflowEventInterrupt) -> Unit = {}
    ) {
        try {
            val req = ChatWorkflowReq(
                workflowId = workflowId,
                appId = defaultAppId,
                parameters = parameters,
                additionalMessages = additionalMessages
            )
            
            workflowService.chat(req)
                .catch { e -> 
                    if (e !is CancellationException) {
                        onError(e.message ?: "Unknown error")
                    }
                }
                .onCompletion { cause ->
                    if (cause == null) {
                        onComplete()
                    }
                }
                .collect { event ->
                    when (event) {
                        is ChatFlowData.WorkflowEvent -> {
                            when (val workflowData = event.data) {
                                is WorkflowStreamData.MessageEvent -> {
                                    val message = workflowData.data
                                    if (message.nodeTitle.isNotEmpty()) {
                                        onMessage("[${message.nodeTitle}] ${message.content}")
                                    } else {
                                        onMessage(message.content)
                                    }
                                }
                                is WorkflowStreamData.InterruptEvent -> {
                                    onInterrupt(workflowData.data)
                                }
                                is WorkflowStreamData.DoneEvent -> {
                                    onMessage("[Done] Debug URL: ${workflowData.data.debugUrl}")
                                }
                                else -> {} // Errors are handled in WorkflowService | 错误已在WorkflowService中处理
                            }
                        }
                        is ChatFlowData.ChatEvent -> {
                            when (val chatData = event.data) {
                                is StreamChatData.ChatMessageEvent -> {
                                    onMessage("[${chatData.event.value}] ${chatData.data.content ?: ""}")
                                }
                                is StreamChatData.CreateChatEvent -> {
                                    onMessage("[${chatData.event.value}] Chat created with ID: ${chatData.data.id}")
                                }
                                is StreamChatData.DoneEvent -> {
                                    onMessage("[${chatData.event.value}] ${chatData.data}")
                                }
                                else -> {} // Errors are handled in WorkflowService | 错误已在WorkflowService中处理
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            if (e !is CancellationException) {
                onError("Failed to run workflow chat: ${e.message}")
            }
        }
    }
} 