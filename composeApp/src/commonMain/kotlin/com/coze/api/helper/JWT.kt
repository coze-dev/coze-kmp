package com.coze.api.helper

interface JWTProvider {
    fun sign(
        payload: Map<String, Any>,
        privateKey: String,
        algorithm: String,
        keyid: String
    ): String
}

expect fun getJWTProvider(): JWTProvider 