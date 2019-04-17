package lv.chi.giffoid.ui.gif_search

import io.reactivex.functions.Consumer
import lv.chi.giffoid.app.AppSettings
import lv.chi.giffoid.data.Gif
import lv.chi.giffoid.data.GifRepository
import lv.chi.giffoid.ui.mvp.BasePresenter
import javax.inject.Inject

class GifSearchPresenter @Inject constructor(
    private val repository: GifRepository,
    private val appSettings: AppSettings
) : BasePresenter<GifSearchContract.View>(), GifSearchContract.Presenter {

    private var hasLoadedBefore: Boolean = false // todo
    private var searchQuery: String = ""
    private val gifs: MutableList<Gif> = mutableListOf()
    private var pageNumber: Int = 0
    override var isLoading = false

    override fun loadGifs(searchQuery: String) {
        if (isLoading || hasLoadedBefore) return
        this.searchQuery = searchQuery
        pageNumber = 0
        gifs.clear() // we should clear our list after new search is initiated
        hasLoadedBefore = true
        launchGifsRxCode(Consumer { view?.showAllGifs(this.gifs) })
    }

    override fun loadMoreGifs() {
        if (isLoading) return
        launchGifsRxCode(Consumer { gifs -> view?.showLoadedGifs(this.gifs, gifs.size) })
    }

    private fun launchGifsRxCode(onSuccess: Consumer<List<Gif>>) {
        var listNumber = 0
        isLoading = true
        compositeDisposable.add(
            repository.loadGifs(
                searchQuery,
"jhgkhg",//                appSettings.apiKey,
                appSettings.searchBatchLimit,
                pageNumber * appSettings.searchBatchLimit // offset on the first page is 0 = 0*50, on second page is 50 = 1*50
            )
                // debugging info to control how sequentially GIF are shown in recyclerview
                .doOnSuccess { it.onEach { gif -> gif.pageNumber = pageNumber; gif.listNumber = listNumber++ } }
                // we increment page number only after we have successfully loaded it
                .doOnSuccess { gifs += it; pageNumber++ }
                .doOnTerminate { isLoading = false }
                .subscribe(
                    { gifs -> onSuccess.accept(gifs) },
                    { throwable -> view?.showError(throwable) })
        )
    }
}