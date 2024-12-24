package com.coze.demo

import com.coze.api.bot.BotService
import com.coze.api.model.bot.*

class BotDemo {
    private val botService = BotService()
    private val spaceId = "7435957313455783944"

    /**
     * 创建一个新的Bot
     */
    suspend fun createBot(name: String, description: String? = null): String {
        val req = CreateBotReq(
            spaceId = spaceId,
            name = name,
            description = description
        )
        val response = botService.create(req).data
        println("Created bot with ID: ${response?.botId}")
        return response?.botId?:""
    }

    /**
     * 更新Bot的配置
     */
    suspend fun updateBot(botId: String, name: String, description: String? = null) {
        val req = UpdateBotReq(
            botId = botId,
            name = name,
            description = description
        )
        botService.update(req)
        println("Updated bot: $botId")
    }

    /**
     * 获取Bot列表
     */
    suspend fun listBots(page: Int = 1, pageSize: Int = 10): ListBotData {
        val req = ListBotReq(
            spaceId = spaceId,
            page = page,
            pageSize = pageSize
        )
        val response = botService.list(req).data
        println("Total bots: ${response?.total}")
        response?.spaceBots?.forEach { bot ->
            println("Bot ID: ${bot.botId}, Name: ${bot.botName}, Published: ${bot.publishTime}")
        }
        return response!!
    }

    /**
     * 发布Bot为API服务
     */
    suspend fun publishBot(botId: String): PublishBotData? {
        val req = PublishBotReq(botId, listOf("1024"))
        val response = botService.publish(req)
        println("Publish response: ${response}")
        println("Published bot: $botId, version: ${response.data?.version}")
        return response.data
    }

    /**
     * 获取Bot的配置信息
     */
    suspend fun getBot(botId: String): BotInfo {
        val req = RetrieveBotReq(botId = botId)
        val botInfo = botService.retrieve(req).data
        println("Retrieved bot, Name: ${botInfo?.name}, Description: ${botInfo?.description}")
        return botInfo!!
    }
} 