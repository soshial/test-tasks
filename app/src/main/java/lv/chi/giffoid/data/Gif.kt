package lv.chi.giffoid.data

import com.google.gson.annotations.SerializedName

data class Gif(
    val type: String,
    val id: String,
    val title: String,
    val url: String,
    @SerializedName("images")
    val imageUrls: ImageTypes
) {
//    @Transient
    /**
     * We need dimensions ratio to properly display ViewHolder before image loads
     */
    val ratio: Float
        get() = 1.0f * imageUrls.original.width / imageUrls.original.height
    @Transient
    var pageNumber: Int = 0
    @Transient
    var listNumber: Int = 0
}

data class ImageTypes(
    val original: UrlsAndSizes,
    @SerializedName("480w_still")
    val jpg: UrlsAndSizes,
    @SerializedName("fixed_width")
    val fixedWidth: UrlsAndSizes
)

data class UrlsAndSizes(
    @SerializedName("url")
    val gif: String,
    val webp: String,
    val width: Int,
    val height: Int
)
