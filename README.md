# Coze Kotlin Multiplatform API SDK

[![Kotlin Multiplatform](https://img.shields.io/badge/Kotlin-Multiplatform-blue.svg)](https://www.jetbrains.com/kotlin-multiplatform/)

[中文文档](README_zh.md)

## Introduction

This is a Kotlin Multiplatform project for Coze API targeting Android, iOS. The SDK provides a powerful way to integrate Coze's open APIs into your Kotlin Multiplatform projects.

Key Features:

* Full support for Coze open APIs and authentication APIs
* Kotlin Coroutines & Flow support for async operations
* Cross-platform support (Android & iOS)
* Optimized streaming APIs with Flow returns
* Simple and intuitive API design

## Project Structure

* `/cozeAPI` - The core API SDK module containing all the API implementations.
  - `commonMain` - Common code for all platforms:
    - API Services:
      - `bot` - Bot management services
      - `chat` - Chat and message services
      - `conversation` - Conversation management services
      - `dataset` - Dataset and document services
      - `file` - File upload services
      - `workflow` - Workflow execution services
      - `workspace` - Workspace management services
  - `androidMain` - Android-specific implementations
  - `iosMain` - iOS-specific implementations

* `/composeApp` - Demo application showcasing SDK usage.
  - `commonMain` - Cross-platform demo code:
    - `com.coze.demo` - Demo implementations for each API feature:
      - `AuthDemo.kt` - Authentication examples
      - `BotDemo.kt` - Bot management examples
      - `ChatDemo.kt` - Chat functionality examples
      - `ConversationDemo.kt` - Conversation management examples
      - `DatasetDemo.kt` - Dataset operations examples
      - `FileDemo.kt` - File handling examples
      - `WorkflowDemo.kt` - Workflow execution examples
      - `WorkspaceDemo.kt` - Workspace management examples
  - `androidMain` - Android-specific demo code
  - `iosMain` - iOS-specific demo code

* `/iosApp` - iOS demo application.
  - Native iOS application entry point
  - Demonstrates SDK usage in iOS environment

## Dependencies
This project utilizes the [Kotlin Multiplatform](https://www.jetbrains.com/kotlin-multiplatform/) framework.

## Authentication

To use the Coze API, you need to implement a `TokenService` interface that provides authentication tokens. The SDK uses this service to handle authentication for all API calls.

```kotlin
interface TokenService {
    /**
     * Get token from service
     * @return TokenInfo Token information including expiration time
     */
    suspend fun getToken(): TokenInfo
}
```

The `TokenInfo` class contains:
```kotlin
data class TokenInfo(
    val token: String?,      // Access token
    val expiresIn: Long      // Expiration time in seconds
)
```

You can implement this interface in different ways:

1. **Personal Access Token (PAT)**
```kotlin
class PATTokenService(private val token: String) : TokenService {
    override suspend fun getToken(): TokenInfo {
        // Return PAT with a default expiration time
        return TokenInfo(token = token, expiresIn = 3600)
    }
}
```

2. **JWT Token**
```kotlin
class JWTTokenService(
    private val appId: String,
    private val keyId: String,
    private val privateKey: String
) : TokenService {
    override suspend fun getToken(): TokenInfo {
        // Implement JWT token generation logic
        val jwtToken = generateJWTToken(
            appId = appId,
            keyId = keyId,
            privateKey = privateKey
        )
        return TokenInfo(token = jwtToken.accessToken, expiresIn = jwtToken.expiresIn)
    }
}
```

3. **OAuth Token**
```kotlin
class OAuthTokenService(
    private val appId: String,
    private val appSecret: String
) : TokenService {
    override suspend fun getToken(): TokenInfo {
        // Implement OAuth token retrieval logic
        val tokenData = getOAuthToken(
            appId = appId,
            appSecret = appSecret
        )
        return TokenInfo(token = tokenData.accessToken, expiresIn = tokenData.expiresIn)
    }
}
```

Then initialize the SDK with your token service:

```kotlin
// Initialize with PAT
val patService = PATTokenService("your-pat-token")
TokenManager.init(patService)

// Or initialize with JWT
val jwtService = JWTTokenService(
    appId = "your-app-id",
    keyId = "your-key-id",
    privateKey = "your-private-key"
)
TokenManager.init(jwtService)

// Or initialize with OAuth
val oauthService = OAuthTokenService(
    appId = "your-app-id",
    appSecret = "your-app-secret"
)
TokenManager.init(oauthService)
```

The SDK will automatically handle token caching and renewal. It will refresh the token when:
- Token is null
- Token is about to expire (30 seconds before expiration)
- Force refresh is requested

## Usage Examples

The SDK provides comprehensive examples for various use cases:

### Example List

| Example                       | File                                                                                                                                      |
| ----------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------- |
| Token Authentication          | [AuthDemo.kt](/composeApp/src/commonMain/kotlin/com/coze/demo/AuthDemo.kt)                           |
| Bot Creation and Publishing   | [BotDemo.kt](/composeApp/src/commonMain/kotlin/com/coze/demo/BotDemo.kt)                          |
| Non-streaming Chat           | [ChatDemo.kt](/composeApp/src/commonMain/kotlin/com/coze/demo/ChatDemo.kt)                                     |
| Streaming Chat              | [ChatDemo.kt](/composeApp/src/commonMain/kotlin/com/coze/demo/ChatDemo.kt)                         |
| Conversation Management      | [ConversationDemo.kt](/composeApp/src/commonMain/kotlin/com/coze/demo/ConversationDemo.kt)                                     |
| Dataset Management          | [DatasetDemo.kt](/composeApp/src/commonMain/kotlin/com/coze/demo/DatasetDemo.kt)                   |
| File Upload and Management  | [FileDemo.kt](/composeApp/src/commonMain/kotlin/com/coze/demo/FileDemo.kt)                                     |
| Workflow Execution          | [WorkflowDemo.kt](/composeApp/src/commonMain/kotlin/com/coze/demo/WorkflowDemo.kt)                   |
| Workspace Management        | [WorkspaceDemo.kt](/composeApp/src/commonMain/kotlin/com/coze/demo/WorkspaceDemo.kt)              |

### Bot Operations
- Create and publish bots
- List and retrieve bots
- Update bot configurations

### Chat Operations
- Non-streaming chat
- Streaming chat
- Chat with images
- Chat with local plugins

### Workflow Operations
- Run workflows
- Stream workflow execution
- Handle workflow interrupts

### Dataset Operations
- Create/Update/Delete datasets
- List datasets
- Document management
- Image management

### Conversation Management
- Create/Update conversations
- List conversations
- Message operations

### File Operations
- Upload files
- Retrieve file information

### Workspace Operations
- List workspaces
- Workspace management

## Code Examples

### Chat Example

```kotlin
// Create a streaming chat
val chatService = ChatService()
chatService.stream(StreamChatReq(
    botId = "your-bot-id",
    additionalMessages = listOf(EnterMessage("Tell me a joke"))
)).collect { chatData ->
    println(chatData)
}

// Create a non-streaming chat
chatService.createAndPollChat(CreateChatReq(
    botId = "your-bot-id",
    additionalMessages = listOf(EnterMessage("Tell me a joke"))
)).let { result ->
    println(result)
}
```

### Workflow Example

```kotlin
val workflowService = WorkflowService()
workflowService.stream(RunWorkflowReq(
    workflowId = "your-workflow-id"
)).collect { workflowData ->
    println(workflowData)
}
```

### Dataset Example

```kotlin
val datasetService = DatasetService()
datasetService.create(CreateDatasetReq(
    name = "Test Dataset",
    spaceId = "your-space-id",
    description = "Test description"
)).let { result ->
    println(result)
}
```

### Bot Example

```kotlin
val botService = BotService()

// Create a bot
botService.create(CreateBotReq(
    spaceId = "your-space-id",
    name = "Test Bot",
    description = "A test bot"
)).let { result ->
    println(result)
}

// List published bots
botService.list(ListBotReq(
    spaceId = "your-space-id"
)).let { result ->
    println(result)
}

// Publish a bot
botService.publish(PublishBotReq(
    botId = "your-bot-id",
    connectorIds = listOf("1024") // Agent as API
)).let { result ->
    println(result)
}
```

### Conversation Example

```kotlin
val conversationService = ConversationService()

// Create a conversation
conversationService.create(CreateConversationReq(
    botId = "your-bot-id"
)).let { result ->
    println(result)
}

// List conversations
conversationService.list(ListConversationReq(
    botId = "your-bot-id"
)).let { result ->
    println(result)
}
```

### File Example

```kotlin
val fileService = FileService()

// Upload a file
fileService.upload(CreateFileReq(
    file = byteArrayOf(), // Your file bytes
    fileName = "test.txt",
    mimeType = "text/plain"
)).let { result ->
    println(result)
}
```

### Workspace Example

```kotlin
val workspaceService = WorkspaceService()

// List workspaces
workspaceService.list(WorkspaceListRequest(
    pageNum = 1,
    pageSize = 10
)).let { result ->
    println(result)
}
```

## Error Handling

The SDK provides comprehensive error handling with the following error types:

- `AuthenticationError` - Authentication related errors (HTTP 401, code 4100)
- `BadRequestError` - Invalid request errors (HTTP 400, code 4000)
- `PermissionDeniedError` - Permission related errors (HTTP 403, code 4101)
- `NotFoundError` - Resource not found errors (HTTP 404, code 4200)
- `RateLimitError` - Rate limit related errors (HTTP 429, code 4013)
- `TimeoutError` - Request timeout errors (HTTP 408)
- `InternalServerError` - Server side errors (HTTP 5xx)
- `GatewayError` - Gateway related errors (HTTP 502)
- `APIConnectionError` - Network connection errors
- `APIUserAbortError` - User aborted request errors
- `JSONParseError` - JSON parsing errors

Example error handling:

```kotlin
try {
    val result = chatService.createChat(params)
    println(result)
} catch (e: AuthenticationError) {
    println("Authentication failed: ${e.message}")
} catch (e: BadRequestError) {
    println("Invalid request: ${e.message}")
} catch (e: APIError) {
    println("API error: ${e.message}")
}
```

Each error type includes:
- HTTP status code (if applicable)
- Error code
- Error message
- Error details
- Help documentation URL (if available)
- Log ID for debugging

## Contributing

We welcome contributions! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.
