package com.coze.api.workspace

import com.coze.api.helper.APIBase
import com.coze.api.helper.RequestOptions
import com.coze.api.model.ApiResponse
import com.coze.api.model.workspace.WorkspaceListRequest
import com.coze.api.model.workspace.WorkspaceListResponse

class WorkspaceService : APIBase() {
    /**
     * 查看当前扣子用户加入的空间列表
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
            println("[Workspace] List failed: ${e.message}")
            throw e
        }
    }
} 