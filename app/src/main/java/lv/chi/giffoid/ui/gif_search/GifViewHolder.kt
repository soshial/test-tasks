package lv.chi.giffoid.ui.gif_search

import android.support.v4.widget.CircularProgressDrawable
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import lv.chi.giffoid.BuildConfig
import lv.chi.giffoid.R
import lv.chi.giffoid.api.GlideRequests
import lv.chi.giffoid.data.Gif


class GifViewHolder(
    gifMainView: View,
    private val glideRequests: GlideRequests,
    private val elementWidth: Int,
    private val gifClickedListener: GifAdapter.GifClickListener
) : RecyclerView.ViewHolder(gifMainView) {

    // views
    private val gifImageView = itemView.findViewById<ImageView>(R.id.some_image)
    private val gifTextView = itemView.findViewById<TextView>(R.id.debug_text)
    private val circularProgressDrawable = CircularProgressDrawable(itemView.context)

    /**
     * Must be called in onBindViewHolder()
     */
    fun setItem(gif: Gif) {
        // we must preset each viewholder height before image loading, otherwise it would stack up unevenly
        gifImageView.layoutParams = FrameLayout.LayoutParams(
            elementWidth,
            (elementWidth / gif.ratio).toInt()
        )
        gifImageView.requestLayout()

        // for blind users, accessibility
        gifTextView.contentDescription = gif.title

        // populating with debug info
        gifTextView.visibility = if (BuildConfig.DEBUG) {
            gifTextView.text = "page:${gif.pageNumber} / elem:${gif.listNumber}"
            View.VISIBLE
        } else View.INVISIBLE

        // load GIF
        glideRequests
            .asGif()
            .load(gif.imageTypes.fixedWidthDownsampled.gif)
            .placeholder(circularProgressDrawable)
//            .error(R.color.material_grey_100)
            .fitCenter()
            .into(gifImageView)

        itemView.setOnClickListener { gifClickedListener.onGifClicked(gif) }
    }

    init {
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.start()
    }
}