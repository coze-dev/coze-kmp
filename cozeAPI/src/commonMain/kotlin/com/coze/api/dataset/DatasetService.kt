package com.coze.api.dataset

import com.coze.api.helper.APIBase
import com.coze.api.helper.RequestOptions
import com.coze.api.model.ApiResponse
import com.coze.api.model.dataset.*

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
     * 创建数据集
     *
     * @param name 数据集名称
     * @param spaceId 数据集所属的空间ID
     * @param formatType 数据集类型，0：文本，2：图片
     * @param description 数据集描述
     * @param iconFileId 图标文件ID，通过 coze.files.upload 上传
     */
    suspend fun create(
        name: String,
        spaceId: String,
        formatType: DocumentFormatType,
        description: String? = null,
        iconFileId: String? = null
    ): ApiResponse<CreateDatasetResponse> {
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
     * 查看数据集列表
     *
     * @param spaceId 数据集所属的空间ID
     * @param name 数据集名称，支持模糊搜索
     * @param formatType 数据集类型，0：文本，1：表格，2：图片
     * @param pageNum 页码，最小值为1，默认为1
     * @param pageSize 每页大小，取值范围为1~300，默认为10
     */
    suspend fun list(
        spaceId: String,
        name: String? = null,
        formatType: DocumentFormatType? = null,
        pageNum: Int = 1,
        pageSize: Int = 10
    ): ApiResponse<DatasetListResponse> {
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
     * 更新数据集
     * 该接口会全量刷新知识库的 name、file_id、description 设置，如果不设置这些参数，会恢复为默认设置。
     *
     * @param datasetId 数据集ID
     * @param name 数据集名称
     * @param description 数据集描述
     * @param iconFileId 图标文件ID，通过 coze.files.upload 上传
     */
    suspend fun update(
        datasetId: String,
        name: String? = null,
        description: String? = null,
        iconFileId: String? = null
    ): ApiResponse<Unit> {
        require(datasetId.isNotBlank()) { "datasetId cannot be empty" }
        
        // 如果传入的都为null，则不更新
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
     * 删除数据集
     * 工作空间管理员可以删除团队内的所有知识库，其他成员只能删除自己拥有的知识库。
     * 删除知识库时，会同时删除上传到该知识库的所有文件，绑定了该知识库的机器人会自动解绑。
     *
     * @param datasetId 数据集ID
     */
    suspend fun delete(datasetId: String): ApiResponse<Unit> {
        require(datasetId.isNotBlank()) { "datasetId cannot be empty" }
        
        return _delete("/v1/datasets/$datasetId")
    }

    /**
     * 查看上传进度
     * 调用该接口获取知识库文件的上传进度。
     * 该接口支持查看所有类型知识库文件的上传进度，例如文本、图片、表格。
     * 支持批量查看多个文件的进度，但文件必须位于同一个知识库中。
     *
     * @param datasetId 数据集ID
     * @param documentIds 文档ID列表
     */
    suspend fun process(
        datasetId: String,
        documentIds: List<String>
    ): ApiResponse<List<DocumentProgress>> {
        require(datasetId.isNotBlank()) { "datasetId cannot be empty" }
        require(documentIds.isNotEmpty()) { "documentIds cannot be empty" }
        require(documentIds.all { it.isNotBlank() }) { "documentIds cannot contain empty strings" }
        
        val body = mapOf(
            "document_ids" to documentIds
        )
        return post("/v1/datasets/$datasetId/process", body)
    }
} 