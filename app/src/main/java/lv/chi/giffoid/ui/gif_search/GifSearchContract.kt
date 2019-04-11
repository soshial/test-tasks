package lv.chi.giffoid.ui.gif_search

import lv.chi.giffoid.model.Gif
import lv.chi.giffoid.ui.mvp.MvpPresenter
import lv.chi.giffoid.ui.mvp.MvpView

interface GifSearchContract {
    interface View : MvpView {
        fun showGifs(gifs: List<Gif>)
    }

    interface Presenter : MvpPresenter<View> {
        fun loadGifs()
    }
}