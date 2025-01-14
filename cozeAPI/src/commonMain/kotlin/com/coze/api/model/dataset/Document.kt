package com.coze.api.model.dataset

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@Serializable
enum class DocumentFileType {
    @SerialName("pdf")
    PDF,
    @SerialName("txt")
    TXT,
    @SerialName("doc")
    DOC,
    @SerialName("docx")
    DOCX;

    override fun toString(): String = when (this) {
        PDF -> "pdf"
        TXT -> "txt"
        DOC -> "doc"
        DOCX -> "docx"
    }
}

@Serializable
enum class DocumentFormatType(val value: Int) {
    @SerialName("0")
    DOCUMENT(0),    // 文档类型，例如 txt、pdf、在线网页等格式
    @SerialName("1")
    SPREADSHEET(1), // 表格类型，例如 xls 表格等格式
    @SerialName("2")
    IMAGE(2);       // 照片类型，例如 png 图片等格式

    override fun toString(): String = value.toString()

    companion object {
        fun fromValue(value: Int): DocumentFormatType = when (value) {
            0 -> DOCUMENT
            1 -> SPREADSHEET
            2 -> IMAGE
            else -> throw IllegalArgumentException("Unknown DocumentFormatType value: $value")
        }
    }
}

@Serializable
enum class DocumentSourceType(val value: Int) {
    @SerialName("0")
    LOCAL_FILE(0),    // 上传本地文件
    @SerialName("1")
    ONLINE_WEB(1),    // 上传在线网页
    @SerialName("2")
    CUSTOM(2),        // 自定义
    @SerialName("3")
    THIRD_PARTY(3),   // 第三方
    @SerialName("4")
    FRONT_CRAWL(4),   // 前端爬虫
    @SerialName("5")
    UPLOAD_FILE_ID(5), // OpenAPI Upload file_id
    @SerialName("101")
    NOTION(101),
    @SerialName("102")
    GOOGLE_DRIVE(102),
    @SerialName("103")
    FEISHU_WEB(103),
    @SerialName("104")
    LARK_WEB(104);

    override fun toString(): String = value.toString()

    companion object {
        fun fromValue(value: Int): DocumentSourceType = when (value) {
            0 -> LOCAL_FILE
            1 -> ONLINE_WEB
            2 -> CUSTOM
            3 -> THIRD_PARTY
            4 -> FRONT_CRAWL
            5 -> UPLOAD_FILE_ID
            101 -> NOTION
            102 -> GOOGLE_DRIVE
            103 -> FEISHU_WEB
            104 -> LARK_WEB
            else -> throw IllegalArgumentException("Unknown DocumentSourceType value: $value")
        }
    }
}

@Serializable
enum class DocumentStatus(val value: Int) {
    @SerialName("0")
    PROCESSING(0),  // 处理中
    @SerialName("1")
    COMPLETED(1),   // 处理完毕
    @SerialName("9")
    FAILED(9);      // 处理失败，建议重新上传

    override fun toString(): String = value.toString()

    companion object {
        fun fromValue(value: Int): DocumentStatus = when (value) {
            0 -> PROCESSING
            1 -> COMPLETED
            9 -> FAILED
            else -> throw IllegalArgumentException("Unknown DocumentStatus value: $value")
        }
    }
}

@Serializable
enum class DocumentUpdateType(val value: Int) {
    @SerialName("0")
    NO_AUTO_UPDATE(0),  // 不自动更新
    @SerialName("1")
    AUTO_UPDATE(1);     // 自动更新

    override fun toString(): String = value.toString()

    companion object {
        fun fromValue(value: Int): DocumentUpdateType = when (value) {
            0 -> NO_AUTO_UPDATE
            1 -> AUTO_UPDATE
            else -> throw IllegalArgumentException("Unknown DocumentUpdateType value: $value")
        }
    }
}

@Serializable
data class DocumentChunkStrategy(
    @SerialName("chunk_type")
    val chunkType: Int? = null,
    @SerialName("caption_type")
    val captionType: Int? = null,
    @SerialName("max_tokens")
    val maxTokens: Int? = null,
    @SerialName("remove_extra_spaces")
    val removeExtraSpaces: Boolean? = null,
    @SerialName("remove_urls_emails")
    val removeUrlsEmails: Boolean? = null,
    val separator: String? = null
) {
    companion object {
        fun buildAuto(): DocumentChunkStrategy = DocumentChunkStrategy(chunkType = 0)
        
        fun buildCustom(
            maxTokens: Int,
            separator: String,
            removeExtraSpaces: Boolean = false,
            removeUrlsEmails: Boolean = false
        ): DocumentChunkStrategy = DocumentChunkStrategy(
            chunkType = 1,
            maxTokens = maxTokens,
            removeExtraSpaces = removeExtraSpaces,
            removeUrlsEmails = removeUrlsEmails,
            separator = separator
        )
    }
}

@Serializable
data class Document(
    @SerialName("document_id")
    val documentId: String,
    @SerialName("char_count")
    val charCount: Int,
    @SerialName("chunk_strategy")
    val chunkStrategy: DocumentChunkStrategy? = null,
    @SerialName("format_type")
    val formatType: DocumentFormatType,
    @SerialName("hit_count")
    val hitCount: Int,
    val name: String,
    val size: Int,
    @SerialName("slice_count")
    val sliceCount: Int,
    @SerialName("source_type")
    val sourceType: DocumentSourceType,
    val status: DocumentStatus,
    val type: String,
    @SerialName("update_interval")
    val updateInterval: Int,
    @SerialName("update_type")
    val updateType: DocumentUpdateType,
    @SerialName("create_time")
    val createTime: Int,
    @SerialName("update_time")
    val updateTime: Int
)

@Serializable
data class DocumentSourceInfo(
    @SerialName("file_base64")
    val fileBase64: String? = null,
    @SerialName("file_type")
    val fileType: DocumentFileType? = null,
    @SerialName("web_url")
    val webUrl: String? = null,
    @SerialName("source_file_id")
    val sourceFileId: String? = null,
    @SerialName("document_source")
    val documentSource: Int? = null
) {
    companion object {
        @OptIn(ExperimentalEncodingApi::class)
        fun buildLocalFile(content: String, fileType: DocumentFileType = DocumentFileType.TXT): DocumentSourceInfo {
            val base64Content = Base64.encode(content.encodeToByteArray())
            return DocumentSourceInfo(
                fileBase64 = base64Content, 
                fileType = fileType,
                documentSource = DocumentSourceType.LOCAL_FILE.value
            )
        }
        
        fun buildWebPage(url: String): DocumentSourceInfo =
            DocumentSourceInfo(webUrl = url, documentSource = DocumentSourceType.ONLINE_WEB.value)

        fun buildFileId(fileId: String, fileType: DocumentFileType): DocumentSourceInfo =
            DocumentSourceInfo(sourceFileId = fileId, documentSource = DocumentSourceType.UPLOAD_FILE_ID.value, fileType = fileType)

        fun buildImageUpload(fileId: String): DocumentSourceInfo =
            DocumentSourceInfo(
                sourceFileId = fileId,
                documentSource = DocumentSourceType.UPLOAD_FILE_ID.value  // 5: Upload image
            )
    }
}

@Serializable
data class DocumentUpdateRule(
    @SerialName("update_type")
    val updateType: DocumentUpdateType,
    @SerialName("update_interval")
    val updateInterval: Int
) {
    companion object {
        fun buildNoAutoUpdate(): DocumentUpdateRule =
            DocumentUpdateRule(updateType = DocumentUpdateType.NO_AUTO_UPDATE, updateInterval = 24)

        fun buildAutoUpdate(interval: Int): DocumentUpdateRule =
            DocumentUpdateRule(updateType = DocumentUpdateType.AUTO_UPDATE, updateInterval = interval)
    }
}

@Serializable
data class DocumentBase(
    val name: String,
    @SerialName("source_info")
    val sourceInfo: DocumentSourceInfo,
    @SerialName("update_rule")
    val updateRule: DocumentUpdateRule? = null
)

@Serializable
data class DocumentListRequest(
    @SerialName("dataset_id")
    val datasetId: String,
    @SerialName("page")
    val page: Int = 1,
    @SerialName("size")
    val size: Int = 10
)

@Serializable
data class DocumentListResponse(
    val code: Int,
    val msg: String = "",
    @SerialName("document_infos")
    val documentInfos: List<Document>,
    val total: Int = 0
) 


@Serializable
data class CreateDocumentRequest(
    @SerialName("dataset_id")
    val datasetId: String,
    @SerialName("document_bases")
    val documentBases: List<DocumentBase>,
    @SerialName("chunk_strategy")
    val chunkStrategy: DocumentChunkStrategy? = null,
    @SerialName("format_type")
    val formatType: Int = DocumentFormatType.DOCUMENT.value
)

@Serializable
data class UpdateDocumentRequest(
    @SerialName("document_id")
    val documentId: String,
    @SerialName("document_name")
    val documentName: String? = null,
    @SerialName("update_rule")
    val updateRule: DocumentUpdateRule? = null
)

@Serializable
data class DeleteDocumentRequest(
    @SerialName("document_ids")
    val documentIds: List<String>
)

@Serializable
data class CreateDocumentResponse(
    val code: Int,
    val msg: String = "",
    @SerialName("document_infos")
    val documentInfos: List<Document> = emptyList()
)
