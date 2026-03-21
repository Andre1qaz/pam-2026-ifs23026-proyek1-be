package org.delcom.kampusmanager.util

class AppException(val code: Int, override val message: String) : Exception(message)
