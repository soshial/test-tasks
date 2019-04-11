package lv.chi.giffoid.ui.gif_search

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import lv.chi.giffoid.R
import lv.chi.giffoid.model.Gif

class GifAdapter(var gifs: List<Gif>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
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
            holder.gifTextView.text = gif.gifText
            Glide.with(holder.itemView).load(gif.url).into(holder.gifImageView)
        }
    }

    override fun getItemCount() = gifs.size

    class GifViewHolder(gifMainView: View) : RecyclerView.ViewHolder(gifMainView) {
        val gifImageView = gifMainView.findViewById<ImageView>(R.id.some_image)
        val gifTextView = gifMainView.findViewById<TextView>(R.id.some_text)
    }
}
