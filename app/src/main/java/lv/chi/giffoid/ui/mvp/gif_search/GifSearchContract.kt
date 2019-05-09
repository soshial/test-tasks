package lv.chi.giffoid.ui.mvp.gif_search

import io.reactivex.Observable
import lv.chi.giffoid.ui.mvp.MvpPresenter
import lv.chi.giffoid.ui.mvp.MvpView

interface GifSearchContract {
    interface View : MvpView {
        fun showSearchStatus(searchStatus: SearchStatus)
        fun showSearchResult(searchResult: SearchResult)

        fun refreshSearchResults(insertedPositionStart: Int, itemCount: Int, resultsCount: Int)
        fun showError(error: Throwable?)
        fun provideEditTextObservable(): Observable<String>
    }

    interface Presenter : MvpPresenter<View> {
        fun retry()
        fun loadMoreGifs(totalItemCount: Int, lastVisibleItemId: Int)
        fun clearSearchClicked()
        val currentState: GifSearchPresenter.CurrentState
    }
}

enum class SearchStatus {
    /**
     * 2 possible cases:
     * 1) User opens activity, search text field is empty
     * 2) User searches then clears input field with a button
     */
    START,
    REQUESTING_DATA,
    /**
     * {@link SearchResult} is received
     */
    FINISHED
}

enum class SearchResult {
    NOTHING_FOUND,
    LOADED,
    /**
     * When several searches has reached its end
     */
    LOADED_EOF,
    ERROR
}