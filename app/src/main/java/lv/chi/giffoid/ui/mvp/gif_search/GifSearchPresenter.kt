package lv.chi.giffoid.ui.mvp.gif_search

import io.reactivex.Single
import lv.chi.giffoid.app.AppSettings
import lv.chi.giffoid.app.SchedulerProvider
import lv.chi.giffoid.data.Gif
import lv.chi.giffoid.data.GifRepository
import lv.chi.giffoid.ui.mvp.BasePresenter
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class GifSearchPresenter @Inject constructor(
    private val repository: GifRepository,
    private val appSettings: AppSettings,
    private val schedulers: SchedulerProvider
) : BasePresenter<GifSearchContract.View>(), GifSearchContract.Presenter {

    override val currentState = CurrentState(mutableListOf())

    override fun bind(view: GifSearchContract.View) {
        super.bind(view)
        // bind to user's search actions flow
        compositeDisposable.add(
            view.provideEditTextObservable()
                .filter { it.length > 1 }
                .distinctUntilChanged()
                .debounce(appSettings.keyboardDebounceMs, TimeUnit.MILLISECONDS, schedulers.ui())
                .switchMapSingle { s -> loadGifs(s) }
                .retry()
                .subscribe({ }, { })
        )
    }

    private fun loadGifs(searchQuery: CharSequence): Single<List<Gif>> {
        currentState.searchQuery = searchQuery
        currentState.pageNumber = 0
        currentState.gifs.clear() // we should clear our list after new search is initiated
        return getGifLoaderSingle()
    }

    override fun loadMoreGifs(totalItemCount: Int, lastVisibleItemId: Int) {
        if (lastVisibleItemId >= totalItemCount - appSettings.visibleThreshold) {
            retry()
        }
    }

    /**
     * Retries to load data again (both initial search and pagination)
     */
    override fun retry() {
        if (currentState.searchStatus != SearchStatus.REQUESTING_DATA) {
            compositeDisposable.add(
                getGifLoaderSingle().subscribe({ }, { })
            )
        }
    }

    private fun getGifLoaderSingle(): Single<List<Gif>> {
        var listNumber = 0
        changeSearchStatus(SearchStatus.REQUESTING_DATA)
        return repository.loadGifs(
            currentState.searchQuery,
            appSettings.apiKey,
            appSettings.searchBatchLimit,
            currentState.pageNumber * appSettings.searchBatchLimit // offset on the first page is 0 = 0*50, on second page is 50 = 1*50
        )
            // extract total search result count from response and figure out SearchResult
            .doOnSuccess { response ->
                currentState.totalCount = response.pagination.totalCount
                currentState.offset = response.pagination.offset
                changeSearchStatus(SearchStatus.FINISHED)
                view?.showSearchResult(
                    when {
                        currentState.totalCount == 0 -> SearchResult.NOTHING_FOUND
                        response.data.size == appSettings.searchBatchLimit -> SearchResult.LOADED
                        else -> SearchResult.LOADED_EOF
                    }
                )
            }
            .map { it.data }
            // debugging info to control how sequentially GIF are shown in recyclerview
            .doOnSuccess {
                it.onEach { gif ->
                    gif.pageNumber = currentState.pageNumber; gif.listNumber = listNumber++
                }
            }
            .doOnSuccess { gifs ->
                val itemCount = gifs.size
                val positionStart = currentState.gifs.size
                currentState.gifs += gifs
                // we increment page number only after we have successfully loaded it
                currentState.pageNumber++
                view?.refreshSearchResults(positionStart, itemCount)
            }
            .doOnError { throwable -> view?.showError(throwable) }
    }

    private fun changeSearchStatus(searchStatus: SearchStatus) {
        currentState.searchStatus = searchStatus
        view?.showSearchStatus(searchStatus)
    }

    override fun clearSearchClicked() {
        changeSearchStatus(SearchStatus.START)
    }

    data class CurrentState(
        val gifs: MutableList<Gif>,
        var pageNumber: Int = 0,
        var offset: Int = 0,
        var totalCount: Int = -1,
        var searchQuery: CharSequence = "",
        var searchStatus: SearchStatus = SearchStatus.START
    )
}