package com.coze.api.chat

import com.coze.api.helper.APIBase
import com.coze.api.helper.RequestOptions
import com.coze.api.model.chat.*
import com.coze.api.model.ApiResponse
import com.coze.api.model.EventType
import com.coze.api.model.ChatStatus
import com.coze.api.model.ChatV3Message
import com.coze.api.model.EnterMessage
import com.coze.api.model.StreamChatData
import com.coze.api.model.sseEvent2ChatData
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlin.random.Random
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

const val POLL_INTERVAL: Long = 1000
const val POLL_TIMEOUT: Long = 60000

/**
 * Generate UUID for chat identification | 生成用于聊天识别的UUID
 * @return String Random UUID | 随机UUID字符串
 */
fun generateUUID(): String = 
    (Random.nextDouble() * Clock.System.now().toEpochMilliseconds()).toString()

/**
 * Chat Service | 聊天服务
 * Handles chat operations including creation, polling, streaming and tool outputs | 处理聊天操作，包括创建、轮询、流式传输和工具输出
 */
class ChatService : APIBase() {

    /**
     * Create a new chat | 创建新的聊天
     * @param params Chat creation parameters | 聊天创建参数
     * @param options Request options | 请求选项
     * @return CreateChatData Chat creation result | 聊天创建结果
     */
    suspend fun createChat(
        params: CreateChatReq, 
        options: RequestOptions? = null
    ): ApiResponse<CreateChatData> {
        require(params.botId.isNotBlank()) { "botId cannot be empty" }
        require(!params.additionalMessages.isNullOrEmpty()) { "additionalMessages cannot be empty" }
        
        params.userId = params.userId.takeUnless { it.isNullOrEmpty() } ?: generateUUID()
        val apiUrl = "/v3/chat${params.conversationId?.let { "?conversation_id=$it" } ?: ""}"
        val payload = params.copy(
            additionalMessages = handleAdditionalMessages(params.additionalMessages),
            stream = false
        )
        return post(apiUrl, payload, options)
    }

    /**
     * Create chat and poll for completion | 创建聊天并轮询完成状态
     * @param params Chat creation parameters | 聊天创建参数
     * @param options Request options | 请求选项
     * @return CreateChatPollData Chat result with messages | 带消息的聊天结果
     */
    suspend fun createAndPollChat(
        params: CreateChatReq, 
        options: RequestOptions? = null
    ): CreateChatPollData {
        require(params.botId.isNotBlank()) { "botId cannot be empty" }
        require(!params.additionalMessages.isNullOrEmpty()) { "additionalMessages cannot be empty" }
        
        params.userId = params.userId.takeUnless { it.isNullOrEmpty() } ?: generateUUID()
        val apiUrl = "/v3/chat${params.conversationId?.let { "?conversation_id=$it" } ?: ""}"
        val payload = params.copy(
            additionalMessages = handleAdditionalMessages(params.additionalMessages),
            stream = false
        )
        val response = post<CreateChatData>(
            path = apiUrl,
            payload = payload,
            options = options
        )
        val chatId = response.data?.id ?: throw Exception("创建聊天失败：返回数据为空")
        val conversationId = response.data.conversationId
        var chat: CreateChatData?

        val startTime = Clock.System.now()
        while (true) {
            delay(POLL_INTERVAL)
            chat = retrieveChat(conversationId, chatId).data
            if (chat != null) {
                if (chat.status in listOf(ChatStatus.COMPLETED, ChatStatus.FAILED, ChatStatus.REQUIRES_ACTION)) {
                    break
                }
            }
            if (Clock.System.now().toEpochMilliseconds() - startTime.toEpochMilliseconds() > POLL_TIMEOUT) {
                throw Exception("轮询超时")
            }
        }

        val nonNullChat = requireNotNull(chat) { "The Response Chat Message data should not be null" }
        val messageList = listMessages(conversationId, chatId).data
        return CreateChatPollData(nonNullChat, messageList)
    }

    /**
     * Retrieve chat information | 获取聊天信息
     * @param conversationId Conversation ID | 会话ID
     * @param chatId Chat ID | 聊天ID
     * @param options Request options | 请求选项
     * @return CreateChatData Chat information | 聊天信息
     */
    private suspend fun retrieveChat(
        conversationId: String, 
        chatId: String, 
        options: RequestOptions? = null
    ): ApiResponse<CreateChatData> {
        require(conversationId.isNotBlank()) { "conversationId cannot be empty" }
        require(chatId.isNotBlank()) { "chatId cannot be empty" }
        
        val apiUrl = "/v3/chat/retrieve?conversation_id=$conversationId&chat_id=$chatId"
        return post<CreateChatData>(
            path = apiUrl,
            payload = null,
            options = options
        )
    }

    /**
     * Cancel an ongoing chat | 取消进行中的聊天
     * @param conversationId Conversation ID | 会话ID
     * @param chatId Chat ID | 聊天ID
     * @param options Request options | 请求选项
     * @return CreateChatData Cancelled chat information | 已取消的聊天信息
     */
    suspend fun cancelChat(
        conversationId: String, 
        chatId: String, 
        options: RequestOptions? = null
    ): ApiResponse<CreateChatData> {
        require(conversationId.isNotBlank()) { "conversationId cannot be empty" }
        require(chatId.isNotBlank()) { "chatId cannot be empty" }
        
        return post<CreateChatData>("/v3/chat/cancel", mapOf(
            "conversation_id" to conversationId,
            "chat_id" to chatId
        ), options)
    }

    /**
     * List messages in a chat | 列出聊天中的消息
     * @param conversationId Conversation ID | 会话ID
     * @param chatId Chat ID | 聊天ID
     * @return List<ChatV3Message> List of chat messages | 聊天消息列表
     */
    private suspend fun listMessages(
        conversationId: String, 
        chatId: String,
    ): ApiResponse<List<ChatV3Message>> {
        require(conversationId.isNotBlank()) { "conversationId cannot be empty" }
        require(chatId.isNotBlank()) { "chatId cannot be empty" }
        
        return get<List<ChatV3Message>>("/v3/chat/message/list", 
        RequestOptions(
            params = mapOf(
                "conversation_id" to conversationId,
                "chat_id" to chatId
            )
        ))
    }

    /**
     * Handle additional messages processing | 处理额外消息
     * @param additionalMessages List of messages to process | 要处理的消息列表
     * @return List<EnterMessage> Processed messages | 处理后的消息列表
     */
    private fun handleAdditionalMessages(additionalMessages: List<EnterMessage>?): List<EnterMessage>? {
        return additionalMessages?.map { it.copy(content = it.content ?: "") }
    }

    /**
     * Submit tool outputs | 提交工具输出
     * @param params Tool outputs parameters | 工具输出参数
     * @param options Request options | 请求选项
     * @return CreateChatData Chat result | 聊天结果
     */
    suspend fun submitToolOutputs(
        params: SubmitToolOutputsReq,
        options: RequestOptions? = null
    ): ApiResponse<CreateChatData> {
        require(params.conversationId.isNotBlank()) { "conversationId cannot be empty" }
        require(params.chatId.isNotBlank()) { "chatId cannot be empty" }
        require(params.toolOutputs.isNotEmpty()) { "toolOutputs cannot be empty" }
        
        val apiUrl = "/v3/chat/submit_tool_outputs?conversation_id=${params.conversationId}&chat_id=${params.chatId}"
        return post(apiUrl, params, options)
    }

    /**
     * Submit tool outputs with streaming | 以流式方式提交工具输出
     * @param params Tool outputs parameters | 工具输出参数
     * @param options Request options | 请求选项
     * @return Flow<StreamChatData> Stream of chat data | 聊天数据流
     */
    suspend fun submitToolOutputsStream(
        params: SubmitToolOutputsReq,
        options: RequestOptions? = null
    ): Flow<StreamChatData> = flow {
        require(params.conversationId.isNotBlank()) { "conversationId cannot be empty" }
        require(params.chatId.isNotBlank()) { "chatId cannot be empty" }
        require(params.toolOutputs.isNotEmpty()) { "toolOutputs cannot be empty" }
        
        val apiUrl = "/v3/chat/submit_tool_outputs?conversation_id=${params.conversationId}&chat_id=${params.chatId}"
        val eventFlow = sse(apiUrl, params, options ?: RequestOptions())
        eventFlow.collect { event ->
            val chatData = sseEvent2ChatData(event)
            emit(chatData)
            if (chatData.event == EventType.DONE) {
                return@collect
            }
        }
    }

    /**
     * Stream chat messages | 流式传输聊天消息
     * @param params Stream chat parameters | 流式聊天参数
     * @param options Request options | 请求选项
     * @return Flow<StreamChatData> Stream of chat data | 聊天数据流
     */
    fun stream(
        params: StreamChatReq, 
        options: RequestOptions? = null
    ): Flow<StreamChatData> = flow {
        require(params.botId.isNotBlank()) { "botId cannot be empty" }
        require(!params.additionalMessages.isNullOrEmpty()) { "additionalMessages cannot be empty" }
        
        params.userId = params.userId.takeUnless { it.isNullOrEmpty() } ?: generateUUID()
        val apiUrl = "/v3/chat${params.conversationId?.let { "?conversation_id=$it" } ?: ""}"
        val payload = params.copy(
            additionalMessages = handleAdditionalMessages(params.additionalMessages),
            stream = true
        )

        val eventFlow = sse(apiUrl, payload, options ?: RequestOptions())
        eventFlow.collect { event ->
            val chatData = sseEvent2ChatData(event)
            emit(chatData)
            if (chatData.event == EventType.DONE) {
                return@collect
            }
        }
    }
}
