package lv.chi.giffoid.api

import io.reactivex.Single
import lv.chi.giffoid.data.Gif
import retrofit2.http.GET
import retrofit2.http.Query

interface API {

    @GET("v1/gifs/search")
    fun getGifs(
        @Query("q") searchQuery: String,
        @Query("api_key") apiKey: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int = 0
    ): Single<EnvelopeList<Gif>>

}
