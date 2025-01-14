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

/**
 * Workflow Service | 工作流服务
 * Handles workflow operations including running, streaming, and chat | 处理工作流操作，包括运行、流式传输和聊天
 */
class WorkflowService : APIBase() {
    /**
     * Start a workflow run | 启动工作流运行
     * @param params Workflow run parameters | 工作流运行参数
     * @param options Request options | 请求选项
     * @return RunWorkflowData Workflow run result | 工作流运行结果
     */
    suspend fun create(
        params: RunWorkflowReq,
        options: RequestOptions? = null
    ): RunWorkflowData {
        // Parameter validation | 参数验证
        require(params.workflowId.isNotBlank()) { "workflowId cannot be empty" }

        val payload = params.copy(stream = false)
        try {
            return getClient().post<RunWorkflowData>("/v1/workflow/run", payload, options)
        } catch (e: Exception) {
            throw e
        }
    }

    /**
     * Stream workflow run events | 流式传输工作流运行事件
     * @param params Workflow run parameters | 工作流运行参数
     * @param options Request options | 请求选项
     * @return Flow<WorkflowStreamData> Stream of workflow events | 工作流事件流
     */
    fun stream(
        params: RunWorkflowReq,
        options: RequestOptions? = null
    ): Flow<WorkflowStreamData> = flow {
        // Parameter validation | 参数验证
        require(params.workflowId.isNotBlank()) { "workflowId cannot be empty" }

        val payload = params.copy(stream = true)
        
        var eventFlow: Flow<ServerSentEvent>? = null
        try {
            eventFlow = sse("/v1/workflow/stream_run", payload, options ?: RequestOptions())
            eventFlow.collect { event ->
                val workflowData = sseEvent2WorkflowData(event)
                when (workflowData) {
                    is WorkflowStreamData.ErrorEvent -> {
                        throw Exception("Workflow error: ${workflowData.data.errorMessage}")
                    }
                    is WorkflowStreamData.CommonErrorEvent -> {
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
                throw e
            }
        } finally {
            try {
                (eventFlow as? AutoCloseable)?.close()
            } catch (e: Exception) {
                // Ignore close errors
            }
        }
    }

    /**
     * Resume a paused workflow run | 恢复暂停的工作流运行
     * @param params Resume workflow parameters | 恢复工作流参数
     * @param options Request options | 请求选项
     * @return WorkflowStreamData Workflow stream data | 工作流流数据
     */
    suspend fun resume(
        params: ResumeWorkflowReq,
        options: RequestOptions? = null
    ): ApiResponse<WorkflowStreamData> {
        // Parameter validation | 参数验证
        require(params.workflowId.isNotBlank()) { "workflowId cannot be empty" }
        require(params.eventId.isNotBlank()) { "eventId cannot be empty" }

        try {
            return post<WorkflowStreamData>("/v1/workflow/stream_resume", params, options)
        } catch (e: Exception) {
            throw e
        }
    }

    /**
     * Start a chat workflow | 启动聊天工作流
     * @param params Chat workflow parameters | 聊天工作流参数
     * @param options Request options | 请求选项
     * @return Flow<ChatFlowData> Stream of chat flow data | 聊天流数据
     */
    fun chat(
        params: ChatWorkflowReq,
        options: RequestOptions? = null
    ): Flow<ChatFlowData> = flow {
        // Parameter validation | 参数验证
        require(params.workflowId.isNotBlank()) { "workflowId cannot be empty" }
        require(params.additionalMessages.isNotEmpty()) { "additionalMessages cannot be empty" }

        var eventFlow: Flow<ServerSentEvent>? = null
        try {
            eventFlow = sse("/v1/workflows/chat", params, options ?: RequestOptions())
            eventFlow.collect { event ->
                val flowData = sseEvent2ChatFlowData(event)
                when (flowData) {
                    is ChatFlowData.WorkflowEvent -> {
                        when (val workflowData = flowData.data) {
                            is WorkflowStreamData.ErrorEvent -> {
                                throw Exception("Workflow error: ${workflowData.data.errorMessage}")
                            }
                            is WorkflowStreamData.CommonErrorEvent -> {
                                throw Exception("Workflow error: ${workflowData.data.msg}")
                            }
                            else -> emit(flowData)
                        }
                    }
                    is ChatFlowData.ChatEvent -> {
                        when (val chatData = flowData.data) {
                            is StreamChatData.ErrorEvent -> {
                                throw Exception("Workflow chat error: ${chatData.data.msg}")
                            }
                            else -> emit(flowData)
                        }
                    }
                }
                
                // Check if completed | 检查是否完成
                if ((flowData is ChatFlowData.ChatEvent && flowData.data is StreamChatData.DoneEvent) || 
                    (flowData is ChatFlowData.WorkflowEvent && flowData.data is WorkflowStreamData.DoneEvent)) {
                    return@collect
                }
            }
        } catch (e: Exception) {
            if (e !is CancellationException) {
                throw e
            }
        } finally {
            try {
                (eventFlow as? AutoCloseable)?.close()
            } catch (e: Exception) {
                // Ignore close errors
            }
        }
    }
} 