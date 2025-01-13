package com.coze.api.helper

import kotlinx.cinterop.*
import platform.Foundation.*
import platform.Security.*
import platform.darwin.*
import platform.posix.size_t
import platform.posix.uint8_tVar
import platform.CoreCrypto.*

class PlatformImpl : Platform {
    override val name: String = "iOS"
}

actual fun getPlatform(): Platform = PlatformImpl()

actual fun generateCodeVerifier(): String {
    // 生成一个随机的code verifier
    val length = 43
    val allowedChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-._~"
    return (1..length)
        .map { allowedChars.random() }
        .joinToString("")
}

@OptIn(ExperimentalForeignApi::class)
actual fun generateCodeChallenge(codeVerifier: String): String {
    val data = codeVerifier.encodeToByteArray()
    val digestLength = 32 // SHA256 digest length is 32 bytes
    
    return memScoped {
        val hash = UByteArray(digestLength)
        val input = UByteArray(data.size) { data[it].toUByte() }
        
        input.usePinned { inputPin ->
            hash.usePinned { hashPin ->
                CC_SHA256(
                    inputPin.addressOf(0),
                    input.size.toUInt(),
                    hashPin.addressOf(0)
                )
            }
        }
        
        hash.usePinned { hashPin ->
            val nsData = NSData.dataWithBytes(hashPin.addressOf(0), length = digestLength.toULong())
            val base64String = nsData.base64EncodedStringWithOptions(0u)
            
            base64String
                .replace("+", "-")
                .replace("/", "_")
                .replace("=", "")
        }
    }
}

actual fun isBrowser(): Boolean = false 