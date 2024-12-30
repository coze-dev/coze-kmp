package com.coze.api.dataset

import com.coze.api.helper.APIBase
import com.coze.api.helper.RequestOptions
import com.coze.api.model.ApiResponse
import com.coze.api.model.dataset.*
import kotlinx.serialization.serializer

class DocumentService : APIBase() {
    private fun openApiOptions() = RequestOptions(
        headers = mapOf("Agw-Js-Conv" to "str")
    )

    /**
     * 上传文件到指定数据集
     *
     * @param datasetId 数据集ID
     * @param documentBases 待上传文件的元数据信息，数组最大长度为10，即一次最多上传10个文件
     * @param chunkStrategy 分段策略，仅在首次上传文件到新数据集时需要设置这些规则
     * @param formatType 文件格式类型，0：文档，1：表格，2：图片
     */
    suspend fun create(
        datasetId: String,
        documentBases: List<DocumentBase>,
        chunkStrategy: DocumentChunkStrategy? = DocumentChunkStrategy.buildAuto(),
        formatType: DocumentFormatType = DocumentFormatType.DOCUMENT
    ): ApiResponse<List<Document>> {
        val payload = CreateDocumentRequest(
            datasetId = datasetId,
            documentBases = documentBases,
            chunkStrategy = chunkStrategy,
            formatType = formatType.value
        )
        println("[DocumentService] create payload: ${getClient().jsonUtil.encodeToString(serializer<CreateDocumentRequest>(), payload)}")
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
     * 修改文件名称和更新策略
     *
     * @param documentId 文件ID
     * @param documentName 文件的新名称
     * @param updateRule 在线文件的更新策略，默认不自动更新
     */
    suspend fun update(
        documentId: String,
        documentName: String? = null,
        updateRule: DocumentUpdateRule? = null
    ): ApiResponse<Unit> {
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
     * 删除数据集中的文件，支持批量删除
     *
     * @param documentIds 待删除的文件ID列表，数组最大长度为100，即一次最多删除100个文件
     */
    suspend fun delete(
        documentIds: List<String>
    ): ApiResponse<Unit> {
        return post(
            "/open_api/knowledge/document/delete", 
            DeleteDocumentRequest(documentIds = documentIds),
            openApiOptions()
        )
    }

    /**
     * 查看指定数据集的文件列表
     *
     * @param datasetId 数据集ID
     * @param pageNum 分页查询的页码，默认为1，即从第一页开始返回数据
     * @param pageSize 分页的大小，默认为10，即每页返回10条数据
     */
    suspend fun list(
        datasetId: String,
        pageNum: Int = 1,
        pageSize: Int = 10
    ): ApiResponse<List<Document>> {
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
