package lv.chi.giffoid.ui.mvp

interface MvpPresenter<L : MvpView> {
    fun bind(view: L)

    fun unbind()
}
