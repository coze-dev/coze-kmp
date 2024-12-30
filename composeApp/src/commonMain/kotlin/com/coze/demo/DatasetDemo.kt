package com.coze.demo

import com.coze.api.dataset.DatasetService
import com.coze.api.model.ApiResponse
import com.coze.api.model.dataset.*

class DatasetDemo {
    private val service = DatasetService()

    // 数据集操作
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

    suspend fun listDatasets(
        spaceId: String,
        pageNum: Int = 1,
        pageSize: Int = 10
    ): ApiResponse<DatasetListResponse> {
        return service.list(spaceId, null, null, pageNum, pageSize)
    }

    suspend fun updateDataset(
        datasetId: String,
        name: String? = null,
        description: String? = null
    ): ApiResponse<Unit> {
        return service.update(datasetId, name, description)
    }

    suspend fun deleteDataset(datasetId: String): ApiResponse<Unit> {
        return service.delete(datasetId)
    }

    suspend fun processDataset(
        datasetId: String,
        documentIds: List<String>
    ): ApiResponse<List<DocumentProgress>> {
        return service.process(datasetId, documentIds)
    }

    // 文档操作
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

    // 图片上传操作
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

    suspend fun listDocuments(
        datasetId: String,
        pageNum: Int = 1,
        pageSize: Int = 10
    ): ApiResponse<List<Document>> {
        return service.documents.list(datasetId, pageNum, pageSize)
    }

    suspend fun deleteDocuments(
        datasetId: String,
        documentIds: List<String>
    ): ApiResponse<Unit> {
        return service.documents.delete(documentIds)
    }

    // 图片操作
    suspend fun updateImage(
        datasetId: String,
        documentId: String,
        caption: String
    ): ApiResponse<Unit> {
        return service.images.update(datasetId, documentId, caption)
    }

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