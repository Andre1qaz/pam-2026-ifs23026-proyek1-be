package org.delcom.kampusmanager.config

object JwtConfig {
    const val AUTH_NAME  = "kampus-jwt"
    const val REALM      = "kampus-manager"
    const val ISSUER     = "delcom-app"       // ← ubah dari "kampus-manager-app"
    const val AUDIENCE   = "delcom-user"      // ← ubah dari "kampus-manager-user"
    const val EXPIRES_MS = 60L * 60 * 1000   // 1 jam
}