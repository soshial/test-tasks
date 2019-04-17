package lv.chi.giffoid.data

import com.google.gson.annotations.SerializedName

data class Gif(
    val type: String,
    val id: String,
    val title: String,
    @SerializedName("images") val imageUrls: ImageUrls
) {
    val ratio: Float = 1.0f * imageUrls.fixed_width.width / imageUrls.fixed_width.height
    @Transient
    var pageNumber: Int = 0
    @Transient
    var listNumber: Int = 0
}

fun Gif.getRatio(): Float = 1.0f * imageUrls.fixed_width.width / imageUrls.fixed_width.height

data class ImageUrls(
    val preview_webp: WebpPreview,

    @SerializedName("480w_still")
    val jpg: Jpg,
    val fixed_width: FixedWidth
)

data class WebpPreview(val url: String)
data class FixedWidth(
    val url: String,
    val webp: String,
    val width: Int,
    val height: Int
)

data class Jpg(val url: String)
