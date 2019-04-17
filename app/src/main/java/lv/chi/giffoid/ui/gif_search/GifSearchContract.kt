package lv.chi.giffoid.ui.gif_search

import lv.chi.giffoid.data.Gif
import lv.chi.giffoid.ui.mvp.MvpPresenter
import lv.chi.giffoid.ui.mvp.MvpView

interface GifSearchContract {
    interface View : MvpView {
        fun showAllGifs(gifs: List<Gif>)
        fun showLoadedGifs(gifs: List<Gif>, sizeOfAdded: Int)
        fun showError(error: Throwable)
    }

    interface Presenter : MvpPresenter<View> {
        fun loadGifs(searchQuery: String = "Harry Potter")
        fun loadMoreGifs()
        var isLoading: Boolean
    }
}