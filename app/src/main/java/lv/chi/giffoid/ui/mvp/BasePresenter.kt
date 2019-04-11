package lv.chi.giffoid.ui.mvp

import io.reactivex.disposables.CompositeDisposable

abstract class BasePresenter<V : MvpView> : MvpPresenter<V> {
    protected var compositeDisposable = CompositeDisposable()
    protected var view: V? = null

    override fun bind(view: V) {
        this.view = view
    }

    override fun unbind() {
        compositeDisposable.clear()
        this.view = null
    }
}
