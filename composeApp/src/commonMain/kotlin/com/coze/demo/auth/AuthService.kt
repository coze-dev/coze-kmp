package com.coze.demo.auth

import com.coze.api.helper.APIClient
import com.coze.api.helper.TokenService
import com.coze.api.model.auth.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import kotlinx.serialization.json.*

val AuthBaseURL = "https://37rtimftq5x63.ahost.marscode.site"

/**
 * Authentication Service | 认证服务
 * Handles JWT token generation and authentication related operations | 处理JWT令牌生成和认证相关操作
 */
object AuthService : TokenService {
    private val api = APIClient(baseURL = AuthBaseURL)

    /**
     * Get token from service | 从服务获取Token
     * @return TokenInfo Token information including expiration time | Token信息，包含过期时间
     */
    override suspend fun getToken(): TokenInfo {
        val tokenData = getJWTToken()
        return TokenInfo(tokenData?.accessToken, tokenData?.expiresIn ?: 900L)
    }

    /**
     * Get JWT Token | 获取JWT令牌
     * Internal implementation | 内部实现
     */
    private suspend fun getJWTToken(): GetTokenData? {
        val cleanKey = try {
            API_CONFIG.PRIVATE_KEY.trimIndent()
                .lines()
                .joinToString("\n")
                .trim()
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to clean private key: ${e.message}")
        }

        val requestBody = GetJWTTokenRequest(
            appId = API_CONFIG.APP_ID,
            keyId = API_CONFIG.KEY_ID,
            privateKey = cleanKey
        )

        val response = try {
            api.request(
                method = HttpMethod.Post,
                path = "/tools/coze/jwt",
                token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJmdW5jdGlvbmlkIjoiMzdydGltZnRxNXg2MyIsImlhdCI6MTcyNTc1NDk1OCwianRpIjoiNjZkY2VlNGU3OWEwMDI3MTEwYjg1YmQ4IiwidHlwZSI6ImZ1bmN0aW9uIiwidXNlcmlkIjoiQ0xPVURJREVfMWRrbnc5NTFuNXA1dm5fNzQxMjA1ODY0NjcwNzE3NjQ2NSIsInZlcnNpb24iOjJ9.H8d6QRxtWk17FAVAKvucWM__oEIL67ow6ENFM22A64E",
                body = requestBody
            )
        } catch (e: Exception) {
            throw Exception("Failed to send request for JWT token: ${e.message}")
        }

        val responseText = response.bodyAsText()
        
        val jwtResponse = try {
            Json.decodeFromString<JWTResponse>(responseText)
        } catch (e: Exception) {
            throw Exception("Failed to parse JWT response: ${e.message}, Response: $responseText")
        }

        if (jwtResponse.code != 0) {
            throw IllegalStateException("Failed to get JWT token, server returned code ${jwtResponse.code}: ${jwtResponse.data}")
        }

        return jwtResponse.data
    }

    private object API_CONFIG {
        const val APP_ID = "1196334548264"
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