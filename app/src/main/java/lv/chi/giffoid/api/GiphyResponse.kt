package lv.chi.giffoid.api

import com.google.gson.annotations.SerializedName

data class GiphyResponse<Gif>(val data: List<Gif>, val pagination: PaginationInfo)

data class PaginationInfo(
    @SerializedName("total_count") val totalCount: Int,
    val count: Int,
    val offset: Int
)