package com.coze.api.workflow

import com.coze.api.helper.APIBase
import com.coze.api.helper.RequestOptions
import com.coze.api.model.ApiResponse
import com.coze.api.model.ChatFlowData
import com.coze.api.model.StreamChatData
import com.coze.api.model.WorkflowEventDone
import com.coze.api.model.WorkflowEventMessage
import com.coze.api.model.WorkflowStreamData
import com.coze.api.model.sseEvent2ChatData
import com.coze.api.model.sseEvent2ChatFlowData
import com.coze.api.model.sseEvent2WorkflowData
import com.coze.api.model.workflow.*
import io.ktor.sse.ServerSentEvent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class WorkflowService : APIBase() {
    /**
     * 启动工作流运行
     */
    suspend fun create(
        params: RunWorkflowReq,
        options: RequestOptions? = null
    ): RunWorkflowData {
        val payload = params.copy(stream = false)
        try {
            return getClient().post<RunWorkflowData>("/v1/workflow/run", payload, options)
        } catch (e: Exception) {
            println("[Workflow] Create failed: ${e.message}")
            throw e
        }
    }

    /**
     * 流式传输工作流运行事件
     */
    fun stream(
        params: RunWorkflowReq,
        options: RequestOptions? = null
    ): Flow<WorkflowStreamData> = flow {
        val payload = params.copy(stream = true)
        
        var eventFlow: Flow<ServerSentEvent>? = null
        try {
            eventFlow = sse("/v1/workflow/stream_run", payload, options ?: RequestOptions())
            eventFlow.collect { event ->
                val workflowData = sseEvent2WorkflowData(event)
                when (workflowData) {
                    is WorkflowStreamData.ErrorEvent -> {
                        println("[Workflow] Error: ${workflowData.data.errorCode}: ${workflowData.data.errorMessage}")
                        throw Exception("Workflow error: ${workflowData.data.errorMessage}")
                    }
                    is WorkflowStreamData.CommonErrorEvent -> {
                        println("[Workflow] Error: ${workflowData.data.code}: ${workflowData.data.msg}")
                        throw Exception("Workflow error: ${workflowData.data.msg}")
                    }
                    else -> emit(workflowData)
                }
                if (workflowData is WorkflowStreamData.DoneEvent) {
                    return@collect
                }
            }
        } catch (e: Exception) {
            if (e !is CancellationException) {
                println("[Workflow] Stream error: ${e.message}")
                throw e
            }
        } finally {
            try {
                (eventFlow as? AutoCloseable)?.close()
            } catch (e: Exception) {
                println("[Workflow] Failed to close stream: ${e.message}")
            }
        }
    }

    /**
     * 恢复暂停的工作流运行
     */
    suspend fun resume(
        params: ResumeWorkflowReq,
        options: RequestOptions? = null
    ): ApiResponse<WorkflowStreamData> {
        try {
            return post<WorkflowStreamData>("/v1/workflow/stream_resume", params, options)
        } catch (e: Exception) {
            println("[Workflow] Resume failed: ${e.message}")
            throw e
        }
    }

    /**
     * ChatFlow 
     */
    fun chat(
        params: ChatWorkflowReq,
        options: RequestOptions? = null
    ): Flow<ChatFlowData> = flow {
        var eventFlow: Flow<ServerSentEvent>? = null
        try {
            eventFlow = sse("/v1/workflows/chat", params, options ?: RequestOptions())
            eventFlow.collect { event ->
                val flowData = sseEvent2ChatFlowData(event)
                println("[Workflow] Flow data: $flowData")
                when (flowData) {
                    is ChatFlowData.WorkflowEvent -> {
                        when (val workflowData = flowData.data) {
                            is WorkflowStreamData.ErrorEvent -> {
                                println("[Workflow] Error: ${workflowData.data.errorCode}: ${workflowData.data.errorMessage}")
                                throw Exception("Workflow error: ${workflowData.data.errorMessage}")
                            }
                            is WorkflowStreamData.CommonErrorEvent -> {
                                println("[Workflow] Error: ${workflowData.data.code}: ${workflowData.data.msg}")
                                throw Exception("Workflow error: ${workflowData.data.msg}")
                            }
                            else -> emit(flowData)
                        }
                    }
                    is ChatFlowData.ChatEvent -> {
                        when (val chatData = flowData.data) {
                            is StreamChatData.ErrorEvent -> {
                                println("[Workflow] Chat Error: ${chatData.data.code}: ${chatData.data.msg}")
                                throw Exception("Workflow chat error: ${chatData.data.msg}")
                            }
                            else -> emit(flowData)
                        }
                    }
                }
                
                // 检查是否完成
                if ((flowData is ChatFlowData.ChatEvent && flowData.data is StreamChatData.DoneEvent) || 
                    (flowData is ChatFlowData.WorkflowEvent && flowData.data is WorkflowStreamData.DoneEvent)) {
                    return@collect
                }
            }
        } catch (e: Exception) {
            if (e !is CancellationException) {
                println("[Workflow] Chat stream error: ${e.message}")
                throw e
            }
        } finally {
            try {
                (eventFlow as? AutoCloseable)?.close()
            } catch (e: Exception) {
                println("[Workflow] Failed to close chat stream: ${e.message}")
            }
        }
    }
} 