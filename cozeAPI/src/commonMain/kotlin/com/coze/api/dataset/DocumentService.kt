package com.coze.api.dataset

import com.coze.api.helper.APIBase
import com.coze.api.helper.RequestOptions
import com.coze.api.model.ApiResponse
import com.coze.api.model.dataset.*
import kotlinx.serialization.serializer

/**
 * Document Service | 文档服务
 * Handles document operations in datasets | 处理数据集中的文档操作
 */
class DocumentService : APIBase() {
    private fun openApiOptions() = RequestOptions(
        headers = mapOf("Agw-Js-Conv" to "str")
    )

    /**
     * Upload files to dataset | 上传文件到数据集
     * @param datasetId Dataset ID | 数据集ID
     * @param documentBases File metadata list (max 10 files) | 待上传文件的元数据列表（最多10个文件）
     * @param chunkStrategy Chunk strategy (required for first upload) | 分段策略（首次上传时需要）
     * @param formatType File format (0: document, 1: table, 2: image) | 文件格式（0：文档，1：表格，2：图片）
     * @return List<Document> Created documents information | 创建的文档信息列表
     */
    suspend fun create(
        datasetId: String,
        documentBases: List<DocumentBase>,
        chunkStrategy: DocumentChunkStrategy? = DocumentChunkStrategy.buildAuto(),
        formatType: DocumentFormatType = DocumentFormatType.DOCUMENT
    ): ApiResponse<List<Document>> {
        // Parameter validation | 参数验证
        require(datasetId.isNotBlank()) { "datasetId cannot be empty" }
        require(documentBases.isNotEmpty()) { "documentBases cannot be empty" }
        require(documentBases.size <= 10) { "documentBases size cannot be greater than 10" }
        require(documentBases.all { it.name.isNotBlank() }) { "document name cannot be empty" }
        
        val payload = CreateDocumentRequest(
            datasetId = datasetId,
            documentBases = documentBases,
            chunkStrategy = chunkStrategy,
            formatType = formatType.value
        )
        val response = getClient().post<CreateDocumentResponse>(
            "/open_api/knowledge/document/create", 
            payload,
            openApiOptions()
        )
        
        return ApiResponse(
            code = response.code,
            msg = response.msg,
            data = response.documentInfos
        )
    }

    /**
     * Update document name and strategy | 修改文件名称和更新策略
     * @param documentId Document ID | 文件ID
     * @param documentName New document name | 文件的新名称
     * @param updateRule Update strategy for online files | 在线文件的更新策略
     */
    suspend fun update(
        documentId: String,
        documentName: String? = null,
        updateRule: DocumentUpdateRule? = null
    ): ApiResponse<Unit> {
        // Parameter validation | 参数验证
        require(documentId.isNotBlank()) { "documentId cannot be empty" }
        require(documentName?.isNotBlank() != false) { "documentName cannot be empty if provided" }
        
        return post(
            "/open_api/knowledge/document/update", 
            UpdateDocumentRequest(
                documentId = documentId,
                documentName = documentName,
                updateRule = updateRule
            ),
            openApiOptions()
        )
    }

    /**
     * Delete documents from dataset | 删除数据集中的文件
     * Supports batch deletion (max 100 files) | 支持批量删除（最多100个文件）
     * @param documentIds Document ID list | 待删除的文件ID列表
     */
    suspend fun delete(
        documentIds: List<String>
    ): ApiResponse<Unit> {
        // Parameter validation | 参数验证
        require(documentIds.isNotEmpty()) { "documentIds cannot be empty" }
        require(documentIds.size <= 100) { "documentIds size cannot be greater than 100" }
        require(documentIds.all { it.isNotBlank() }) { "documentIds cannot contain empty strings" }
        
        return post(
            "/open_api/knowledge/document/delete", 
            DeleteDocumentRequest(documentIds = documentIds),
            openApiOptions()
        )
    }

    /**
     * List documents in dataset | 获取数据集的文件列表
     * @param datasetId Dataset ID | 数据集ID
     * @param pageNum Page number (default: 1) | 页码（默认：1）
     * @param pageSize Page size (default: 10) | 每页大小（默认：10）
     * @return List<Document> List of documents | 文件列表
     */
    suspend fun list(
        datasetId: String,
        pageNum: Int = 1,
        pageSize: Int = 10
    ): ApiResponse<List<Document>> {
        // Parameter validation | 参数验证
        require(datasetId.isNotBlank()) { "datasetId cannot be empty" }
        require(pageNum >= 1) { "pageNum must be greater than or equal to 1" }
        require(pageSize >= 1) { "pageSize must be greater than or equal to 1" }
        
        val response = getClient().post<DocumentListResponse>(
            "/open_api/knowledge/document/list", 
            DocumentListRequest(
                datasetId = datasetId,
                page = pageNum,
                size = pageSize
            ),
            openApiOptions()
        )
        
        return ApiResponse<List<Document>>(
            code = response.code,
            msg = response.msg,
            data = response.documentInfos,
            total = response.total
        )
    }
}
