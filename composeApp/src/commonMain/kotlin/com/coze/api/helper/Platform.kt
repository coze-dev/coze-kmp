package com.coze.api.helper

expect fun generateCodeVerifier(): String
expect fun generateCodeChallenge(codeVerifier: String): String
expect fun isBrowser(): Boolean

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform