package com.coze.api

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform