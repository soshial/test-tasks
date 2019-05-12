package lv.chi.giffoid.ui.mvp.gif_search

import io.reactivex.Single
import io.reactivex.disposables.Disposable
import lv.chi.giffoid.app.AppSettings
import lv.chi.giffoid.app.SchedulerProvider
import lv.chi.giffoid.data.Gif
import lv.chi.giffoid.data.GifRepository
import lv.chi.giffoid.ui.mvp.base.BasePresenter
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class GifSearchPresenter @Inject constructor(
    private val repository: GifRepository,
    private val appSettings: AppSettings,
    private val schedulers: SchedulerProvider
) : BasePresenter<GifSearchContract.View>(), GifSearchContract.Presenter {

    override val currentState = CurrentState(mutableListOf())
    var disposableLoadMore: Disposable? = null

    override fun bind(view: GifSearchContract.View) {
        super.bind(view)
        // bind to user's search actions flow
        compositeDisposable.add(
            view.provideEditTextObservable()
                .filter { it.length > 1 }
                .distinctUntilChanged()
                .debounce(appSettings.keyboardDebounceMs, TimeUnit.MILLISECONDS, schedulers.ui())
                .switchMapSingle { searchQuery ->
                    disposableLoadMore?.dispose() // load data for new string has higher priority, so we cancel "load more" request TODO fix a RecyclerView crash
                    currentState.searchQuery = searchQuery
                    currentState.pageNumber = 0
                    currentState.gifs.clear() // we should clear our list after new search is initiated
                    getGifLoaderSingle()
                }
                .subscribe({ }, { })
        )
    }

    /**
     * Retries to load additional data during pagination
     */
    override fun loadMoreGifs(totalItemCount: Int, lastVisibleItemId: Int) {
        if (currentState.gifs.size < currentState.totalCount && lastVisibleItemId >= totalItemCount - appSettings.visibleThreshold
            && currentState.searchStatus != SearchStatus.REQUESTING_DATA
        ) {
            // we can load
            val isLoadingMoreDataNow = !(disposableLoadMore?.isDisposed ?: true)
            if (!isLoadingMoreDataNow) {
                val disposableTmp = getGifLoaderSingle().subscribe({ }, { })
                compositeDisposable.add(disposableTmp)
                disposableLoadMore = disposableTmp
            }
        }
    }

    private fun getGifLoaderSingle(): Single<List<Gif>> {
        changeSearchStatus(SearchStatus.REQUESTING_DATA)
        var listNumber = 0
        return repository.loadGifs(
            currentState.searchQuery,
            appSettings.apiKey,
            appSettings.searchBatchLimit,
            currentState.pageNumber * appSettings.searchBatchLimit // offset on the very first page is 0 = 0*searchBatchLimit
        )
            .observeOn(schedulers.ui())
            // extract total search result count from response and figure out SearchResult
            .doOnSuccess { giphyResponse ->
                currentState.totalCount = giphyResponse.pagination.totalCount
                currentState.offset = giphyResponse.pagination.offset
                changeSearchStatus(SearchStatus.FINISHED)
                changeSearchResult(
                    when {
                        currentState.totalCount == 0 -> SearchResult.NOTHING_FOUND
                        giphyResponse.data.size == appSettings.searchBatchLimit -> SearchResult.LOADED
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
                val insertedItemCount = gifs.size
                val positionStart = currentState.gifs.size
                currentState.gifs += gifs
                // we increment page number only after we have successfully loaded it
                currentState.pageNumber++
                view?.refreshSearchResults(positionStart, insertedItemCount, currentState.totalCount)
            }
            .doOnError { throwable -> view?.showError(throwable); changeSearchResult(SearchResult.ERROR) }
            // retry with timeout, forever
            .retryWhen { throwables ->
                throwables.delay(
                    appSettings.retryTimeoutSec,
                    TimeUnit.SECONDS,
                    schedulers.ui()
                )
            }
    }

    private fun changeSearchStatus(searchStatus: SearchStatus) {
        currentState.searchStatus = searchStatus
        view?.showSearchStatus(searchStatus)
    }

    private fun changeSearchResult(searchResult: SearchResult) {
        currentState.searchResult = searchResult
        view?.showSearchResult(searchResult)
    }

    override fun clearSearchClicked() {
        currentState.gifs.clear()
        changeSearchStatus(SearchStatus.START)
    }

    data class CurrentState(
        val gifs: MutableList<Gif>,
        var pageNumber: Int = 0,
        var offset: Int = 0,
        var totalCount: Int = -1,
        var searchQuery: String = "",
        var searchStatus: SearchStatus = SearchStatus.START,
        var searchResult: SearchResult = SearchResult.LOADED
    )
}