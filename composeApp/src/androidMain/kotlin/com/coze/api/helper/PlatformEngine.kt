package com.coze.api.helper

import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*

actual fun createPlatformEngine(): HttpClientEngine {
    return CIO.create()
} 