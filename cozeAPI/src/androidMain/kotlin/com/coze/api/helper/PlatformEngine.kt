package com.coze.api.helper

import io.ktor.client.HttpClient
import io.ktor.client.engine.*

actual fun createPlatformEngine(): HttpClientEngine {
    return HttpClient().engine
} 