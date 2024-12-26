package com.coze.demo

import com.coze.api.model.WorkflowEventInterrupt
import com.coze.api.model.WorkflowStreamData
import com.coze.api.model.workflow.*
import com.coze.api.workflow.WorkflowService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.serialization.json.JsonPrimitive

class WorkflowDemo {
    private val workflowService = WorkflowService()
    private val defaultBotId = "7373880376026103809"

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
                is WorkflowStreamData.ErrorEvent -> {
                    emit("[Error] ${event.data.errorCode}: ${event.data.errorMessage}")
                }
                is WorkflowStreamData.InterruptEvent -> {
                    emit("[Interrupt] ${event.data.nodeTitle}")
                }
                is WorkflowStreamData.DoneEvent -> {
                    emit("[Done] Debug URL: ${event.data.debugUrl}")
                }
            }
        }
    }

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
            
            val result = workflowService.create(req)
            
            if (useStream) {
                workflowService.stream(req)
                    .catch { e -> 
                        if (e !is CancellationException) {
                            onError("Error: ${e.message}")
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
                            is WorkflowStreamData.ErrorEvent -> {
                                onError("Error ${event.data.errorCode}: ${event.data.errorMessage}")
                            }
                            is WorkflowStreamData.InterruptEvent -> {
                                onInterrupt(event.data)
                            }
                            is WorkflowStreamData.DoneEvent -> {
                                onMessage("[Done] Debug URL: ${event.data.debugUrl}")
                            }
                        }
                    }
            } else {
                onComplete()
            }
        } catch (e: Exception) {
            if (e !is CancellationException) {
                onError("Failed to run workflow: ${e.message}")
            }
        }
    }

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
} 