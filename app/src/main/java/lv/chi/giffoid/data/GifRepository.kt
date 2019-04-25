package lv.chi.giffoid.data

import io.reactivex.Single
import lv.chi.giffoid.api.GiphyResponse

interface GifRepository {
    fun loadGifs(searchQuery: CharSequence, apiKey: CharSequence, limit: Int, offset: Int): Single<GiphyResponse<Gif>>
}
