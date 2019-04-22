package lv.chi.giffoid.api

import io.reactivex.Single
import lv.chi.giffoid.data.Gif
import retrofit2.http.GET
import retrofit2.http.Query

interface API {

    @GET("v1/gifs/search")
    fun getGifs(
        @Query("q") searchQuery: CharSequence,
        @Query("api_key") apiKey: CharSequence,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int = 0
    ): Single<EnvelopeList<Gif>>

}
