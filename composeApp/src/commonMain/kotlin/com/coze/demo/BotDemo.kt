package com.coze.demo

import com.coze.api.bot.BotService
import com.coze.api.model.bot.*

/**
 * Bot Demo | 机器人演示
 * Demonstrates bot management functionality | 演示机器人管理功能
 */
class BotDemo {
    private val botService = BotService()
    private val spaceId = "7435957313455783944"

    /**
     * Create a new bot | 创建新机器人
     * @param name Bot name | 机器人名称
     * @param description Bot description | 机器人描述
     * @return String Bot ID | 机器人ID
     */
    suspend fun createBot(name: String, description: String? = null): String {
        val req = CreateBotReq(
            spaceId = spaceId,
            name = name,
            description = description
        )
        return botService.create(req).data?.botId ?: ""
    }

    /**
     * Update bot configuration | 更新机器人配置
     * @param botId Bot ID | 机器人ID
     * @param name New bot name | 新的机器人名称
     * @param description New bot description | 新的机器人描述
     */
    suspend fun updateBot(botId: String, name: String, description: String? = null) {
        val req = UpdateBotReq(
            botId = botId,
            name = name,
            description = description
        )
        botService.update(req)
    }

    /**
     * List all bots | 列出所有机器人
     * @param page Page number | 页码
     * @param pageSize Page size | 每页大小
     * @return ListBotData List of bots | 机器人列表
     */
    suspend fun listBots(page: Int = 1, pageSize: Int = 10): ListBotData {
        val req = ListBotReq(
            spaceId = spaceId,
            page = page,
            pageSize = pageSize
        )
        return botService.list(req).data!!
    }

    /**
     * Publish bot as API service | 发布机器人为API服务
     * @param botId Bot ID | 机器人ID
     * @return PublishBotData? Published bot data | 发布的机器人数据
     */
    suspend fun publishBot(botId: String): PublishBotData? {
        val req = PublishBotReq(botId, listOf("1024"))
        return botService.publish(req).data
    }

    /**
     * Get bot configuration | 获取机器人配置
     * @param botId Bot ID | 机器人ID
     * @return BotInfo Bot information | 机器人信息
     */
    suspend fun getBot(botId: String): BotInfo {
        val req = RetrieveBotReq(botId = botId)
        return botService.retrieve(req).data!!
    }
} 