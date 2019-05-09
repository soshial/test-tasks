package lv.chi.giffoid.ui.mvp.base

import android.support.annotation.CallSuper
import io.reactivex.disposables.CompositeDisposable

abstract class BasePresenter<V : MvpView> : MvpPresenter<V> {
    protected val compositeDisposable = CompositeDisposable()
    protected var view: V? = null

    @CallSuper
    override fun bind(view: V) {
        this.view = view
    }

    @CallSuper
    override fun unbind() {
        compositeDisposable.clear()
        this.view = null
    }
}
