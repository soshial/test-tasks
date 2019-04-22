package lv.chi.giffoid.data

import io.reactivex.Single

interface GifRepository {
    fun loadGifs(searchQuery: CharSequence, apiKey: CharSequence, limit: Int, offset: Int): Single<List<Gif>>
}
