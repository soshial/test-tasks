package lv.chi.giffoid.data

import io.reactivex.Single

interface GifRepository {
    fun loadGifs(searchQuery: String, apiKey: String, limit: Int, offset: Int): Single<List<Gif>>
}
