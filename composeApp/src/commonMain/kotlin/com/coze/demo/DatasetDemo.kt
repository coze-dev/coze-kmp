package com.coze.demo

import com.coze.api.dataset.DatasetService
import com.coze.api.model.ApiResponse
import com.coze.api.model.dataset.*

/**
 * Dataset Demo | 数据集演示
 * Demonstrates dataset management functionality | 演示数据集管理功能
 */
class DatasetDemo {
    private val service = DatasetService()

    // Dataset operations | 数据集操作
    /**
     * Create dataset | 创建数据集
     * @param name Dataset name | 数据集名称
     * @param spaceId Space ID | 空间ID
     * @param description Dataset description | 数据集描述
     * @return ApiResponse<CreateDatasetResponse> Created dataset | 创建的数据集
     */
    suspend fun createDataset(
        name: String,
        spaceId: String,
        description: String? = null
    ): ApiResponse<CreateDatasetResponse> {
        return service.create(
            name = name,
            spaceId = spaceId,
            formatType = DocumentFormatType.DOCUMENT,
            description = description
        )
    }

    /**
     * List datasets | 列出数据集
     * @param spaceId Space ID | 空间ID
     * @param pageNum Page number | 页码
     * @param pageSize Page size | 每页大小
     * @return ApiResponse<DatasetListResponse> List of datasets | 数据集列表
     */
    suspend fun listDatasets(
        spaceId: String,
        pageNum: Int = 1,
        pageSize: Int = 10
    ): ApiResponse<DatasetListResponse> {
        return service.list(spaceId, null, null, pageNum, pageSize)
    }

    /**
     * Update dataset | 更新数据集
     * @param datasetId Dataset ID | 数据集ID
     * @param name New dataset name | 新的数据集名称
     * @param description New dataset description | 新的数据集描述
     * @return ApiResponse<Unit> Update result | 更新结果
     */
    suspend fun updateDataset(
        datasetId: String,
        name: String? = null,
        description: String? = null
    ): ApiResponse<Unit> {
        return service.update(datasetId, name, description)
    }

    /**
     * Delete dataset | 删除数据集
     * @param datasetId Dataset ID | 数据集ID
     * @return ApiResponse<Unit> Delete result | 删除结果
     */
    suspend fun deleteDataset(datasetId: String): ApiResponse<Unit> {
        return service.delete(datasetId)
    }

    /**
     * Process dataset | 处理数据集
     * @param datasetId Dataset ID | 数据集ID
     * @param documentIds Document IDs | 文档ID列表
     * @return ApiResponse<List<DocumentProgress>> Processing progress | 处理进度
     */
    suspend fun processDataset(
        datasetId: String,
        documentIds: List<String>
    ): ApiResponse<List<DocumentProgress>> {
        return service.process(datasetId, documentIds)
    }

    // Document operations | 文档操作
    /**
     * Create document | 创建文档
     * @param datasetId Dataset ID | 数据集ID
     * @param name Document name | 文档名称
     * @param content Document content | 文档内容
     * @return ApiResponse<List<Document>> Created documents | 创建的文档列表
     */
    suspend fun createDocument(
        datasetId: String,
        name: String,
        content: String
    ): ApiResponse<List<Document>> {
        return service.documents.create(
            datasetId = datasetId,
            documentBases = listOf(
                DocumentBase(
                    name = name,
                    sourceInfo = DocumentSourceInfo.buildLocalFile(content)
                )
            ),
            formatType = DocumentFormatType.DOCUMENT
        )
    }

    // Image upload operations | 图片上传操作
    /**
     * Create image | 创建图片
     * @param datasetId Dataset ID | 数据集ID
     * @param name Image name | 图片名称
     * @param fileId File ID | 文件ID
     * @return ApiResponse<List<Document>> Created images | 创建的图片列表
     */
    suspend fun createImage(
        datasetId: String,
        name: String,
        fileId: String
    ): ApiResponse<List<Document>> {
        return service.documents.create(
            datasetId = datasetId,
            documentBases = listOf(
                DocumentBase(
                    name = name,
                    sourceInfo = DocumentSourceInfo.buildImageUpload(fileId)
                )
            ),
            formatType = DocumentFormatType.IMAGE
        )
    }

    /**
     * List documents | 列出文档
     * @param datasetId Dataset ID | 数据集ID
     * @param pageNum Page number | 页码
     * @param pageSize Page size | 每页大小
     * @return ApiResponse<List<Document>> List of documents | 文档列表
     */
    suspend fun listDocuments(
        datasetId: String,
        pageNum: Int = 1,
        pageSize: Int = 10
    ): ApiResponse<List<Document>> {
        return service.documents.list(datasetId, pageNum, pageSize)
    }

    /**
     * Delete documents | 删除文档
     * @param datasetId Dataset ID | 数据集ID
     * @param documentIds Document IDs | 文档ID列表
     * @return ApiResponse<Unit> Delete result | 删除结果
     */
    suspend fun deleteDocuments(
        datasetId: String,
        documentIds: List<String>
    ): ApiResponse<Unit> {
        return service.documents.delete(documentIds)
    }

    // Image operations | 图片操作
    /**
     * Update image | 更新图片
     * @param datasetId Dataset ID | 数据集ID
     * @param documentId Document ID | 文档ID
     * @param caption Image caption | 图片描述
     * @return ApiResponse<Unit> Update result | 更新结果
     */
    suspend fun updateImage(
        datasetId: String,
        documentId: String,
        caption: String
    ): ApiResponse<Unit> {
        return service.images.update(datasetId, documentId, caption)
    }

    /**
     * List images | 列出图片
     * @param datasetId Dataset ID | 数据集ID
     * @param keyword Search keyword | 搜索关键词
     * @param hasCaption Has caption filter | 是否有描述过滤
     * @param pageNum Page number | 页码
     * @param pageSize Page size | 每页大小
     * @return ApiResponse<PhotoListResponse> List of images | 图片列表
     */
    suspend fun listImages(
        datasetId: String,
        keyword: String? = null,
        hasCaption: Boolean = false,
        pageNum: Int = 1,
        pageSize: Int = 10
    ): ApiResponse<PhotoListResponse> {
        return service.images.list(
            datasetId = datasetId,
            keyword = keyword,
            hasCaption = hasCaption,
            pageNum = pageNum,
            pageSize = pageSize
        )
    }

    /**
     * Update document | 更新文档
     * @param documentId Document ID | 文档ID
     * @param documentName New document name | 新的文档名称
     * @param updateRule Update rule | 更新规则
     * @return ApiResponse<Unit> Update result | 更新结果
     */
    suspend fun updateDocument(
        documentId: String,
        documentName: String? = null,
        updateRule: DocumentUpdateRule? = null
    ): ApiResponse<Unit> {
        return service.documents.update(
            documentId = documentId,
            documentName = documentName,
            updateRule = updateRule
        )
    }
} 