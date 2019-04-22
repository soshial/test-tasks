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

    override val currentState = CurrentState(mutableListOf())

    override fun bind(view: GifSearchContract.View) {
        super.bind(view)
//        compositeDisposable.add()
    }

    override fun loadGifs(searchQuery: String) {
        // disregard unchanged or short strings
        if (currentState.isLoading || searchQuery.length < 2 || currentState.searchQuery.equals(searchQuery)) return
        currentState.searchQuery = searchQuery.trim()
        currentState.pageNumber = 0
        currentState.gifs.clear() // we should clear our list after new search is initiated
        launchGifsRxCode(Consumer { view?.showAllGifs(currentState.gifs) })
    }

    override fun loadMoreGifs() {
        if (currentState.isLoading) return
        launchGifsRxCode(Consumer { gifs -> view?.showLoadedGifs(currentState.gifs, gifs.size) })
    }

    private fun launchGifsRxCode(onSuccess: Consumer<List<Gif>>) {
        var listNumber = 0
        currentState.isLoading = true
        compositeDisposable.add(
            repository.loadGifs(
                currentState.searchQuery,
                appSettings.apiKey,
                appSettings.searchBatchLimit,
                currentState.pageNumber * appSettings.searchBatchLimit // offset on the first page is 0 = 0*50, on second page is 50 = 1*50
            )
//todo                .debounce(300, TimeUnit.MILLISECONDS)
//todo                  .switchMap(repository.loadGifs())
                // debugging info to control how sequentially GIF are shown in recyclerview
                .doOnSuccess {
                    it.onEach { gif ->
                        gif.pageNumber = currentState.pageNumber; gif.listNumber = listNumber++
                    }
                }
                // we increment page number only after we have successfully loaded it
                .doOnSuccess { currentState.gifs += it; currentState.pageNumber++ }
                .doAfterTerminate { currentState.isLoading = false }
                .subscribe(
                    { gifs -> onSuccess.accept(gifs) },
                    { throwable -> view?.showError(throwable) })
        )
    }

    override fun isLoading() = currentState.isLoading

    data class CurrentState(
        val gifs: MutableList<Gif>,
        var isLoading: Boolean = false,
        var pageNumber: Int = 0,
        var searchQuery: CharSequence = ""
    )
}