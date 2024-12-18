package com.coze.demo

import com.coze.api.auth.Auth
import com.coze.api.model.auth.*
import kotlinx.coroutines.runBlocking

object AuthDemo {
    // 测试配置
    private const val APP_ID = "1196334548264"
    private const val AUD = "api.coze.com"
    private const val KEY_ID = "trlYyMEkj6_Xhh4BMUk0s1f3g_wn2A8n09mtYT6UULk"
    private const val PRIVATE_KEY = """
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

    private var cachedToken: String? = null
    private var tokenExpireTime: Long = 0

    fun getJWTAuth(): String? {
        return runBlocking {
            // 如果token存在且未过期，直接返回
            if (cachedToken != null && System.currentTimeMillis() < tokenExpireTime) {
                return@runBlocking cachedToken
            }

            val config = JWTTokenConfig(
                appId = APP_ID,
                aud = AUD,
                keyId = KEY_ID,
                privateKey = PRIVATE_KEY,
            )
            try {
                val token = Auth.getJWTToken(config)
                // 缓存token和过期时间（提前5分钟过期以确保安全）
                cachedToken = token.access_token
                tokenExpireTime = System.currentTimeMillis() + (token.expires_in - 300) * 1000
                println("JWT Access Token: ${token.access_token}")
                println("Expires in: ${token.expires_in}")
                return@runBlocking token.access_token
            } catch (e: Exception) {
                println("Error getting JWT token: ${e.message}")
                return@runBlocking null
            }
        }
    }
}
