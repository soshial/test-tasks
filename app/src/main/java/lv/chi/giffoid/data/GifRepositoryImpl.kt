package lv.chi.giffoid.data

import io.reactivex.Single
import lv.chi.giffoid.api.API
import lv.chi.giffoid.app.SchedulerProvider
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class GifRepositoryImpl @Inject constructor(val api: API, val schedulers: SchedulerProvider) :
    GifRepository {

    override fun loadGifs(searchQuery: String, apiKey: String, limit: Int, offset: Int): Single<List<Gif>> =
        api.getGifs(searchQuery, apiKey, limit, offset)
            .subscribeOn(schedulers.io())
            .delaySubscription(200, TimeUnit.MILLISECONDS)
            .map { gifdata -> gifdata.data }
            .observeOn(schedulers.ui())
}