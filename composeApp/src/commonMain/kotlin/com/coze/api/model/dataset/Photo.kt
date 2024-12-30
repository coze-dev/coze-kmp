package com.coze.api.model.dataset

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class PhotoStatus(val value: Int) {
    @SerialName("0")
    IN_PROCESSING(0),     // 处理中
    @SerialName("1")
    COMPLETED(1),         // 处理完毕
    @SerialName("9")
    PROCESSING_FAILED(9)  // 处理失败，建议重新上传
}

@Serializable
data class Photo(
    @SerialName("document_id")
    val documentId: String,
    val name: String,
    val url: String,
    val caption: String,
    val size: Int,
    val type: String,
    val status: PhotoStatus,
    @SerialName("create_time")
    val createTime: Int,
    @SerialName("update_time")
    val updateTime: Int
)

@Serializable
data class PhotoListResponse(
    @SerialName("total_count")
    val totalCount: Int,
    @SerialName("photo_infos")
    val photoInfos: List<Photo>
) 