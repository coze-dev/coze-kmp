package com.coze.api.model.bot

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateBotReq(
    @SerialName("space_id")
    val spaceId: String,
    val name: String,
    val description: String? = null,
    @SerialName("icon_file_id")
    val iconFileId: String? = null,
    @SerialName("prompt_info")
    val promptInfo: PromptInfo? = null,
    @SerialName("onboarding_info")
    val onboardingInfo: OnboardingInfo? = null
)

@Serializable
data class UpdateBotReq(
    @SerialName("bot_id")
    val botId: String,
    val name: String,
    val description: String? = null,
    @SerialName("icon_file_id")
    val iconFileId: String? = null,
    @SerialName("prompt_info")
    val promptInfo: PromptInfo? = null,
    @SerialName("onboarding_info")
    val onboardingInfo: OnboardingInfo? = null,
    val knowledge: KnowledgeInfo? = null
)

@Serializable
data class PublishBotReq(
    @SerialName("bot_id")
    val botId: String,

    // 智能体的发布渠道 ID 列表，Agent as API: "1024"，WebSDK: "999"，...
    // 参考 https://www.coze.com/docs/developer_guides/publish_bot
    @SerialName("connector_ids")
    val connectorIds: List<String>
)

@Serializable
data class ListBotReq(
    @SerialName("space_id")
    val spaceId: String,
    @SerialName("page_size")
    val pageSize: Int? = null,
    @SerialName("page_index")
    val page: Int? = null
)

@Serializable
data class RetrieveBotReq(
    @SerialName("bot_id")
    val botId: String
)

@Serializable
data class CreateBotData(
    @SerialName("bot_id")
    val botId: String
)

@Serializable
data class ListBotData(
    val total: Int,
    @SerialName("space_bots")
    val spaceBots: List<SimpleBot>
)

@Serializable
data class SimpleBot(
    @SerialName("bot_id")
    val botId: String,
    @SerialName("bot_name")
    val botName: String,
    val description: String,
    @SerialName("icon_url")
    val iconUrl: String,
    @SerialName("publish_time")
    val publishTime: String
)

@Serializable
data class BotInfo(
    @SerialName("bot_id")
    val botId: String,
    val name: String,
    val description: String,
    @SerialName("icon_url")
    val iconUrl: String,
    @SerialName("create_time")
    val createTime: Long,
    @SerialName("update_time")
    val updateTime: Long,
    val version: String,
    @SerialName("prompt_info")
    val promptInfo: PromptInfo,
    @SerialName("onboarding_info")
    val onboardingInfo: OnboardingInfo,
    @SerialName("bot_mode")
    val botMode: Int,
    @SerialName("plugin_info_list")
    val pluginInfoList: List<BotPlugin>,
    @SerialName("model_info")
    val modelInfo: ModelInfo
)

@Serializable
data class PromptInfo(
    val prompt: String
)

@Serializable
data class OnboardingInfo(
    val prologue: String,
    @SerialName("suggested_questions")
    val suggestedQuestions: List<String>? = null
)

@Serializable
data class BotPlugin(
    @SerialName("plugin_id")
    val pluginId: String,
    val name: String,
    val description: String,
    @SerialName("icon_url")
    val iconUrl: String,
    @SerialName("api_info_list")
    val apiInfoList: List<ApiInfo>
)

@Serializable
data class ApiInfo(
    @SerialName("api_id")
    val apiId: String,
    val name: String,
    val description: String
)

@Serializable
data class ModelInfo(
    @SerialName("model_id")
    val modelId: String,
    @SerialName("model_name")
    val modelName: String
)

@Serializable
data class KnowledgeInfo(
    @SerialName("dataset_ids")
    val datasetIds: List<String>? = null,
    @SerialName("auto_call")
    val autoCall: Boolean? = null,
    @SerialName("search_strategy")
    val searchStrategy: Int? = null
)

@Serializable
data class PublishBotData(
    @SerialName("bot_id")
    val botId: String,
    val version: String
) 