package org.delcom.kampusmanager.util

import org.delcom.kampusmanager.util.AppException

class Validator(private val data: Map<String, Any?>) {
    private val errors = mutableListOf<String>()

    fun required(field: String, label: String = field) {
        val v = data[field]
        if (v == null || (v is String && v.isBlank()))
            errors += "$field: $label tidak boleh kosong"
    }

    fun minLength(field: String, min: Int, label: String = field) {
        val v = data[field]
        if (v is String && v.length < min)
            errors += "$field: $label minimal $min karakter"
    }

    fun validate() {
        if (errors.isNotEmpty()) throw AppException(400, errors.joinToString(" | "))
    }
}
