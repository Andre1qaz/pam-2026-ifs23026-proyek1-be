package org.delcom.kampusmanager.util

import io.ktor.http.content.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import io.ktor.util.cio.*
import java.io.File
import java.util.UUID

object FileUtil {
    suspend fun saveUpload(part: PartData.FileItem, folder: String): String {
        val ext = part.originalFileName
            ?.substringAfterLast('.', "")
            ?.let { if (it.isNotEmpty()) ".$it" else "" } ?: ""
        val filename = UUID.randomUUID().toString() + ext
        val path     = "uploads/$folder/$filename"

        withContext(Dispatchers.IO) {
            val file = File(path)
            file.parentFile?.mkdirs()
            part.provider().copyAndClose(file.writeChannel())
        }
        return path
    }

    fun deleteFile(path: String?) {
        path?.let { File(it).takeIf { f -> f.exists() }?.delete() }
    }
}
