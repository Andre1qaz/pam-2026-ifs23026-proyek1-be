package org.delcom.kampusmanager.util

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.delcom.kampusmanager.config.JwtConfig
import java.util.Date

object TokenUtil {
    fun generateAuthToken(secret: String, userId: String): String =
        JWT.create()
            .withAudience(JwtConfig.AUDIENCE)
            .withIssuer(JwtConfig.ISSUER)
            .withClaim("userId", userId)
            .withExpiresAt(Date(System.currentTimeMillis() + JwtConfig.EXPIRES_MS))
            .sign(Algorithm.HMAC256(secret))

    fun extractUserId(secret: String, token: String): String? = try {
        JWT.require(Algorithm.HMAC256(secret))
            .build()
            .verify(token)
            .getClaim("userId")
            .asString()
    } catch (e: Exception) { null }
}
