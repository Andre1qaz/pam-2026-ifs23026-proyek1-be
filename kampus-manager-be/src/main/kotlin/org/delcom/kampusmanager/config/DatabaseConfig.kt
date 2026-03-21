package org.delcom.kampusmanager.config

import io.ktor.server.application.*

/**
 * Fungsi ini tidak lagi digunakan secara langsung.
 * Koneksi database diatur di Application.kt via embeddedServer.
 */
@Deprecated("Database is now configured directly in Application.kt via embeddedServer")
fun Application.configureDatabase() {
    // No-op: database connection is handled in Application.configureApp()
}
