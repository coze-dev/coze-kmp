package com.coze.api.helper

import android.annotation.SuppressLint
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.security.KeyFactory
import java.security.interfaces.RSAPrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Base64

class AndroidJWTProvider : JWTProvider {
    @SuppressLint("NewApi")
    override fun sign(
        payload: Map<String, Any>,
        privateKey: String,
        algorithm: String,
        keyid: String
    ): String {
        println("keyid: $keyid")
        val cleanKey = privateKey
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("-----BEGIN RSA PRIVATE KEY-----", "")
            .replace("-----END RSA PRIVATE KEY-----", "")
            .replace("\n", "")
        val keyBytes = Base64.getDecoder().decode(cleanKey)
        val keySpec = PKCS8EncodedKeySpec(keyBytes)
        val keyFactory = KeyFactory.getInstance("RSA")
        val privateKeyObj = keyFactory.generatePrivate(keySpec) as RSAPrivateKey
        
        val alg = Algorithm.RSA256(null, privateKeyObj)
        
        return JWT.create()
            .withKeyId(keyid)
            .apply {
                payload.forEach { (key, value) ->
                    when (value) {
                        is String -> withClaim(key, value)
                        is Int -> withClaim(key, value)
                        is Long -> withClaim(key, value)
                        is Double -> withClaim(key, value)
                        is Boolean -> withClaim(key, value)
                    }
                }
            }
            .sign(alg)
    }
}

actual fun getJWTProvider(): JWTProvider = AndroidJWTProvider() 