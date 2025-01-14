# Coze Kotlin Multiplatform API SDK

[![Kotlin Multiplatform](https://img.shields.io/badge/Kotlin-Multiplatform-blue.svg)](https://www.jetbrains.com/kotlin-multiplatform/)

## 简介

这是一个面向 Android 和 iOS 的 Coze API 的 Kotlin Multiplatform 项目。该 SDK 提供了一种强大的方式来将 Coze 的开放 API 集成到你的 Kotlin Multiplatform 项目中。

主要特性:

* 完整支持 Coze 开放 API 和认证 API
* 支持使用 Kotlin 协程和 Flow 进行异步操作
* 跨平台支持 (Android 和 iOS)
* 优化的流式 API，返回 Flow 对象
* 简单直观的 API 设计

## 项目结构

* `/cozeAPI` - 核心 API SDK 模块，包含所有 API 实现。
  - `commonMain` - 所有平台的通用代码：
    - API 服务：
      - `bot` - 机器人管理服务
      - `chat` - 聊天和消息服务
      - `conversation` - 对话管理服务
      - `dataset` - 数据集和文档服务
      - `file` - 文件上传服务
      - `workflow` - 工作流执行服务
      - `workspace` - 工作空间管理服务
  - `androidMain` - Android 平台特定实现
  - `iosMain` - iOS 平台特定实现

* `/composeApp` - 演示应用，展示 SDK 使用方法。
  - `commonMain` - 跨平台演示代码：
    - `com.coze.demo` - 各个 API 功能的演示实现：
      - `AuthDemo.kt` - 认证示例
      - `BotDemo.kt` - 机器人管理示例
      - `ChatDemo.kt` - 聊天功能示例
      - `ConversationDemo.kt` - 对话管理示例
      - `DatasetDemo.kt` - 数据集操作示例
      - `FileDemo.kt` - 文件处理示例
      - `WorkflowDemo.kt` - 工作流执行示例
      - `WorkspaceDemo.kt` - 工作空间管理示例
  - `androidMain` - Android 平台特定演示代码
  - `iosMain` - iOS 平台特定演示代码

* `/iosApp` - iOS 演示应用。
  - 原生 iOS 应用入口点
  - 展示在 iOS 环境中的 SDK 使用方法

## 项目依赖
本项目使用 [Kotlin Multiplatform](https://www.jetbrains.com/kotlin-multiplatform/) 框架。

## 认证

要使用 Coze API,您需要实现一个提供认证令牌的 `TokenService` 接口。SDK 使用此服务为所有 API 调用处理认证。

```kotlin
interface TokenService {
    /**
     * 从服务获取令牌
     * @return TokenInfo 包含过期时间的令牌信息
     */
    suspend fun getToken(): TokenInfo
}
```

`TokenInfo` 类包含:
```kotlin
data class TokenInfo(
    val token: String?,      // 访问令牌
    val expiresIn: Long      // 过期时间(秒)
)
```

您可以通过以下几种方式实现此接口:

1. **个人访问令牌 (PAT)**
```kotlin
class PATTokenService(private val token: String) : TokenService {
    override suspend fun getToken(): TokenInfo {
        // 返回 PAT 和默认过期时间
        return TokenInfo(token = token, expiresIn = 3600)
    }
}
```

2. **JWT 令牌**
```kotlin
class JWTTokenService(
    private val appId: String,
    private val keyId: String,
    private val privateKey: String
) : TokenService {
    override suspend fun getToken(): TokenInfo {
        // 实现 JWT 令牌生成逻辑
        val jwtToken = generateJWTToken(
            appId = appId,
            keyId = keyId,
            privateKey = privateKey
        )
        return TokenInfo(token = jwtToken.accessToken, expiresIn = jwtToken.expiresIn)
    }
}
```

3. **OAuth 令牌**
```kotlin
class OAuthTokenService(
    private val appId: String,
    private val appSecret: String
) : TokenService {
    override suspend fun getToken(): TokenInfo {
        // 实现 OAuth 令牌获取逻辑
        val tokenData = getOAuthToken(
            appId = appId,
            appSecret = appSecret
        )
        return TokenInfo(token = tokenData.accessToken, expiresIn = tokenData.expiresIn)
    }
}
```

然后使用您的令牌服务初始化 SDK:

```kotlin
// 使用 PAT 初始化
val patService = PATTokenService("your-pat-token")
TokenManager.init(patService)

// 或使用 JWT 初始化
val jwtService = JWTTokenService(
    appId = "your-app-id",
    keyId = "your-key-id",
    privateKey = "your-private-key"
)
TokenManager.init(jwtService)

// 或使用 OAuth 初始化
val oauthService = OAuthTokenService(
    appId = "your-app-id",
    appSecret = "your-app-secret"
)
TokenManager.init(oauthService)
```

SDK 将自动处理令牌缓存和更新。在以下情况下会刷新令牌:
- 令牌为空
- 令牌即将过期(过期前 30 秒)
- 请求强制刷新

## 使用示例

SDK 为各种用例提供了全面的示例：

### 示例列表

| 示例                       | 文件                                                                                                                                      |
| ----------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------- |
| Token 认证                    | [AuthDemo.kt](/composeApp/src/commonMain/kotlin/com/coze/demo/AuthDemo.kt)                           |
| 机器人创建和发布              | [BotDemo.kt](/composeApp/src/commonMain/kotlin/com/coze/demo/BotDemo.kt)                          |
| 非流式聊天                    | [ChatDemo.kt](/composeApp/src/commonMain/kotlin/com/coze/demo/ChatDemo.kt)                                     |
| 流式聊天                      | [ChatDemo.kt](/composeApp/src/commonMain/kotlin/com/coze/demo/ChatDemo.kt)                         |
| 对话管理                      | [ConversationDemo.kt](/composeApp/src/commonMain/kotlin/com/coze/demo/ConversationDemo.kt)                                     |
| 数据集管理                    | [DatasetDemo.kt](/composeApp/src/commonMain/kotlin/com/coze/demo/DatasetDemo.kt)                   |
| 文件上传和管理                | [FileDemo.kt](/composeApp/src/commonMain/kotlin/com/coze/demo/FileDemo.kt)                                     |
| 工作流执行                    | [WorkflowDemo.kt](/composeApp/src/commonMain/kotlin/com/coze/demo/WorkflowDemo.kt)                   |
| 工作空间管理                  | [WorkspaceDemo.kt](/composeApp/src/commonMain/kotlin/com/coze/demo/WorkspaceDemo.kt)              |

### 机器人操作
- 创建和发布机器人
- 列出和获取机器人信息
- 更新机器人配置

### 聊天操作
- 非流式聊天
- 流式聊天
- 图片聊天
- 本地插件聊天

### 工作流操作
- 运行工作流
- 流式执行工作流
- 处理工作流中断

### 数据集操作
- 创建/更新/删除数据集
- 列出数据集
- 文档管理
- 图片管理

### 对话管理
- 创建/更新对话
- 列出对话
- 消息操作

### 文件操作
- 上传文件
- 获取文件信息

### 工作空间操作
- 列出工作空间
- 工作空间管理

## 代码示例

### 聊天示例

```kotlin
// 创建流式聊天
val chatService = ChatService()
chatService.stream(StreamChatReq(
    botId = "your-bot-id",
    additionalMessages = listOf(EnterMessage("讲个笑话"))
)).collect { chatData ->
    println(chatData)
}

// 创建非流式聊天
chatService.createAndPollChat(CreateChatReq(
    botId = "your-bot-id",
    additionalMessages = listOf(EnterMessage("讲个笑话"))
)).let { result ->
    println(result)
}
```

### 工作流示例

```kotlin
val workflowService = WorkflowService()
workflowService.stream(RunWorkflowReq(
    workflowId = "your-workflow-id"
)).collect { workflowData ->
    println(workflowData)
}
```

### 数据集示例

```kotlin
val datasetService = DatasetService()
datasetService.create(CreateDatasetReq(
    name = "测试数据集",
    spaceId = "your-space-id",
    description = "测试描述"
)).let { result ->
    println(result)
}
```

### 机器人示例

```kotlin
val botService = BotService()

// 创建机器人
botService.create(CreateBotReq(
    spaceId = "your-space-id",
    name = "测试机器人",
    description = "一个测试机器人"
)).let { result ->
    println(result)
}

// 列出已发布的机器人
botService.list(ListBotReq(
    spaceId = "your-space-id"
)).let { result ->
    println(result)
}

// 发布机器人
botService.publish(PublishBotReq(
    botId = "your-bot-id",
    connectorIds = listOf("1024") // Agent as API
)).let { result ->
    println(result)
}
```

### 对话示例

```kotlin
val conversationService = ConversationService()

// 创建对话
conversationService.create(CreateConversationReq(
    botId = "your-bot-id"
)).let { result ->
    println(result)
}

// 列出对话
conversationService.list(ListConversationReq(
    botId = "your-bot-id"
)).let { result ->
    println(result)
}
```

### 文件示例

```kotlin
val fileService = FileService()

// 上传文件
fileService.upload(CreateFileReq(
    file = byteArrayOf(), // 您的文件字节数组
    fileName = "test.txt",
    mimeType = "text/plain"
)).let { result ->
    println(result)
}
```

### 工作空间示例

```kotlin
val workspaceService = WorkspaceService()

// 列出工作空间
workspaceService.list(WorkspaceListRequest(
    pageNum = 1,
    pageSize = 10
)).let { result ->
    println(result)
}
```

## 错误处理

SDK 提供了全面的错误处理，包含以下错误类型：

- `AuthenticationError` - 认证相关错误 (HTTP 401, code 4100)
- `BadRequestError` - 无效请求错误 (HTTP 400, code 4000)
- `PermissionDeniedError` - 权限相关错误 (HTTP 403, code 4101)
- `NotFoundError` - 资源未找到错误 (HTTP 404, code 4200)
- `RateLimitError` - 速率限制错误 (HTTP 429, code 4013)
- `TimeoutError` - 请求超时错误 (HTTP 408)
- `InternalServerError` - 服务器端错误 (HTTP 5xx)
- `GatewayError` - 网关相关错误 (HTTP 502)
- `APIConnectionError` - 网络连接错误
- `APIUserAbortError` - 用户中止请求错误
- `JSONParseError` - JSON 解析错误

错误处理示例:

```kotlin
try {
    val result = chatService.createChat(params)
    println(result)
} catch (e: AuthenticationError) {
    println("认证失败: ${e.message}")
} catch (e: BadRequestError) {
    println("无效请求: ${e.message}")
} catch (e: APIError) {
    println("API 错误: ${e.message}")
}
```

每种错误类型包含:
- HTTP 状态码 (如适用)
- 错误码
- 错误信息
- 错误详情
- 帮助文档 URL (如可用)
- 用于调试的日志 ID

## 贡献

我们欢迎贡献！请随时提交 Pull Request。

## 许可

本项目采用 MIT 许可证 - 详见 LICENSE 文件。 