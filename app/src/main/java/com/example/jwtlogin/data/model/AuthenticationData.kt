package com.example.jwtlogin.data.model

import com.auth0.jwt.JWT
import com.google.gson.annotations.SerializedName

data class AuthenticationData(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String = "bearer",
    @SerializedName("expires_in") val expiresIn: Long = 0,
    @SerializedName("refresh_token") val refreshToken: String = "",
) {
    fun getClaims(): JwtClaims {
        val jwt = JWT.decode(accessToken)
        return JwtClaims(
            userId = jwt.getClaim("idp:user_id").asString(),
            userName = jwt.getClaim("idp:user_name").asString(),
            fullName = jwt.getClaim("idp:fullname").asString(),
            role = jwt.getClaim("role").asString(),
            exp = jwt.getClaim("exp").asLong(),
        )
    }
}

data class JwtClaims (
    val userId: String,
    val userName: String,
    val fullName: String,
    val role: String,
    val exp: Long = 0,
)