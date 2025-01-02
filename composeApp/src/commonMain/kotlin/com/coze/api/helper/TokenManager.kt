package com.coze.api.helper

import com.coze.api.auth.AuthService
import com.coze.api.model.auth.JWTTokenConfig
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.*
import kotlinx.datetime.Clock

object TokenManager {
    private var _token: String? = null
    private var tokenExpireTime: Long = 0

    suspend fun getTokenAsync(forceRefresh: Boolean = false): String {
        val now = Clock.System.now().epochSeconds
        
        // 如果token不存在或已过期（提前30秒认为过期），重新获取
        if (_token == null || now >= tokenExpireTime - 30 || forceRefresh) {
            val (token, expireIn) = generateToken()
            _token = token
            // 根据返回的过期时间设置
            tokenExpireTime = now + expireIn
        }
        return _token ?: throw IllegalStateException("Token not available")
    }

    private suspend fun generateToken(): Pair<String, Long> {
        try {
            val config = JWTTokenConfig(
                appId = API_CONFIG.APP_ID,
                aud = API_CONFIG.AUD,
                keyId = API_CONFIG.KEY_ID,
                privateKey = API_CONFIG.PRIVATE_KEY
                    .trimIndent()
                    .lines()
                    .joinToString("\n")
                    .trim()
            )
            val jwtRsp = AuthService.getJWTToken(config)
//            println("JWT response: $jwtRsp")

            val token = jwtRsp.accessToken
            if (token.isEmpty()) {
                throw IllegalStateException("Received empty access token from server")
            }
            // 返回token和过期时间
            return Pair(token, jwtRsp.expiresIn ?: 900L)
        } catch (e: Exception) {
            println("Token generation failed: ${e.message}")
            throw IllegalStateException("Failed to generate token: ${e.message}", e)
        }
    }

    fun clearToken() {
        _token = null
        tokenExpireTime = 0
    }

    private object API_CONFIG {
        const val APP_ID = "1196334548264"
        const val AUD = "api.coze.com"
        const val KEY_ID = "trlYyMEkj6_Xhh4BMUk0s1f3g_wn2A8n09mtYT6UULk"
        const val PRIVATE_KEY = """
-----BEGIN PRIVATE KEY-----
MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQChTvq59MiRnwJW
5ozmWWcKCD76eqdPtQID8jewZiC7auDf459Fp9BdAn+IHRNpUu/dhtRqsKRb+vxQ
m72XRYt6c5fhFx9l+z7ownrDssMKvPB6ur4dMRMtL2ZuAbcwMLXHZjn5TYrQxpln
jTCwDEUn1wG2V+XDSIR3OBC8LQxgf4zPo+CAznf8VXocc9T/5K0Xi2uoQBRHvAkV
SlgpNy9AHij4P5e7daP33T8HbU9v8bu1u+sfiCRXLANZr3ppB7tNOpJ/37B0uu9z
haGVvm76fFohrpGkbXvp4WOtSuj9yz9CliBh4hiXwOANxMWkc2ptVLoBWTyosnyM
mJtx2T67AgMBAAECggEAPWi5V9DCCHoN5HJZwpnXecDA5Q+LoXMJ5OGx5LREksTx
/hoEOPrVfoskbPeQu8CIs3+QX1uG/sNSigd4Sl395uyZreXlHKhk/yyGmvjzeqOK
M1bjaG6V29ZXOtrpV+27TkFzIZ8tESUCIqNkHSlWrH+UVcwpmsoBL/fGmzTVKOjb
Ercq+O5qbpdPwbVX4YmQ7v/EiPeTN+2Ihrl7o4gUQVrXGj2t2I7t/00WEQL9mrix
MKLiCR/LSR47B7isHSdd2/VaOpCi/IlmuN6EZlAlUSzkvzG7Ro0+IwT8nfMA56/w
WXj1hmzfhJS+seu4FolaVw7BSi48d0ppp2piHOl7yQKBgQDViLO3yWFP1IZA/azc
aQK8K5AhEWBIvKHXuA4KSqigi1UMutdDZ9V3+WiaLDA4QiSwr9ll5opaqGPw/YI+
k2oVKTi0vpLN0ut3NA5O3VtPdBwcjgEh8biIk3VkHHWT1BxJWDCJjWVFYQhrsrFJ
3UmeENDX8SOWiI3OHE1hRa8x8wKBgQDBY2i/Dj+phc40KLF1Ya5m2lDRvcGPWUn+
LlldLE93Z55qdjFQzLb9uyo5alLp9nzKKVxNnqLH/umaLToGgY9ljmn62vgLP1RB
mgUKoVDD7zKGUHMNl6TcGse2YGwHQEy4Cgmp0ZW5KYhlRALIucYMxiX7zEH0hbnJ
JNn6s6aqGQKBgQCmh+lZMRXSc3WMpAo3DzKR3AuYrnt/3pT9rs5MWnNuWZunxONy
7zy9R2a7rjCg/3yry0jpvsUx9NeUr7Du6nq2LCLMW0AUgyUeHxv1h47ZxTzTTDA3
79MxuIa537QW6TpyPPQFop9TX1x42bCIYaszOfwWHTrbAH5POyVh8j2y3wKBgQC0
nHocTVtpoLzne8XYweTWzDhE2rrmfVacnH77hMGm29BYz4/ZTYftyppxyvoq5fi/
+gbpuyl+LXXQd0LFBsV87JfQICceogO3zPe+aNB8XW5LgOEjJahjpGM20jCySPwd
ucoynrn9l2t6YB1ViCMOlRSuaKKEk0vjLpaiDJywKQKBgQDLx5i7QT26PukTjbUW
PUrBPspy0O4pltQTM3EiUWOV0tzZj7TEIKUJGRvM8FR1YWHxmeKsXgIJQqgTcnLk
s/wFcZjKreatkOwVHDyTgL7cpaCwzFLCWXSjj7e+01GJxKgrZsDtNC71IwDRFQV+
FQtEbyXUN8BGlP3yyRJr+6v/fw==
-----END PRIVATE KEY-----
        """
    }
}