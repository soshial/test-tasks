package lv.chi.giffoid.data

import io.reactivex.Single
import lv.chi.giffoid.api.API
import lv.chi.giffoid.api.GiphyResponse
import lv.chi.giffoid.app.SchedulerProvider
import javax.inject.Inject

class GifRepositoryImpl @Inject constructor(private val api: API, private val schedulers: SchedulerProvider) :
    GifRepository {

    override fun loadGifs(
        searchQuery: CharSequence,
        apiKey: CharSequence,
        limit: Int,
        offset: Int
    ): Single<GiphyResponse<Gif>> =
        api.getGifs(searchQuery, apiKey, limit, offset)
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
}