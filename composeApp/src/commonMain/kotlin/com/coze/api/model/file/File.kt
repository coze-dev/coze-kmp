package com.coze.api.model.file

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateFileReq(
    val file: ByteArray,
    @SerialName("file_name")
    val fileName: String,
    @SerialName("mime_type")
    val mimeType: String
)

@Serializable
data class FileObject(
    val id: String,
    @SerialName("file_name")
    val fileName: String,
    val bytes: Long,
    @SerialName("created_at")
    val createdAt: String
) 