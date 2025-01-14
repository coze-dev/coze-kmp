package com.coze.api.file

import com.coze.api.helper.APIBase
import com.coze.api.helper.RequestOptions
import com.coze.api.model.ApiResponse
import com.coze.api.model.file.CreateFileReq
import com.coze.api.model.file.FileObject
import io.ktor.client.request.forms.*
import io.ktor.http.*

/**
 * File Service | 文件服务
 * Handles file upload and retrieval operations | 处理文件上传和获取操作
 */
class FileService : APIBase() {
    /**
     * Upload file to Coze platform | 上传文件到Coze平台
     * @param params Upload file parameters | 上传文件参数
     * @param options Request options | 请求选项
     * @return FileObject File information | 文件信息
     */
    suspend fun upload(
        params: CreateFileReq,
        options: RequestOptions? = null
    ): ApiResponse<FileObject> {
        // Parameter validation | 参数验证
        require(params.file.isNotEmpty()) { "file cannot be empty" }
        require(params.fileName.isNotBlank()) { "fileName cannot be empty" }
        require(params.mimeType.isNotBlank()) { "mimeType cannot be empty" }
        
        val formData = MultiPartFormDataContent(
            formData {
                append(
                    key = "file",
                    value = params.file,
                    headers = Headers.build {
                        append(HttpHeaders.ContentType, params.mimeType)
                        append(HttpHeaders.ContentDisposition, "filename=\"${params.fileName}\"")
                    }
                )
            }
        )

        val requestOptions = options?.copy(
            headers = mapOf(
                HttpHeaders.ContentType to ContentType.MultiPart.FormData.toString()
            )
        ) ?: RequestOptions(
            headers = mapOf(
                HttpHeaders.ContentType to ContentType.MultiPart.FormData.toString()
            )
        )
        
        return post<FileObject>("/v1/files/upload", formData, requestOptions)
    }

    /**
     * Retrieve uploaded file information | 获取已上传文件的信息
     * @param fileId File ID | 文件ID
     * @param options Request options | 请求选项
     * @return FileObject File information | 文件信息
     */
    suspend fun retrieve(
        fileId: String,
        options: RequestOptions? = null
    ): ApiResponse<FileObject> {
        require(fileId.isNotBlank()) { "fileId cannot be empty" }
        
        val queryParams = mapOf("file_id" to fileId)
        val requestOptions = options?.copy(params = queryParams) 
            ?: RequestOptions(params = queryParams)
        return get("/v1/files/retrieve", requestOptions)
    }
} 