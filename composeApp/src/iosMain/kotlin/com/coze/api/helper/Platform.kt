package com.coze.api.helper

import platform.Foundation.*
import platform.Security.*

actual fun generateCodeVerifier(): String {
    val bytes = ByteArray(32)
    SecRandomCopyBytes(kSecRandomDefault, bytes.size.toULong(), bytes.refTo(0))
    return bytes.joinToString("") { "%02x".format(it) }
}

actual fun generateCodeChallenge(codeVerifier: String): String {
    val data = codeVerifier.encodeToByteArray()
    val digest = NSMutableData()
    CC_SHA256(data.refTo(0), data.size.toUInt(), digest.mutableBytes)
    
    return NSData.create(bytes = digest.bytes, length = CC_SHA256_DIGEST_LENGTH.toULong())
        .base64EncodedStringWithOptions(0)
        .replace("=", "")
        .replace("+", "-")
        .replace("/", "_")
}

actual fun isBrowser(): Boolean = false

class IOSPlatform: Platform {
    override val name: String = "iOS"
}

actual fun getPlatform(): Platform = IOSPlatform() 