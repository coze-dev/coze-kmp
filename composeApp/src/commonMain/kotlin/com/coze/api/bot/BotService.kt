package com.coze.api.bot

import com.coze.api.helper.APIBase
import com.coze.api.helper.RequestOptions
import com.coze.api.model.ApiResponse
import com.coze.api.model.bot.*

class BotService : APIBase() {
    /**
     * 创建一个新的智能体
     * @param params 创建Bot的参数
     * @param options 请求选项
     * @return 创建的Bot信息
     */
    suspend fun create(
        params: CreateBotReq,
        options: RequestOptions? = null
    ): ApiResponse<CreateBotData> {
        // 参数验证
        require(params.spaceId.isNotBlank()) { "spaceId cannot be empty" }
        require(params.name.isNotBlank()) { "name cannot be empty" }

        return post<CreateBotData>("/v1/bot/create", params, options)
    }

    /**
     * 修改智能体的配置
     * @param params 修改Bot的参数
     * @param options 请求选项
     */
    suspend fun update(
        params: UpdateBotReq,
        options: RequestOptions? = null
    ) {
        // 参数验证
        require(params.botId.isNotBlank()) { "botId cannot be empty" }
        require(params.name.isNotBlank()) { "name cannot be empty" }

        post<Unit>("/v1/bot/update", params, options)
    }

    /**
     * 查看指定空间发布到Agent as API渠道的智能体列表
     * @param params 列出Bot的参数
     * @param options 请求选项
     * @return Bot列表数据
     */
    suspend fun list(
        params: ListBotReq,
        options: RequestOptions? = null
    ): ApiResponse<ListBotData> {
        // 参数验证
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
     * 发布指定智能体为API服务
     * @param params 发布Bot的参数
     * @param options 请求选项
     */
    suspend fun publish(
        params: PublishBotReq,
        options: RequestOptions? = null
    ): ApiResponse<PublishBotData> {
        // 参数验证
        require(params.botId.isNotBlank()) { "botId cannot be empty" }
        require(params.connectorIds.isNotEmpty()) { "connectorIds cannot be empty" }

        return post<PublishBotData>("/v1/bot/publish", params, options)
    }

    /**
     * 获取指定智能体的配置信息
     * @param params 获取Bot的参数
     * @param options 请求选项
     * @return Bot的配置信息
     */
    suspend fun retrieve(
        params: RetrieveBotReq,
        options: RequestOptions? = null
    ): ApiResponse<BotInfo> {
        // 参数验证
        require(params.botId.isNotBlank()) { "botId cannot be empty" }

        val queryParams = mapOf(
            "bot_id" to params.botId
        )
        val requestOptions = options?.copy(params = queryParams) 
            ?: RequestOptions(params = queryParams)
        return get<BotInfo>("/v1/bot/get_online_info", requestOptions)
    }
} 