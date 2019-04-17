package lv.chi.giffoid.ui.gif_search

import android.support.design.widget.Snackbar
import android.support.v4.widget.CircularProgressDrawable
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import lv.chi.giffoid.BuildConfig
import lv.chi.giffoid.R
import lv.chi.giffoid.app.GlideRequests
import lv.chi.giffoid.data.Gif
import lv.chi.giffoid.data.getRatio


class GifAdapter(var gifs: List<Gif>, val glideRequests: GlideRequests, val elementWidth: Int) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return GifViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.listitem_gif_result,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is GifViewHolder) {
            val gif = gifs[position]

            // we must preset each viewholder height before image loading, otherwise it would stack up unevenly
            holder.gifImageView.layoutParams = FrameLayout.LayoutParams(
                elementWidth,
                (elementWidth / gif.getRatio()).toInt()
            )
            holder.gifImageView.requestLayout()

            // load GIF
            glideRequests
                .asGif()
                .load(gif.imageUrls.fixed_width.url)
                .placeholder(holder.circularProgressDrawable)
                .fitCenter()
                .into(holder.gifImageView)
            // populating with debug info
            holder.gifTextView.visibility = if (BuildConfig.DEBUG) {
                holder.gifTextView.text = "p${gif.pageNumber} / l${gif.listNumber}"
                View.VISIBLE
            } else View.INVISIBLE
            // for blind users, accessibility
            holder.gifTextView.contentDescription = gif.title
            holder.itemView.setOnClickListener { }
        }
    }

    override fun getItemCount() = gifs.size

    class GifViewHolder(gifMainView: View) : RecyclerView.ViewHolder(gifMainView) {
        val gifImageView = gifMainView.findViewById<ImageView>(R.id.some_image)
        val gifTextView = gifMainView.findViewById<TextView>(R.id.debug_text)
        val circularProgressDrawable = CircularProgressDrawable(itemView.context)

        init {
            circularProgressDrawable.strokeWidth = 5f
            circularProgressDrawable.centerRadius = 30f
            circularProgressDrawable.start()
            itemView.setOnClickListener { onClick(it) }
        }

        private fun onClick(view: View) {
            Snackbar.make(view, gifTextView.text, 1000).show()// (getAdapterPosition())
        }
    }
}
