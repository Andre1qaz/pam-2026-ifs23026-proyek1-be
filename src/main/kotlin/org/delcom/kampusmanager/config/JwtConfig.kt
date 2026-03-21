package org.delcom.kampusmanager.config

object JwtConfig {
    const val AUTH_NAME = "kampus-jwt"
    const val REALM     = "kampus-manager"
    const val ISSUER    = "kampus-manager-app"
    const val AUDIENCE  = "kampus-manager-user"
    const val EXPIRES_MS = 60L * 60 * 1000   // 1 jam
}
