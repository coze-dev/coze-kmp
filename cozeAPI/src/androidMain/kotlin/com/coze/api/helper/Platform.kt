package com.coze.api.helper

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom

actual fun generateCodeVerifier(): String {
    val secureRandom = SecureRandom()
    val bytes = ByteArray(32)
    secureRandom.nextBytes(bytes)
    return bytes.joinToString("") { "%02x".format(it) }
}

actual fun generateCodeChallenge(codeVerifier: String): String {
    val bytes = codeVerifier.toByteArray()
    val messageDigest = MessageDigest.getInstance("SHA-256")
    val digest = messageDigest.digest(bytes)
    return Base64.encodeToString(digest, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
}

actual fun isBrowser(): Boolean = false

class AndroidPlatform: Platform {
    override val name: String = "Android"
}

actual fun getPlatform(): Platform = AndroidPlatform() 