package com.coze.demo

import com.coze.api.file.FileService
import com.coze.api.model.file.CreateFileReq
import com.coze.api.model.file.FileObject

class FileDemo {
    private val fileService = FileService()

    /**
     * 上传文件
     * @param fileName 文件名
     * @param mimeType 文件类型
     * @param content 文件内容
     * @return 上传的文件信息
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
     * 获取文件信息
     * @param fileId 文件ID
     * @return 文件信息
     */
    suspend fun getFile(fileId: String): FileObject? {
        return fileService.retrieve(fileId).data
    }
} 