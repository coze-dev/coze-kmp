package com.coze.api.dataset

import com.coze.api.helper.APIBase
import com.coze.api.helper.RequestOptions
import com.coze.api.model.ApiResponse
import com.coze.api.model.dataset.*

/**
 * Dataset Service | 数据集服务
 * Handles dataset operations including creation, listing, updating and deletion | 处理数据集操作，包括创建、列表、更新和删除
 */
class DatasetService : APIBase() {
    private var _documents: DocumentService? = null
    private var _images: ImageService? = null

    val documents: DocumentService
        get() {
            if (_documents == null) {
                _documents = DocumentService()
            }
            return _documents!!
        }

    val images: ImageService
        get() {
            if (_images == null) {
                _images = ImageService()
            }
            return _images!!
        }

    /**
     * Create a dataset | 创建数据集
     * @param name Dataset name | 数据集名称
     * @param spaceId Space ID | 空间ID
     * @param formatType Dataset type (0: text, 2: image) | 数据集类型（0：文本，2：图片）
     * @param description Dataset description | 数据集描述
     * @param iconFileId Icon file ID (upload via coze.files.upload) | 图标文件ID（通过 coze.files.upload 上传）
     * @return CreateDatasetResponse Created dataset information | 创建的数据集信息
     */
    suspend fun create(
        name: String,
        spaceId: String,
        formatType: DocumentFormatType,
        description: String? = null,
        iconFileId: String? = null
    ): ApiResponse<CreateDatasetResponse> {
        // Parameter validation | 参数验证
        require(name.isNotBlank()) { "name cannot be empty" }
        require(spaceId.isNotBlank()) { "spaceId cannot be empty" }
        
        return post("/v1/datasets", mapOf(
            "name" to name,
            "space_id" to spaceId,
            "format_type" to formatType.toString(),
            "description" to description,
            "file_id" to iconFileId
        ).filterValues { it != null })
    }

    /**
     * List datasets | 获取数据集列表
     * @param spaceId Space ID | 空间ID
     * @param name Dataset name (fuzzy search) | 数据集名称（支持模糊搜索）
     * @param formatType Dataset type (0: text, 1: table, 2: image) | 数据集类型（0：文本，1：表格，2：图片）
     * @param pageNum Page number (min: 1, default: 1) | 页码（最小值：1，默认：1）
     * @param pageSize Page size (range: 1-300, default: 10) | 每页大小（范围：1-300，默认：10）
     * @return DatasetListResponse List of datasets | 数据集列表
     */
    suspend fun list(
        spaceId: String,
        name: String? = null,
        formatType: DocumentFormatType? = null,
        pageNum: Int = 1,
        pageSize: Int = 10
    ): ApiResponse<DatasetListResponse> {
        // Parameter validation | 参数验证
        require(spaceId.isNotBlank()) { "spaceId cannot be empty" }
        require(pageNum >= 1) { "pageNum must be greater than or equal to 1" }
        require(pageSize in 1..300) { "pageSize must be between 1 and 300" }
        
        val params = mutableMapOf(
            "space_id" to spaceId,
            "page_num" to pageNum.toString(),
            "page_size" to pageSize.toString()
        )
        name?.let { params["name"] = it }
        formatType?.let { params["format_type"] = it.toString() }

        return get("/v1/datasets", RequestOptions(params = params))
    }

    /**
     * Update dataset | 更新数据集
     * This API will fully refresh the dataset's name, file_id, and description settings | 该接口会全量刷新数据集的 name、file_id、description 设置
     * @param datasetId Dataset ID | 数据集ID
     * @param name Dataset name | 数据集名称
     * @param description Dataset description | 数据集描述
     * @param iconFileId Icon file ID | 图标文件ID
     */
    suspend fun update(
        datasetId: String,
        name: String? = null,
        description: String? = null,
        iconFileId: String? = null
    ): ApiResponse<Unit> {
        // Parameter validation | 参数验证
        require(datasetId.isNotBlank()) { "datasetId cannot be empty" }
        
        // Skip update if all parameters are null | 如果所有参数都为null，则不更新
        if (name == null && description == null && iconFileId == null) {
            return ApiResponse(code = 0, msg = "success")
        }
        val body = mapOf(
            "name" to name,
            "description" to description,
            "file_id" to iconFileId
        )
        return put("/v1/datasets/$datasetId", body)
    }

    /**
     * Delete dataset | 删除数据集
     * Workspace admins can delete all datasets, others can only delete their own | 工作空间管理员可以删除所有数据集，其他成员只能删除自己的
     * Deleting a dataset will also delete all its files and unbind related bots | 删除数据集时会同时删除其所有文件并解绑相关机器人
     * @param datasetId Dataset ID | 数据集ID
     */
    suspend fun delete(datasetId: String): ApiResponse<Unit> {
        // Parameter validation | 参数验证
        require(datasetId.isNotBlank()) { "datasetId cannot be empty" }
        
        return _delete("/v1/datasets/$datasetId")
    }

    /**
     * Check upload progress | 查看上传进度
     * Get the upload progress of dataset files | 获取数据集文件的上传进度
     * Supports all file types (text, image, table) | 支持所有文件类型（文本、图片、表格）
     * Can check multiple files in the same dataset | 可以批量查看同一数据集中的多个文件进度
     * @param datasetId Dataset ID | 数据集ID
     * @param documentIds Document ID list | 文档ID列表
     * @return List<DocumentProgress> Progress information | 进度信息列表
     */
    suspend fun process(
        datasetId: String,
        documentIds: List<String>
    ): ApiResponse<List<DocumentProgress>> {
        // Parameter validation | 参数验证
        require(datasetId.isNotBlank()) { "datasetId cannot be empty" }
        require(documentIds.isNotEmpty()) { "documentIds cannot be empty" }
        require(documentIds.all { it.isNotBlank() }) { "documentIds cannot contain empty strings" }
        
        val body = mapOf(
            "document_ids" to documentIds
        )
        return post("/v1/datasets/$datasetId/process", body)
    }
} 