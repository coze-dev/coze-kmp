package com.coze.api.workflow

import com.coze.api.helper.APIBase
import com.coze.api.helper.RequestOptions
import com.coze.api.model.ApiResponse
import com.coze.api.model.WorkflowStreamData
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
                emit(workflowData)
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
} 