package lv.chi.giffoid.data

import io.reactivex.Single
import lv.chi.giffoid.api.API
import lv.chi.giffoid.app.SchedulerProvider
import javax.inject.Inject

class GifRepositoryImpl @Inject constructor(val api: API, val schedulers: SchedulerProvider) :
    GifRepository {

    override fun loadGifs(searchQuery: CharSequence, apiKey: CharSequence, limit: Int, offset: Int): Single<List<Gif>> =
        api.getGifs(searchQuery, apiKey, limit, offset)
            .subscribeOn(schedulers.io())
            .map { gifdata -> gifdata.data }
            .observeOn(schedulers.ui())
}