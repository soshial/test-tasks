package lv.chi.giffoid.ui.gif_search

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import lv.chi.giffoid.R
import lv.chi.giffoid.app.GlideRequests
import lv.chi.giffoid.data.Gif


class GifAdapter(
    var gifs: List<Gif>,
    val glideRequests: GlideRequests,
    val elementWidth: Int,
    val onGifClickedListener: GifClickListener
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return GifViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.listitem_gif_result, parent, false
            ), glideRequests, elementWidth, onGifClickedListener
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is GifViewHolder) {
            holder.setItem(gifs[position])
        }
    }

    override fun getItemCount() = gifs.size

    interface GifClickListener {
        fun onGifClicked(gif: Gif)
    }
}
