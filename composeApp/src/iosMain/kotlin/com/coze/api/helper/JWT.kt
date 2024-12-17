package com.coze.api.helper

import platform.Foundation.*
import platform.Security.*

class IOSJWTProvider : JWTProvider {
    override fun sign(
        payload: Map<String, Any>,
        privateKey: String,
        algorithm: String,
        keyid: String
    ): String {
        // TODO: iOS 平台的 JWT 签名实现

        // 临时实现，抛出异常提示需要实现
        throw NotImplementedError("JWT signing not yet implemented for iOS platform")
    }
}

actual fun getJWTProvider(): JWTProvider = IOSJWTProvider()