package lv.chi.giffoid.ui.mvp.gif_search

import io.reactivex.Single
import io.reactivex.functions.Consumer
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
                .doOnNext { view.hideSearchButton(it.isEmpty()) }
                .filter { it.length > 1 }
                .distinctUntilChanged()
                .debounce(appSettings.keyboardDebounceMs, TimeUnit.MILLISECONDS, schedulers.ui())
                .switchMapSingle { s -> loadGifs(s) }
                .doOnError { throwable -> view.showError(throwable) }
                .retry()
                .subscribe({ }, { })
        )
    }

    private fun loadGifs(searchQuery: CharSequence): Single<List<Gif>> {
        currentState.searchQuery = searchQuery
        currentState.pageNumber = 0
        currentState.gifs.clear() // we should clear our list after new search is initiated
        return launchGifsRxCode(Consumer { view?.showAllGifs(currentState.gifs) })
    }

    override fun loadMoreGifs() {
        launchGifsRxCode(Consumer { gifs -> view?.showLoadedGifs(currentState.gifs, gifs.size) })
    }

    private fun launchGifsRxCode(onSuccess: Consumer<List<Gif>>): Single<List<Gif>> {
        var listNumber = 0
        return repository.loadGifs(
            currentState.searchQuery,
            appSettings.apiKey,
            appSettings.searchBatchLimit,
            currentState.pageNumber * appSettings.searchBatchLimit // offset on the first page is 0 = 0*50, on second page is 50 = 1*50
        )
            // debugging info to control how sequentially GIF are shown in recyclerview
            .doOnSuccess {
                it.onEach { gif ->
                    gif.pageNumber = currentState.pageNumber; gif.listNumber = listNumber++
                }
            }
            // we increment page number only after we have successfully loaded it
            .doOnSuccess { currentState.gifs += it; currentState.pageNumber++ }
            .doOnSuccess(onSuccess)
            .doOnError { throwable -> view?.showError(throwable) }
    }

    override fun isLoading() = false

    override fun clearSearchClicked() {
        view?.clearSearch()
    }

    data class CurrentState(
        val gifs: MutableList<Gif>,
        var pageNumber: Int = 0,
        var searchQuery: CharSequence = ""
    )
}