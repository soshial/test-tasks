package lv.chi.giffoid.ui.mvvm.gif_search

import android.arch.lifecycle.MutableLiveData
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import lv.chi.giffoid.app.AppSettings
import lv.chi.giffoid.app.SchedulerProvider
import lv.chi.giffoid.data.Gif
import lv.chi.giffoid.data.GifRepository
import lv.chi.giffoid.ui.mvp.gif_search.SearchResult
import lv.chi.giffoid.ui.mvp.gif_search.SearchStatus
import lv.chi.giffoid.ui.mvvm.base.NonNullMutableLiveData
import lv.chi.giffoid.ui.mvvm.base.SingleLiveEvent
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class GifViewModel @Inject constructor(
    private val repository: GifRepository,
    private val appSettings: AppSettings,
    private val schedulers: SchedulerProvider
) : BaseViewModel() {
    val currentState = CurrentStateMvvm()
    private val compositeDisposable = CompositeDisposable()

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }

    fun init(editTextObservable: Observable<String>) {
        compositeDisposable.add(
            editTextObservable
                .map(String::trim)
                .filter { it.length > 1 }
                .distinctUntilChanged()
                .debounce(appSettings.keyboardDebounceMs, TimeUnit.MILLISECONDS, schedulers.ui())
                .switchMapSingle { s -> loadGifs(s) }
                .subscribe({ }, { })
        )
    }

    private fun loadGifs(searchQuery: String): Single<List<Gif>> {
        currentState.searchQuery.value = searchQuery
        currentState.pageNumber = 0
        currentState.gifs.clear() // we should clear our list after new search is initiated
        return getGifLoaderSingle()
    }

    fun loadMoreGifs(totalItemCount: Int, lastVisibleItemId: Int) {
        if (currentState.gifs.size < currentState.totalCount && lastVisibleItemId >= totalItemCount - appSettings.visibleThreshold) {
            retry()
        }
    }

    /**
     * Retries to load data again (both initial search and pagination)
     */
    fun retry() {
        if (currentState.searchStatus.value != SearchStatus.REQUESTING_DATA) {
            compositeDisposable.add(
                getGifLoaderSingle().subscribe({ }, { })
            )
        }
    }

    private fun getGifLoaderSingle(): Single<List<Gif>> {
        var listNumber = 0
        changeSearchStatus(SearchStatus.REQUESTING_DATA)
        return repository.loadGifs(
            currentState.searchQuery.value,
            appSettings.apiKey,
            appSettings.searchBatchLimit,
            currentState.pageNumber * appSettings.searchBatchLimit // offset on the very first page is 0 = 0*searchBatchLimit
        )
            // extract total search result count from response and figure out SearchResult
            .doOnSuccess { response ->
                currentState.totalCount = response.pagination.totalCount
                currentState.offset = response.pagination.offset
                changeSearchStatus(SearchStatus.FINISHED)
                currentState.searchResult.value = when {
                    currentState.totalCount == 0 -> SearchResult.NOTHING_FOUND
                    response.data.size == appSettings.searchBatchLimit -> SearchResult.LOADED
                    else -> SearchResult.LOADED_EOF
                }
            }
            .map { it.data }
            // debugging info to control how sequentially GIF are shown in recyclerview
            .doOnSuccess {
                it.onEach { gif ->
                    gif.pageNumber = currentState.pageNumber; gif.listNumber = listNumber++
                }
            }
            .doOnSuccess { gifs ->
                val insertedPositionStart = currentState.gifs.size
                currentState.gifs.addAll(gifs)
                currentState.gifsUpdatedIndex.value = insertedPositionStart

                // we increment page number only after we have successfully loaded it
                currentState.pageNumber++
            }
            .doOnError { throwable -> currentState.errorResult.value = throwable }
            // retry with timeout
            .retryWhen { throwables ->
                throwables.delay(
                    appSettings.retryTimeoutSec,
                    TimeUnit.SECONDS,
                    schedulers.ui()
                )
            }
    }

    private fun changeSearchStatus(searchStatus: SearchStatus) {
        currentState.searchStatus.value = searchStatus
    }

    fun onClearSearch() {
        currentState.searchStatus.value = SearchStatus.START
    }

    data class CurrentStateMvvm(
        val gifs: MutableList<Gif> = mutableListOf(),
        val gifsUpdatedIndex: NonNullMutableLiveData<Int> = NonNullMutableLiveData(
            0
        ),
        var pageNumber: Int = 0,
        var offset: Int = 0,
        var totalCount: Int = -1,
        val searchQuery: NonNullMutableLiveData<String> = NonNullMutableLiveData(
            ""
        ),
        val searchStatus: MutableLiveData<SearchStatus> = MutableLiveData(),
        val searchResult: MutableLiveData<SearchResult> = MutableLiveData(),
        val errorResult: SingleLiveEvent<Throwable> = SingleLiveEvent()
    ) {
        init {
            // searchQuery.value = ""
            searchStatus.value = SearchStatus.START
        }
    }
}

