package lv.chi.giffoid.data

import com.google.gson.annotations.SerializedName

data class Gif(
    val type: String,
    val id: String,
    val title: String,
    val url: String,
    @SerializedName("images")
    val imageTypes: ImageTypes
) {
//    @Transient
    /**
     * We need dimensions ratio to properly display ViewHolder before image loads
     */
    val ratio: Float
        get() = 1.0f * imageTypes.original.width / imageTypes.original.height
    @Transient
    var pageNumber: Int = 0
    @Transient
    var listNumber: Int = 0
}

data class ImageTypes(
    val original: UrlsAndSizes,
    @SerializedName("480w_still")
    val originalStill: UrlsAndSizes,
    @SerializedName("fixed_width")
    val fixedWidth: UrlsAndSizes,
    @SerializedName("fixed_height_downsampled")
    val fixedWidthDownsampled: UrlsAndSizes
)

data class UrlsAndSizes(
    @SerializedName("url")
    val gif: String,
    @Deprecated("Not supported yet by Glide", level = DeprecationLevel.ERROR)
    val webp: String,
    val width: Int,
    val height: Int
)
