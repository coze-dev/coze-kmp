package com.coze.demo

import com.coze.api.file.FileService
import com.coze.api.model.file.CreateFileReq
import com.coze.api.model.file.FileObject

/**
 * File Demo | 文件演示
 * Demonstrates file upload and retrieval functionality | 演示文件上传和获取功能
 */
class FileDemo {
    private val fileService = FileService()

    /**
     * Upload file | 上传文件
     * @param fileName File name | 文件名称
     * @param mimeType MIME type | 文件类型
     * @param content File content as byte array | 文件内容字节数组
     * @return FileObject? Uploaded file information | 上传的文件信息
     */
    suspend fun uploadFile(
        fileName: String,
        mimeType: String,
        content: ByteArray
    ): FileObject? {
        val params = CreateFileReq(
            fileName = fileName,
            mimeType = mimeType,
            file = content
        )
        return fileService.upload(params).data
    }

    /**
     * Get file information | 获取文件信息
     * @param fileId File ID | 文件ID
     * @return FileObject? File information | 文件信息
     */
    suspend fun getFile(fileId: String): FileObject? {
        return fileService.retrieve(fileId).data
    }
} 