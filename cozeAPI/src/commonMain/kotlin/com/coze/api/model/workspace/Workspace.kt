package com.coze.api.model.workspace

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WorkspaceListResponse(
    val workspaces: List<Workspace>,
    @SerialName("total_count")
    val totalCount: Int
)

@Serializable
data class Workspace(
    val id: String,
    val name: String,
    @SerialName("icon_url")
    val iconUrl: String,
    @SerialName("role_type")
    val roleType: String,
    @SerialName("workspace_type")
    val workspaceType: String
)

@Serializable
data class WorkspaceListRequest(
    @SerialName("page_num")
    val pageNum: Int = 1,
    @SerialName("page_size")
    val pageSize: Int = 10
) 