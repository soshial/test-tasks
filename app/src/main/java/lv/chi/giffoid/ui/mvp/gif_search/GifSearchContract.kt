package lv.chi.giffoid.ui.mvp.gif_search

import io.reactivex.Observable
import lv.chi.giffoid.ui.mvp.base.MvpPresenter
import lv.chi.giffoid.ui.mvp.base.MvpView

interface GifSearchContract {
    interface View : MvpView {
        fun showSearchStatus(searchStatus: SearchStatus)
        fun showSearchResult(searchResult: SearchResult)

        fun refreshSearchResults(insertedPositionStart: Int, insertedItemCount: Int, resultsCount: Int)
        fun showError(error: Throwable?)
        fun provideEditTextObservable(): Observable<String>
    }

    interface Presenter : MvpPresenter<View> {
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
    /**
     * In process of fetching data, also if encounters error
     */
    REQUESTING_DATA,
    /**
     * Only when {@link SearchResult} is received without error
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