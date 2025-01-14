package com.coze.api.bot

import com.coze.api.helper.APIBase
import com.coze.api.helper.RequestOptions
import com.coze.api.model.ApiResponse
import com.coze.api.model.bot.*

/**
 * Bot Service | 智能体服务
 * Handles bot operations including creation, update, listing and publishing | 处理智能体操作，包括创建、更新、列表和发布
 */
class BotService : APIBase() {
    /**
     * Create a new bot | 创建新的智能体
     * @param params Bot creation parameters | 创建Bot的参数
     * @param options Request options | 请求选项
     * @return CreateBotData Created bot information | 创建的Bot信息
     */
    suspend fun create(
        params: CreateBotReq,
        options: RequestOptions? = null
    ): ApiResponse<CreateBotData> {
        // Parameter validation | 参数验证
        require(params.spaceId.isNotBlank()) { "spaceId cannot be empty" }
        require(params.name.isNotBlank()) { "name cannot be empty" }

        return post<CreateBotData>("/v1/bot/create", params, options)
    }

    /**
     * Update bot configuration | 修改智能体的配置
     * @param params Bot update parameters | 修改Bot的参数
     * @param options Request options | 请求选项
     */
    suspend fun update(
        params: UpdateBotReq,
        options: RequestOptions? = null
    ) {
        // Parameter validation | 参数验证
        require(params.botId.isNotBlank()) { "botId cannot be empty" }
        require(params.name.isNotBlank()) { "name cannot be empty" }

        post<Unit>("/v1/bot/update", params, options)
    }

    /**
     * List bots published as API in specified space | 查看指定空间发布到Agent as API渠道的智能体列表
     * @param params Bot listing parameters | 列出Bot的参数
     * @param options Request options | 请求选项
     * @return ListBotData Bot list data | Bot列表数据
     */
    suspend fun list(
        params: ListBotReq,
        options: RequestOptions? = null
    ): ApiResponse<ListBotData> {
        // Parameter validation | 参数验证
        require(params.spaceId.isNotBlank()) { "spaceId cannot be empty" }

        val queryParams = mapOf(
            "space_id" to params.spaceId,
            "page" to (params.page ?: 1).toString(),
            "page_size" to (params.pageSize ?: 50).toString()
        )
        val requestOptions = options?.copy(params = queryParams) 
            ?: RequestOptions(params = queryParams)
        return get("/v1/space/published_bots_list", requestOptions)
    }

    /**
     * Publish bot as API service | 发布指定智能体为API服务
     * @param params Bot publishing parameters | 发布Bot的参数
     * @param options Request options | 请求选项
     * @return PublishBotData Published bot data | 已发布的Bot数据
     */
    suspend fun publish(
        params: PublishBotReq,
        options: RequestOptions? = null
    ): ApiResponse<PublishBotData> {
        // Parameter validation | 参数验证
        require(params.botId.isNotBlank()) { "botId cannot be empty" }
        require(params.connectorIds.isNotEmpty()) { "connectorIds cannot be empty" }

        return post<PublishBotData>("/v1/bot/publish", params, options)
    }

    /**
     * Retrieve bot configuration | 获取指定智能体的配置信息
     * @param params Bot retrieval parameters | 获取Bot的参数
     * @param options Request options | 请求选项
     * @return BotInfo Bot configuration information | Bot的配置信息
     */
    suspend fun retrieve(
        params: RetrieveBotReq,
        options: RequestOptions? = null
    ): ApiResponse<BotInfo> {
        // Parameter validation | 参数验证
        require(params.botId.isNotBlank()) { "botId cannot be empty" }

        val queryParams = mapOf(
            "bot_id" to params.botId
        )
        val requestOptions = options?.copy(params = queryParams) 
            ?: RequestOptions(params = queryParams)
        return get<BotInfo>("/v1/bot/get_online_info", requestOptions)
    }
} 