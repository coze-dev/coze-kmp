package com.coze.api.dataset

import com.coze.api.helper.APIBase
import com.coze.api.helper.RequestOptions
import com.coze.api.model.ApiResponse
import com.coze.api.model.dataset.*
import kotlinx.serialization.Serializable

@Serializable
data class UpdateImageRequest(
    val caption: String
)

/**
 * Image Service | 图片服务
 * Handles image operations in datasets | 处理数据集中的图片操作
 */
class ImageService : APIBase() {
    /**
     * Update image caption | 更新图片描述
     * @param datasetId Dataset ID | 数据集ID
     * @param documentId Image ID | 图片ID
     * @param caption Image caption | 图片描述
     */
    suspend fun update(
        datasetId: String,
        documentId: String,
        caption: String
    ): ApiResponse<Unit> {
        // Parameter validation | 参数验证
        require(datasetId.isNotBlank()) { "datasetId cannot be empty" }
        require(documentId.isNotBlank()) { "documentId cannot be empty" }
        require(caption.isNotBlank()) { "caption cannot be empty" }
        
        return put("/v1/datasets/$datasetId/images/$documentId", UpdateImageRequest(caption))
    }

    /**
     * List images in dataset | 获取数据集中的图片列表
     * Filter images by caption | 支持通过图片标注进行筛选
     * @param datasetId Dataset ID | 数据集ID
     * @param keyword Keyword for filtering | 筛选关键词
     * @param hasCaption Has caption filter | 是否有图片描述
     * @param pageNum Page number (default: 1) | 页码（默认：1）
     * @param pageSize Page size (range: 1-299, default: 10) | 每页大小（范围：1-299，默认：10）
     * @return PhotoListResponse List of images | 图片列表
     */
    suspend fun list(
        datasetId: String,
        keyword: String? = null,
        hasCaption: Boolean? = null,
        pageNum: Int = 1,
        pageSize: Int = 10
    ): ApiResponse<PhotoListResponse> {
        // Parameter validation | 参数验证
        require(datasetId.isNotBlank()) { "datasetId cannot be empty" }
        require(pageNum >= 1) { "pageNum must be greater than or equal to 1" }
        require(pageSize in 1..299) { "pageSize must be between 1 and 299" }
        
        val params = mutableMapOf(
            "page_num" to pageNum.toString(),
            "page_size" to pageSize.toString()
        )
        keyword?.let { params["keyword"] = it }
        hasCaption?.let { params["has_caption"] = it.toString() }

        return get("/v1/datasets/$datasetId/images", RequestOptions(params = params))
    }
} 