package com.coze.api.file

import com.coze.api.helper.APIBase
import com.coze.api.helper.RequestOptions
import com.coze.api.model.ApiResponse
import com.coze.api.model.file.CreateFileReq
import com.coze.api.model.file.FileObject
import io.ktor.client.request.forms.*
import io.ktor.http.*

class FileService : APIBase() {
    /**
     * 上传文件到Coze平台
     * @param params 上传文件的参数
     * @param options 请求选项
     * @return 上传的文件信息
     */
    suspend fun upload(
        params: CreateFileReq,
        options: RequestOptions? = null
    ): ApiResponse<FileObject> {
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
        
        // println("[DEBUG] 文件大小: ${params.file.size} bytes")
        // println("[DEBUG] 文件名: ${params.fileName}")
        // println("[DEBUG] MIME类型: ${params.mimeType}")
        
        val requestOptions = options?.copy(
            headers = mapOf(
                HttpHeaders.ContentType to ContentType.MultiPart.FormData.toString()
            )
        ) ?: RequestOptions(
            headers = mapOf(
                HttpHeaders.ContentType to ContentType.MultiPart.FormData.toString()
            )
        )
        
        val response = post<FileObject>("/v1/files/upload", formData, requestOptions)
        println("[文件上传结果] $response")
        return response
    }

    /**
     * 获取已上传文件的信息
     * @param fileId 文件ID
     * @param options 请求选项
     * @return 文件信息
     */
    suspend fun retrieve(
        fileId: String,
        options: RequestOptions? = null
    ): ApiResponse<FileObject> {
        val queryParams = mapOf("file_id" to fileId)
        val requestOptions = options?.copy(params = queryParams) 
            ?: RequestOptions(params = queryParams)
        return get("/v1/files/retrieve", requestOptions)
    }
} 