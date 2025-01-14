package com.coze.api.helper

import io.ktor.client.engine.*
import io.ktor.client.engine.darwin.*

actual fun createPlatformEngine(): HttpClientEngine {
    return Darwin.create()
} 