package lv.chi.giffoid.ui.gif_search

import android.support.v4.widget.CircularProgressDrawable
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import lv.chi.giffoid.BuildConfig
import lv.chi.giffoid.R
import lv.chi.giffoid.app.GlideRequests
import lv.chi.giffoid.data.Gif


class GifViewHolder(
    private val gifMainView: View,
    private val glideRequests: GlideRequests,
    private val elementWidth: Int,
    private val gifClickedListener: GifAdapter.GifClickListener
) :
    RecyclerView.ViewHolder(gifMainView) {

    // views
    val gifImageView = gifMainView.findViewById<ImageView>(R.id.some_image)
    val gifTextView = gifMainView.findViewById<TextView>(R.id.debug_text)
    val circularProgressDrawable = CircularProgressDrawable(itemView.context)

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
            .load(gif.imageUrls.fixedWidth.gif)
            .placeholder(circularProgressDrawable)
            .error(R.drawable.abc_ic_star_black_36dp)
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