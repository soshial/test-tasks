package lv.chi.giffoid.ui.gif_search

import io.reactivex.Observable
import lv.chi.giffoid.data.Gif
import lv.chi.giffoid.ui.mvp.MvpPresenter
import lv.chi.giffoid.ui.mvp.MvpView

interface GifSearchContract {
    interface View : MvpView {
        fun showOrHideSearchResults(showResults: Boolean)
        fun showAllGifs(gifs: List<Gif>)
        fun showLoadedGifs(gifs: List<Gif>, sizeOfAdded: Int)
        fun showError(error: Throwable)
        fun clearSearch()
        fun hideSearchButton(hide: Boolean)
        fun provideEditTextObservable(): Observable<String>
    }

    interface Presenter : MvpPresenter<View> {
        fun loadMoreGifs()
        fun isLoading(): Boolean
        fun clearSearchClicked()
        val currentState: GifSearchPresenter.CurrentState
    }
}