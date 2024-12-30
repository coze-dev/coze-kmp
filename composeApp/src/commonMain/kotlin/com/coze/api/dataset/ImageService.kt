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

class ImageService : APIBase() {
    /**
     * 更新图片描述
     *
     * @param datasetId 数据集ID
     * @param documentId 图片ID
     * @param caption 图片描述
     */
    suspend fun update(
        datasetId: String,
        documentId: String,
        caption: String
    ): ApiResponse<Unit> {
        return put("/v1/datasets/$datasetId/images/$documentId", UpdateImageRequest(caption))
    }

    /**
     * 查看图片知识库中的图片详细信息
     * 查看图片时，支持通过图片标注进行筛选
     *
     * @param datasetId 数据集ID
     * @param keyword 关键词，用于筛选图片
     * @param hasCaption 是否有图片描述
     * @param pageNum 分页查询的页码，默认为1，即从第一页开始返回数据
     * @param pageSize 分页的大小，默认为10，即每页返回10条数据。取值范围为1~299，默认为10
     */
    suspend fun list(
        datasetId: String,
        keyword: String? = null,
        hasCaption: Boolean? = null,
        pageNum: Int = 1,
        pageSize: Int = 10
    ): ApiResponse<PhotoListResponse> {
        val params = mutableMapOf(
            "page_num" to pageNum.toString(),
            "page_size" to pageSize.toString()
        )
        keyword?.let { params["keyword"] = it }
        hasCaption?.let { params["has_caption"] = it.toString() }

        return get("/v1/datasets/$datasetId/images", RequestOptions(params = params))
    }
} 