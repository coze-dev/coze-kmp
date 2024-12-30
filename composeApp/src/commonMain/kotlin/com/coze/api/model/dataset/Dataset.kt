package com.coze.api.model.dataset

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class DatasetStatus {
    @SerialName("1")
    ENABLED,    // 启用
    @SerialName("3")
    DISABLED    // 禁用
}

@Serializable
data class Dataset(
    @SerialName("dataset_id")
    val datasetId: String,
    val name: String,
    val description: String,
    @SerialName("space_id")
    val spaceId: String,
    val status: DatasetStatus,
    @SerialName("format_type")
    val formatType: DocumentFormatType,
    @SerialName("can_edit")
    val canEdit: Boolean = false,
    @SerialName("icon_url")
    val iconUrl: String = "",
    @SerialName("doc_count")
    val docCount: Int = 0,
    @SerialName("file_list")
    val fileList: List<String> = emptyList(),
    @SerialName("hit_count")
    val hitCount: Int = 0,
    @SerialName("bot_used_count")
    val botUsedCount: Int = 0,
    @SerialName("slice_count")
    val sliceCount: Int = 0,
    @SerialName("all_file_size")
    val allFileSize: Int = 0,
    @SerialName("chunk_strategy")
    val chunkStrategy: DocumentChunkStrategy? = null,
    @SerialName("failed_file_list")
    val failedFileList: List<String> = emptyList(),
    @SerialName("processing_file_list")
    val processingFileList: List<String> = emptyList(),
    @SerialName("processing_file_id_list")
    val processingFileIdList: List<String> = emptyList(),
    @SerialName("avatar_url")
    val avatarUrl: String = "",
    @SerialName("creator_id")
    val creatorId: String = "",
    @SerialName("creator_name")
    val creatorName: String = "",
    @SerialName("create_time")
    val createTime: Int = 0,
    @SerialName("update_time")
    val updateTime: Int = 0
)

@Serializable
data class CreateDatasetResponse(
    @SerialName("dataset_id")
    val datasetId: String
)

@Serializable
data class DatasetListResponse(
    @SerialName("total_count")
    val totalCount: Int,
    @SerialName("dataset_list")
    val datasetList: List<Dataset>
)

@Serializable
data class DocumentProgress(
    @SerialName("document_id")
    val documentId: String = "",
    val url: String = "",
    val size: Int = 0,
    val type: String = "",
    val status: DocumentStatus,
    val progress: Int = 0,
    @SerialName("update_type")
    val updateType: DocumentUpdateType,
    @SerialName("document_name")
    val documentName: String = "",
    @SerialName("remaining_time")
    val remainingTime: Int = 0,
    @SerialName("status_descript")
    val statusDescript: String = "",
    @SerialName("update_interval")
    val updateInterval: Int = 0
) 