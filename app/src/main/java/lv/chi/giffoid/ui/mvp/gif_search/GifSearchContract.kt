package lv.chi.giffoid.ui.mvp.gif_search

import io.reactivex.Observable
import lv.chi.giffoid.ui.mvp.MvpPresenter
import lv.chi.giffoid.ui.mvp.MvpView

interface GifSearchContract {
    interface View : MvpView {
        fun showSearchStatus(searchStatus: SearchStatus)
        fun showSearchResult(searchResult: SearchResult)

        fun refreshSearchResults(positionStart: Int, itemCount: Int)
        fun showError(error: Throwable)
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
     * User opens activity, search text field is empty
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
     * When several searches has reaches its end
     */
    LOADED_EOF,
    ERROR
}