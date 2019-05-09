package lv.chi.giffoid.ui.mvp.base

interface MvpPresenter<L : MvpView> {
    fun bind(view: L)

    fun unbind()
}
