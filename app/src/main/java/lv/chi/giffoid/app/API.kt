package lv.chi.giffoid.app

import io.reactivex.Single
import lv.chi.giffoid.model.Gif
import retrofit2.http.GET
import retrofit2.http.Query

interface API {

    @GET("v1/gifs/search")
    fun getGifs(
        @Query("api_key") apiKey: String,
        @Query("q") searchQuery: String,
        @Query("limit") limit: Int = 3,
        @Query("offset") offset: Int = 0
    ): Single<List<Gif>>

}
