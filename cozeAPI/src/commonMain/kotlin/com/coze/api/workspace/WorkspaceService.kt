package com.coze.api.workspace

import com.coze.api.helper.APIBase
import com.coze.api.helper.RequestOptions
import com.coze.api.model.ApiResponse
import com.coze.api.model.workspace.WorkspaceListRequest
import com.coze.api.model.workspace.WorkspaceListResponse

/**
 * Workspace Service | 工作空间服务
 * Handles workspace operations | 处理工作空间操作
 */
class WorkspaceService : APIBase() {
    /**
     * List workspaces for current user | 获取当前用户的工作空间列表
     * @param req List request parameters | 列表请求参数
     * @param options Request options | 请求选项
     * @return WorkspaceListResponse List of workspaces | 工作空间列表
     */
    suspend fun list(
        req: WorkspaceListRequest = WorkspaceListRequest(),
        options: RequestOptions? = null
    ): ApiResponse<WorkspaceListResponse> {
        try {
            val params = mapOf( 
                "page_num" to req.pageNum.toString(),
                "page_size" to req.pageSize.toString()
            )
            return get<WorkspaceListResponse>("/v1/workspaces", options?.copy(
                params = options.params + params
            ) ?: RequestOptions(params = params))
        } catch (e: Exception) {
            throw e
        }
    }
} 