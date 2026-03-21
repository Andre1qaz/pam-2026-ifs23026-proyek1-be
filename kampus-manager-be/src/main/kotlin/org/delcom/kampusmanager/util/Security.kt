package org.delcom.kampusmanager.util

import org.mindrot.jbcrypt.BCrypt

object Security {
    fun hashPassword(plain: String): String = BCrypt.hashpw(plain, BCrypt.gensalt(12))
    fun verifyPassword(plain: String, hashed: String): Boolean = BCrypt.checkpw(plain, hashed)
}
