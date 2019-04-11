package lv.chi.giffoid.ui.gif_search

import lv.chi.giffoid.model.Gif
import lv.chi.giffoid.ui.mvp.BasePresenter
import javax.inject.Inject

class GifSearchPresenter @Inject constructor() : BasePresenter<GifSearchContract.View>(), GifSearchContract.Presenter {
    override fun loadGifs() {
        view?.showGifs(
            arrayListOf(
                Gif("https://www.cbronline.com/wp-content/uploads/2016/06/what-is-URL-770x503.jpg", "FIRST"),
                Gif("https://www.cbronline.com/wp-content/uploads/2016/06/what-is-URL-770x503.jpg", "SECOND"),
                Gif("https://www.cbronline.com/wp-content/uploads/2016/06/what-is-URL-770x503.jpg", "SECOND"),
                Gif("https://www.cbronline.com/wp-content/uploads/2016/06/what-is-URL-770x503.jpg", "THIRD")
            )
        )
    }
}